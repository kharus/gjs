(ns gjs.unit-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :refer [chan <!! put!]]
            [gjs.auction-translator :refer [translate]]
            [gjs.application-runner :refer :all]
            [gjs.fake-auction-server :refer :all])
  (:import (org.jivesoftware.smack.packet Message)))

(deftest ^:unit notifies-auction-closed-when-close-message-received
  (let [message (doto (Message.)
                  (.setBody "SOLVersion: 1.1; Event: CLOSE;"))
        messages (chan)
        events (chan)]
    (translate messages events)
    (put! messages [:chan message])
    (is (= [:auction-closed] (<!! events)))))

(deftest ^:unit notifies-bid-details-when-current-price-message-received
  (let [message (doto (Message.)
                  (.setBody "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;"))
        messages (chan)
        events (chan)]
    (translate messages events)
    (put! messages [:chan message])
    (is (= [:current-price 192 7] (<!! events)))))