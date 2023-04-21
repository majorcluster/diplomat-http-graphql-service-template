(defproject org.clojars.majorcluster/lein-template.diplomat-http-graphql-service "0.0.1"
  :description "Diplomat architecture-pedestal styled template for leiningen generation"
  :url "https://github.com/majorcluster/diplomat-http-graphql-service-template"
  :license {:name "The MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :deploy-repositories [["clojars" {:url "https://repo.clojars.org"
                                    :username :env/clojars_username
                                    :password :env/clojars_password}]]
  :eval-in-leiningen true)
