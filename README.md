# HQL

[![CircleCI](https://circleci.com/gh/avichalp/hql/tree/master.svg?style=svg)](https://circleci.com/gh/avichalp/hql/tree/master)

Write graphql queries with Clojure data structures.


## Usage 
Get the latest version from Clojars

[![Latest version](https://clojars.org/hql/latest-version.svg)](https://clojars.org/hql)


Converting an EDN based query to a GraphQL query

```Clojure
(require '[hql.core :as hql])

(def q  [:user
          [:id]
          [:name]
          [:profilePic {:size 300}]])
            
(hql/graphql q)
;; ==> "user{id name profilePic(size: 300)}"
```

Lets look at some example queries.


#### Examples

1. A query to get your username from Github

```Clojure
[:query
  [:viewer
    [:login]]]
```

This will translate to:
```Javascript
query {
  viewer {
    login
  }
}
```

_Represent a `Feild` as a vector._

2. Get your last N repos using a query with variables.

```Clojure
[:query
  {:$number_of_repos 'Int!}
  [:viewer
    [:name]
    [:repositories
      {:last '$number_of_repos}
       [:nodes
         [:name]]]]]
```

```Javascript
query($number_of_repos: Int!) {
  viewer {
    name
    repositories(last: $number_of_repos) {
      nodes {
        name
      }
    }
  }
}
```

_Variables in a query are represented as a `map`. And arguments to the field `repositories`, in the above query, is represend as a `map` too._ 

3. Get closed issues from a repo:

```Clojure
[:query
  "issuesQuery"
  {:$owner 'String! :$repo 'String!}
  [:repository
    "repo"
    {:owner '$owner, :name '$repo}
    [:issues
      "issues"
      {:last 20, :states 'CLOSED}
      [:edges
        [:node
          [:labels
            {:first 5}
            [:edges
              [:node
                [:name]]]]]]]]]
```

```Javascript
query issuesQuery($owner: String!, $repo: String!) {
  repo: repository(owner: $owner, name: $repo) {
    issues: issues(last: 20, states: CLOSED) {
      edges {
        node {
          labels(first: 5) {
            edges {
              node {
                name
              }
            }
          }
        }
      }
    }
  }
}
```

_Repo and Issues feilds have aliases "repo" and "issues" in the above query.
To specify an alias add it as the second element in the field vector._

4. Use mutations to add reaction to an issue

```Clojure
[:mutation
  "AddReactionToIssue"
  [:addReaction
    {:input
      {:subjectId "MDU6SXNzdWUyMzEzOTE1NTE="
       :content   'HOORAY}}
    [:reaction
      [:content]]
    [:subject
      [:id]]]]]
```

```Javascript
mutation AddReactionToIssue {
  addReaction(
    input: { subjectId: "MDU6SXNzdWU1MjM5NzY0MDE=", content: HOORAY }
  ) {
    reaction {
      content
    }
    subject {
      id
    }
  }
}

```

## License

Copyright © 2020 Avichal

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

