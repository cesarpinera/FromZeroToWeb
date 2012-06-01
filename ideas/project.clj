(defproject ideas "1.0.0-SNAPSHOT"
  :description "Ideas web app"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [korma "0.3.0-beta7"]  
                 [mysql/mysql-connector-java "5.1.6"]
                 [clj-time "0.4.2"]
                 [compojure "1.1.0"]
                 [hiccup "1.0.0"]]
  :plugins [[lein-swank "1.4.4"]
            [lein-ring "0.7.1"]]
  :ring {:handler ideas.core/app})