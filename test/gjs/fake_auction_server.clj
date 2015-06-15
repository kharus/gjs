(ns gjs.fake-auction-server
  (:require [gjs.core :refer :all]
            [gjs.smack :refer :all]
            [clojure.core.async :refer [chan >!! <!! alts!! timeout go <!]])
  (:import
    (org.jivesoftware.smack.tcp XMPPTCPConnection XMPPTCPConnectionConfiguration)
    (org.jivesoftware.smack.chat ChatManager)
    (org.jivesoftware.smack ConnectionConfiguration$SecurityMode)
    (org.junit Assert)
    (org.jivesoftware.smack.packet Message)
    (org.hamcrest Matchers)))

(def item-id-as-login "auction-%s")
(def auction-password "auction")
(def messages-channel (chan))

(defn new-fake-auction-server [item-id]
  (let [config (-> (XMPPTCPConnectionConfiguration/builder)
                   (.setServiceName xmpp-servicename)
                   (.setHost xmpp-hostname)
                   (.setSecurityMode ConnectionConfiguration$SecurityMode/disabled)
                   .build)]
    {:item-id      item-id
     :connection   (XMPPTCPConnection. config)
     :current-chat (atom nil)}))

(defn start-selling-item [{:keys [item-id connection current-chat]}]
  (.connect connection)
  (.login connection (format item-id-as-login item-id)
          auction-password, auction-resource)
  (let [chat-manager (ChatManager/getInstanceFor connection)
        chat-channel (chan)
        chat-listner (new-chat-listener chat-channel)]
    (.addChatListener chat-manager chat-listner)
    (go (let [[chat created-locally] (<! chat-channel)]
              (reset! current-chat chat)
              (.addMessageListener chat (new-message-listener messages-channel))))))

(defn has-received-join-request-from-sniper []
  (Assert/assertThat "Message"
                     (alts!! [messages-channel (timeout 5000)])
                     (Matchers/is (Matchers/notNullValue))))

(defn announce-closed [{:keys [item-id connection current-chat]}]
  (.sendMessage @current-chat (Message.)))

(defn stop-fake-auction-server [{:keys [item-id connection current-chat]}]
  (.disconnect connection))