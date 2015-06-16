(ns gjs.application-runner
  (:require [gjs.core :refer :all]
            [gjs.auction-sniper-driver :refer :all]))

(def sniper-id "sniper")
(def sniper-password "sniper")
(def driver (atom nil))

(defn start-bidding-in [auction]
  (future (-main xmpp-hostname sniper-id, sniper-password, (:item-id auction)))
  (reset! driver (init-driver 1000))
  (shows-sniper-status @driver status-joining))

(defn shows-sniper-has-lost-auction []
  (shows-sniper-status @driver status-lost))

(defn has-shown-sniper-is-bidding [])

(defn stop-application-runner []
  (.dispose @driver))