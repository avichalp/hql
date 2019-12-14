(defproject hql "0.0.1-SNAPSHOT"
  :description "A DSL to write GraphQL queries"

  :url "https://github.com/avichalp/hql"

  :scm {:name "git"
        :url  "https://github.com/avichalp/hql"}

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [expound "0.8.2"]]

  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]

  :profiles {:uberjar {:aot :all}
             :dev     {:dependencies [[org.clojure/test.check "0.9.0"]]}}

  :deploy-repositories [["releases" :clojars]]

  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[hql \"[0-9.]*\"\\\\]/[hql \"${:version}\"]/" "README.md"]}

  :pom-addition [:developers [:developer
                              [:name "Avichal Pandey"]
                              [:url "https://avichalp.me"]
                              [:email "hi@avichalp.me"]]]

  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
