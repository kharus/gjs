(ns gjs.core-test
  (:require [clojure.test :refer :all]
            [gjs.core :refer :all]))

(def auction  (FakeAuctionServer. "item-54321"))
(def application (ApplicationRunner.))

(defn stop-auction [f]
  (f)
  (stop. auction))

(defn stop-application [f]
  (f)
  (stop. application))

(use-fixtures :each stop-auction stop-application)

(deftest sniper-joins-auction-until-auction-closes
  (startSellingItem. auction)
  (startBiddingIn. application auction)
  (hasReceivedJoinRequestFromSniper. auction)
  (announceClosed. auction)
  (showsSniperHasLostAuction. application))