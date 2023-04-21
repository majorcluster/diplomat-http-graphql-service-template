(ns {{namespace}}.ports.http.core
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [com.walmartlabs.lacinia.pedestal2 :as p2]
    [com.walmartlabs.lacinia.schema :as schema]
    [com.walmartlabs.lacinia.util :as util]
    [io.pedestal.http :as http]
    [io.pedestal.http :as server]
    [io.pedestal.http.route :as route]
    [{{namespace}}.ports.http.routes.core :as routes]))

(defn ^:private schema-resolvers
  [resolvers]
  (let [schema-files (-> (io/resource "schemas")
                         io/file
                         file-seq)
        schemas (reduce (fn [acc schema-file]
                          (let [file-map (-> schema-file
                                             slurp
                                             edn/read-string)]
                            (-> acc
                                (assoc :scalars (get file-map :scalars {})
                                       :objects (get file-map :objects {})
                                       :unions (get file-map :unions {})
                                       :interfaces (get file-map :interfaces {})
                                       :queries (get file-map :queries {})
                                       :input-objects (get file-map :input-objects {})
                                       :mutations (get file-map :mutations {})
                                       :enums (get file-map :enums {})))))
                        {:scalars       {}
                         :objects       {}
                         :unions        {}
                         :interfaces    {}
                         :queries       {}
                         :input-objects {}
                         :mutations     {}
                         :enums         {}}
                        (filter #(not (.isDirectory %)) schema-files))]
    (-> (util/inject-resolvers schemas resolvers)
        schema/compile)))

(defn add-routes
  [service-map]
  (assoc service-map ::server/routes (-> service-map
                                         (get ::server/routes #{})
                                         (clojure.set/union routes/specs)
                                         route/expand-routes)))


(def service (-> (schema-resolvers routes/graphql-specs)
                 (p2/default-service nil)
                 (merge {:env :prod
                         ::http/resource-path "/public"

                         ::http/type :jetty
                         ::http/port 8080
                         ::http/container-options {:h2c? true
                                                   :h2? false
                                                   :ssl? false}})
                 add-routes))

(defonce runnable-service (server/create-server service))

(defn start
  []
  (server/start runnable-service))

(defn start-dev
  []
  (-> service
      (merge {:env :dev
              ::server/join? false
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}
              ::server/secure-headers {:content-security-policy-settings {:object-src "'none'"}}})
      server/default-interceptors
      server/dev-interceptors
      server/create-server
      server/start))
