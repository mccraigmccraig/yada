;; Copyright © 2015, JUXT LTD.

(ns phonebook.resource-types-test
  (:require
   [bidi.bidi :as bidi]
   [bidi.ring :refer [make-handler]]
   [clojure.test :refer :all]
   [juxt.iota :refer (given)]
   [clojure.edn :as edn]
   [ring.mock.request :refer [request]]
   [phonebook.util :refer (to-string)]
   [phonebook.db :as db]
   [phonebook.api :refer [api]]))

(def full-seed {1 {:surname "Sparks"
                   :firstname "Malcolm"
                   :phone "1234"}

                2 {:surname "Pither"
                   :firstname "Jon"
                   :phone "1235"}})

(defn create-api [db]
  (let [*routes (promise)
        api (api db *routes)]
    (deliver *routes api)
    api))

(deftest list-all-entries
  (let [db (db/create-db full-seed)
        h (make-handler (create-api db))
        req (merge-with merge
                        (request :get "/phonebook")
                        {:headers {"accept" "application/edn"}})
        response @(h req)]

    (given response
      :status := 200
      := nil)

    (given (-> response :body to-string edn/read-string)
      count := 2
      [first second :firstname] := "Malcolm")))

(deftest create-entry
  (let [db (db/create-db {})
        h (make-handler (create-api db))
        req (request :post "/phonebook" {"surname" "Pither" "firstname" "Jon" "phone" "1235"})
        response @(h req)]

    (given response
      :status := 303
      :headers :⊃ {"location" "/phonebook/1" "content-length" 0}
      :body := nil
      )))

(deftest delete-entry
  (let [db (db/create-db full-seed)
        h (make-handler (create-api db))]
    (is (= (db/count-entries db) 2))
    (let [req (request :delete "/phonebook/1")
          response @(h req)]
      (given response
        :status := 204
        :body := nil
        )
      (is (= (db/count-entries db) 1)))))
