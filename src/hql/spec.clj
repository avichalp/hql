(ns hql.spec
  (:require [clojure.spec.alpha :as spec]))

(spec/def ::name
  (spec/and string? #(re-matches #"[_a-zA-Z][_0-9a-zA-Z]*" %)))

(spec/def ::field
  (spec/cat
   :name (spec/and keyword? #(spec/valid? ::name (name %)))
   :alias (spec/? ::name)
   :args (spec/? map?)
   :selection-set (spec/? ::selection-set)))

(spec/def ::fragment-spread
  (spec/cat
   :fragment #{:fragment}
   :name string?))

(spec/def :hql.spec.typed-condition/on
  (spec/and symbol? #(spec/valid? ::name (str %))))

(spec/def ::inline-fragment
  (spec/cat
   :fragment #{:fragment}
   :condition (spec/? (spec/keys :req-un [:hql.spec.typed-condition/on]))
   ;; todo: add directives spec
   :selection-set ::selection-set))

(spec/def ::value
  (spec/or :int     int?
           :float   float?
           :string  string?
           :boolean boolean?
           :enum    symbol?
           :list    (spec/coll-of ::value :kind vector? :gen-max 10)
           :object  (spec/map-of keyword? ::value)))

(spec/def ::selection
  (spec/or :fragment-spread ::fragment-spread
           :inline-fragment ::inline-fragment
           :field ::field))

(spec/def ::selection-set
  (spec/coll-of ::selection :kind vector? :gen-max 5))

(spec/def ::operation
  (spec/cat
   :type #{:query :mutation}
   :name (spec/? (spec/and string? ::name))
   :variables (spec/? map?)
   :selection-set ::selection-set))

(spec/def ::fragment
  (spec/cat
   :type #{:fragment}
   :name string?
   :condition (spec/keys :req-un [:hql.spec.typed-condition/on])
   ;; todo: add directives spec
   :selection-set ::selection-set))

(spec/def ::executable
  (spec/or :operation ::operation
           :fragment  ::fragment))

(spec/def ::document
  (spec/coll-of ::executable :kind vector? :gen-max 5))

(comment

  (spec/conform ::selection-set
                [[:handle]
                 [:fragment "FriendFields"]
                 [:fragment
                  {:on 'User}
                  [[:friends
                    [[:count]]]]]
                 [:fragment
                  {:on 'Page}
                  [[:likers
                    [[:count]]]]]])

  (spec/conform ::inline-fragment
                [:fragment
                 {:on 'User}
                 [[:friends
                   [[:count]]]]]))
