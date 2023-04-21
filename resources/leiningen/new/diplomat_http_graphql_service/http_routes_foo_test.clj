(ns {{namespace}}.ports.http.routes.foo-test
  (:require [clojure.test :refer :all]
    [io.pedestal.test :refer :all]
    [io.pedestal.http :as bootstrap]
    [{{namespace}}.ports.http.core :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(def json-header
  {"Content-Type" "application/json"})

(deftest foo-get-test
   (is (= (:body (response-for service :get "/foo"))
          "{\"a\":48,\"state\":\"active\"}"))
   (is (= "application/json"
          (get (:headers (response-for service :get "/foo")) "Content-Type"))))

(deftest foo-query-test
   (is (= (:body (response-for service :post "/api"
                               :headers json-header
                               :body "{\"query\":\"query{foo {state}}\"}"))
          "{\"data\":{\"foo\":{\"state\":\"a\"}}}"))
   (is (= (:body (response-for service :post "/api"
                               :headers json-header
                               :body "{\"query\":\"query{foo {a state}}\"}"))
          "{\"data\":{\"foo\":{\"a\":\"b\",\"state\":\"a\"}}}")))
