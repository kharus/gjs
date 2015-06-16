(ns gjs.fake-auction-server
  (:require [gjs.core :refer :all]
            [gjs.smack :refer :all]
            [clojure.core.async :refer [chan >!! <!! alts!! timeout go <!]])
  (:import
    (org.junit Assert)
    (org.jivesoftware.smack.packet Message)
    (org.hamcrest Matchers)))

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
      (let [[chat created-locally] (<! chat-channel)]
        (reset! current-chat chat)
        (bind-channel chat channel)))))

(defn has-received-join-request-from-sniper [{channel :channel}]
  (Assert/assertThat "Message"
                     (alts!! [channel (timeout 5000)])
                     (Matchers/is (Matchers/notNullValue))))

(defn announce-closed [{:keys [item-id connection current-chat]}]
  (.sendMessage @current-chat (Message.)))

(defn stop-fake-auction-server [{:keys [item-id connection current-chat]}]
  (.disconnect connection))