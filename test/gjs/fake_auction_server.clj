(ns gjs.fake-auction-server
  (:require [gjs.core :refer :all]
            [gjs.smack :refer :all]
            [clojure.core.async :refer [chan >!! <!! alts!! timeout go <! pipe]])
  (:import
    (org.junit Assert)
    (org.jivesoftware.smack.packet Message)
    (org.hamcrest Matchers)
    (org.jivesoftware.smack.chat Chat)))

(def item-id-as-login "auction-%s")
(def auction-password "auction")

(defn new-fake-auction-server [item-id]
  {:item-id      item-id
   :connection   (new-host-connection xmpp-hostname xmpp-servicename)
   :current-chat (atom nil)
   :channel      (chan)})

(defn start-selling-item [{:keys [item-id connection current-chat channel]}]
  (do-login connection
            (format item-id-as-login item-id)
            auction-password
            auction-resource)
  (let [chat-channel (listen-for-new-chats connection)]
    (go
      (let [[chat messages-channel] (<! chat-channel)]
        (reset! current-chat chat)
        (pipe messages-channel channel)))))

(defn recives-a-message [{channel :channel} message-matcher]
  (let [[[_ message] _] (alts!! [channel (timeout 5000)])]
    (Assert/assertThat "Message" message (Matchers/is (Matchers/notNullValue)))
    (Assert/assertThat (.getBody message) message-matcher)))

(defn has-received-join-request-from-sniper [auction]
  (recives-a-message auction (Matchers/is (Matchers/anything))))

(defn has-received-bid [{channel :channel ^Chat current-chat :current-chat} bid sniperId]
  (Assert/assertThat (.getParticipant current-chat) (Matchers/equalTo sniperId)))

(defn announce-closed [{current-chat :current-chat}]
  (.sendMessage @current-chat (Message.)))

(defn stop-fake-auction-server [{connection :connection}]
  (.disconnect connection))

(defn report-price [{:keys [item-id connection current-chat]}  price increment  bidder]
  (.sendMessage @current-chat
                (Message.
                  (format "SOLVersion: 1.1; Event: PRICE; CurrentPrice: %d; Increment: %d; Bidder: %s;"
                                    price, increment, bidder))))

