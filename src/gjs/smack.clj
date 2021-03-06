(ns gjs.smack
  (:require [clojure.core.async :refer [put! chan >!!]])
  (:import
    (org.jivesoftware.smack.chat ChatManagerListener ChatManager ChatMessageListener Chat)
    (org.jivesoftware.smack.tcp XMPPTCPConnection XMPPTCPConnectionConfiguration)
    (org.jivesoftware.smack ConnectionConfiguration$SecurityMode)))

(defn new-host-connection [hostname servicename]
  (let [config (-> (XMPPTCPConnectionConfiguration/builder)
                   (.setServiceName servicename)
                   (.setHost hostname)
                   (.setSecurityMode ConnectionConfiguration$SecurityMode/disabled)
                   .build)]
    (XMPPTCPConnection. config)))

(defn do-login [connection username password resource]
  (doto connection
    (.connect)
    (.login username password resource)))

(defn new-message-listener [channel]
  (reify
    ChatMessageListener
    (processMessage [this chat message]
      (put! channel [chat message]))))

(defn new-chat-listener [channel]
  (let [message-channel (chan)]
    (reify
      ChatManagerListener
      (chatCreated [this chat created-locally]
        (.addMessageListener chat (new-message-listener message-channel))
        (put! channel [chat message-channel])))))

(defn listen-for-new-chats [connection]
  (let [chat-manager (ChatManager/getInstanceFor connection)
        chat-channel (chan)
        chat-listner (new-chat-listener chat-channel)]
    (.addChatListener chat-manager chat-listner)
    chat-channel))
