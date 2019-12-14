(ns hql.core
  "
  ### Express GraphQL queries using clojure data structures.

  #### Example:

  #### HQL

  ```Clojure
  [:user
    [[:id]
     [:name]
     [:profilePic {:size 300}]]]
  ```

  #### GRAPHQL
  ```Javascript
   {
     user {
       id
       name
       profilePic(size: 300)
      }
    }
  ```
  "
  (:require [clojure.string :as s]
            [clojure.spec.alpha :as spec]
            [expound.alpha :as expound]
            [hql.spec]))

(declare field)

(declare selection-set)

(defmulti selection first)

(defmethod selection :field
  [[_ f]]
  (field f))

(defmethod selection :fragment-spread
  [[_ {fragment-name :name}]]
  (str "..." fragment-name))

(defmethod selection :inline-fragment
  [[_ {cond :condition
       ss   :selection-set}]]
  (str "... on " (:on cond) (selection-set ss)))

(defn selection-set
  [ss]
  (str
   "{"
   (reduce (fn [f1 f2]
             (str f1 " " f2))
           (map selection ss))
   "}"))

(defn parsed-arg
  [[t v]]
  (condp = t
    :string (str "\"" v "\"")
    :list (str "[" (s/join ", " (mapv parsed-arg v)) "]")
    :object (str "{"
                 (s/join ", "
                         (mapv #(str (-> % key name)
                                     ": "
                                     (-> % val parsed-arg)) v))
                 "}")
    v))

(defn argmap->args
  "Takes a map and converts it into args
  Ex: {:id 10, :size 300} -> (id)
  "
  [args]
  (str "("
       (s/join ", "
               (map #(str (-> % key name)
                          ": "
                          (->> %
                               val
                               (spec/conform :hql.spec/value)
                               parsed-arg))
                    args))
       ")"))

(defn fname-with-args
  [fname args]
  (if (empty? args)
    (name fname)
    (str (name fname) (argmap->args args))))

(defn field-with-alias
  [^String f alias]
  (if alias
    (str alias ": " f)
    f))

(defn field
  [{fname :name
    args  :args
    alias :alias
    ss    :selection-set}]
  (if (nil? ss)
    (field-with-alias (fname-with-args fname args) alias)
    (str (field-with-alias (fname-with-args fname args) alias)
         (selection-set ss))))

(defn op-with-vars
  [optype opname vars]
  (if (empty? vars)
    (str (name optype) " " opname)
    ;; todo: use a different function for varsmap->vars
    (str (name optype) " " opname " "(argmap->args vars))))

(defn operation
  [{optype :type
    opname :name
    vars   :variables
    ss     :selection-set}]
  (str (op-with-vars optype opname vars)
       (selection-set ss)))

(defn fragment
  [{fragment :fragment
    fname    :name
    cond     :condition
    ss       :selection-set}]
  (str "fragment "
       fname
       " on "
       (:on cond)
       (selection-set ss)))

(defmulti executable first)

(defmethod executable :operation
  [[_ op]]
  (operation op))

(defmethod executable :fragment
  [[_ f]]
  (fragment f))

(defmulti hql first)

(defmethod hql :document
  [[_ d]]
  (->> d
       (mapv executable)
       (reduce str)))

(defmethod hql :operation
  [[_ op]]
  (operation op))

(defmethod hql :field
  [[_ f]]
  (field f))

(defn graphql
  [q]
  (if (spec/valid? :hql.spec/hql q)
    (->> q (spec/conform :hql.spec/hql) hql)
    (expound/expound :hql.spec/hql
                     q
                     {:print-specs? false
                      :theme        :figwheel-theme})))
