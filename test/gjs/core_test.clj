(ns gjs.core-test
  (:require [clojure.test :refer :all]
            [gjs.core :refer :all]
            [gjs.application-runner :refer :all]
            [gjs.fake-auction-server :refer :all]))

(def auction  (new-fake-auction-server "item-54321"))
;(def application (ApplicationRunner.))

(defn stop-auction [f]
  (f)
  (stop-fake-auction-server auction))

(defn stop-application [f]
  (f)
  (stop-application-runner))

(use-fixtures :each stop-auction stop-application)

(deftest sniper-joins-auction-until-auction-closes
  (start-selling-item auction)
  (start-bidding-in auction)
  (has-received-join-request-from-sniper auction)
  (announce-closed auction)
  (shows-sniper-has-lost-auction))