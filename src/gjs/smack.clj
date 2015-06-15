(ns gjs.smack
  (:require [clojure.core.async :refer [put!]])
  (:import
    (org.jivesoftware.smack.chat ChatManagerListener ChatManager ChatMessageListener)))

(defn new-message-listener [channel]
  (reify
    ChatMessageListener
    (processMessage [this chat message]
      (put! channel [chat message]))))

(defn new-chat-listener [channel]
  (reify
    ChatManagerListener
    (chatCreated [this chat created-locally]
      (put! channel [chat created-locally]))))