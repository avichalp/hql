(ns hql.core-test
  (:require [clojure.test :refer :all]
            [hql.core]))

(defmacro deftest-graphql
  [& ts]
  `(deftest graphql-test
     ~@(mapv (fn [[d gq q]]
               `(testing ~d
                  (is (= ~gq (hql.core/graphql ~q)))))
             ts)))

(deftest-graphql
  ["Test a simple query"
   "query {viewer{login}}"
   [:query
    [[:viewer
      [[:login]]]]]]

  ["Test query with variables"
   "query  ($number_of_repos: Int!){viewer{name repositories(last: $number_of_repos){nodes{name}}}}"
   [:query
    {:$number_of_repos 'Int!}
    [[:viewer
      [[:name]
       [:repositories
        {:last '$number_of_repos}
        [[:nodes
          [[:name]]]]]]]]]]

  ["Query last 20 closed issues in a repo from Github"
   "query issuesQuery ($owner: String!, $repo: String!){repo: repository(owner: $owner, name: $repo){issues: issues(last: 20, states: CLOSED){edges{node{labels(first: 5){edges{node{name}}}}}}}}"
   [:query
    "issuesQuery"
    {:$owner 'String! :$repo 'String!}
    [[:repository
      "repo"
      {:owner '$owner, :name '$repo}
      [[:issues
        "issues"
        {:last 20, :states 'CLOSED}
        [[:edges
          [[:node
            [[:labels
              {:first 5}
              [[:edges
                [[:node
                  [[:name]]]]]]]]]]]]]]]]]]

  ["Test Mutation, add a reaction to a Github comment"
   "mutation AddReactionToIssue{addReaction(input: {subjectId: \"MDU6SXNzdWUyMzEzOTE1NTE=\", content: HOORAY}){reaction{content} subject{id}}}"
   [:mutation
    "AddReactionToIssue"
    [[:addReaction
      {:input {:subjectId "MDU6SXNzdWUyMzEzOTE1NTE="
               :content   'HOORAY}}
      [[:reaction
        [[:content]]]
       [:subject
        [[:id]]]]]]]]

  ["Test a GraphQL document with query and fragments and inline fragments"
   "query MyQuery ($myId: Int!){...FriendFields ... on User{friends{count}} user(id: $myId){profilePic}}fragment friendFields on User{profilePic(size: 50)}"
   [[:query
     "MyQuery"
     {:$myId 'Int!}
     [[:fragment "FriendFields"]
      [:fragment
       {:on 'User}
       [[:friends
         [[:count]]]]]
      [:user
       {:id '$myId}
       [[:profilePic]]]]]
    [:fragment
     "friendFields"
     {:on 'User}
     [[:profilePic
       {:size 50}]]]]]
  )

(comment
  (run-tests *ns*))
