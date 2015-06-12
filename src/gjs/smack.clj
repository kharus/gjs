(ns gjs.smack
  (:require [clojure.core.async :refer [chan >!! <!! alts!! timeout]])
  (:import
    (org.jivesoftware.smack.chat ChatManagerListener ChatManager ChatMessageListener)))

(defn new-message-listener [channel]
  (reify
    ChatMessageListener
    (processMessage [this chat message]
      (>!! channel [chat message]))))

(defn new-chat-listener [channel]
  (reify
    ChatManagerListener
    (chatCreated [this chat created-locally]
      (>!! channel [chat created-locally]))))