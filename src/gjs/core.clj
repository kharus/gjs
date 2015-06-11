(ns gjs.core
  (:gen-class)
  (:import (javax.swing JFrame SwingUtilities JLabel)
           (javax.swing.border LineBorder)
           (java.awt Color)
           (org.jivesoftware.smack ConnectionConfiguration$SecurityMode)
           (org.jivesoftware.smack.tcp XMPPTCPConnectionConfiguration XMPPTCPConnection)
           (org.jivesoftware.smack.chat ChatManager ChatMessageListener)
           (org.jivesoftware.smack.packet Message)))

(def xmpp-hostname "localhost")
(def xmpp-servicename "auction")
(def main-window-name "Auction Sniper Main")
(def main-window (atom nil))
(def sniper-status-name "sniper status")
(def initial-text "Joining")
(def auction-resource "Auction")
(def status-joining "Joining")
(def status-lost "Lost")

(def arg-hostname 0)
(def arg-username 1)
(def arg-password 2)
(def arg-item-id 3)

(def item-as-a-login "auction-%s")
(def auction-id-format
  (str item-as-a-login "@%s/" auction-resource))

(defn create-label [initial-text]
  (doto (JLabel. initial-text)
    (.setName sniper-status-name)
    (.setBorder (LineBorder. Color/BLACK))))

(def sniper-status-label (create-label initial-text))

(defn show-status [status]
  (.setText sniper-status-label status))

(defn create-main-window []
  (doto (JFrame. "Auction Sniper")
    (.setName main-window-name)
    (.add sniper-status-label)
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))

(defn start-user-interface []
  (SwingUtilities/invokeAndWait
    (fn [] (reset! main-window (create-main-window)))))

(defn connect-to [hostname username password]
  (let [config (-> (XMPPTCPConnectionConfiguration/builder)
                   (.setServiceName xmpp-servicename)
                   (.setHost hostname)
                   (.setSecurityMode ConnectionConfiguration$SecurityMode/disabled)
                   .build)]
    (doto (XMPPTCPConnection. config)
      (.connect)
      (.login username password auction-resource))))

(defn auction-id [item-id connection]
  (format auction-id-format item-id (.getServiceName connection)))

(defn new-chat-message-listener [callback]
  (reify
    ChatMessageListener
    (processMessage [this chat message]
      (callback this chat message))))

(def update-label-to-lost
  (new-chat-message-listener
    (fn [_ _ _]
      (SwingUtilities/invokeLater #(show-status status-lost)))))

(defn create-auction-chat [connection auction-id listner]
  (-> connection
      ChatManager/getInstanceFor
      (.createChat auction-id listner)))

(defn -main
  [& args]
  (start-user-interface)
  (let [connection (connect-to (nth args arg-hostname)
                               (nth args arg-username)
                               (nth args arg-password))
        auction-id (auction-id (nth args arg-item-id) connection)
        chat (create-auction-chat connection auction-id update-label-to-lost)]
      (.sendMessage chat (Message.))))