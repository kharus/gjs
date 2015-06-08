(ns gjs.fake-auction-server
  (:require [gjs.core :refer :all])
  (:import
    (org.jivesoftware.smack.tcp XMPPTCPConnection XMPPTCPConnectionConfiguration)
    (org.jivesoftware.smack.chat ChatManagerListener ChatManager ChatMessageListener)
    (org.jivesoftware.smack ConnectionConfiguration$SecurityMode MessageListener)
    (org.junit Assert)
    (org.jivesoftware.smack.packet Message)
    (java.util.concurrent ArrayBlockingQueue TimeUnit)
    (org.hamcrest Matchers)))

(def item-id-as-login "auction-%s")
(def auction-password "auction")
(def messages (ArrayBlockingQueue. 1))
(def message-listener
  (reify
    ChatMessageListener
    (processMessage [this chat message]
      (.add messages message))))

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
        chat-listner (reify
                       ChatManagerListener
                       (chatCreated [this chat created-locally]
                         (reset! current-chat chat)
                         (.addMessageListener chat message-listener)))]
    (.addChatListener chat-manager chat-listner)))

(defn has-received-join-request-from-sniper []
  (Assert/assertThat "Message"
                     (.poll messages 10 TimeUnit/SECONDS)
                     (Matchers/is (Matchers/notNullValue))))

(defn announce-closed [{:keys [item-id connection current-chat]}]
  (.sendMessage @current-chat (Message.)))

(defn stop-fake-auction-server [{:keys [item-id connection current-chat]}]
  (.disconnect connection))