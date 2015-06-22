(ns gjs.e2e-test
  (:require [clojure.test :refer :all]
            [gjs.core :refer :all]
            [gjs.application-runner :refer :all]
            [gjs.fake-auction-server :refer :all]))

(def auction (atom nil))

(defn stop-auction [f]
  (reset! auction (new-fake-auction-server "item-54321"))
  (f)
  (stop-fake-auction-server @auction)
  (reset! auction nil))

(defn stop-application [f]
  (f)
  (stop-application-runner))

(use-fixtures :each stop-auction stop-application)

(deftest ^:e2e sniper-joins-auction-until-auction-closes
  (start-selling-item @auction)
  (start-bidding-in @auction)
  (has-received-join-request-from-sniper @auction sniper-id)
  (announce-closed @auction)
  (shows-sniper-has-lost-auction))

(deftest ^:e2e sniper-makes-a-higher-bid-but-loses
  (start-selling-item @auction)
  (start-bidding-in @auction)
  (has-received-join-request-from-sniper @auction sniper-id)
  (report-price @auction 1000 98 "other bidder")
  (has-shown-sniper-is-bidding)
  (has-received-bid @auction 1098 sniper-id)
  (announce-closed @auction)
  (shows-sniper-has-lost-auction))