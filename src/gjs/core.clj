(ns gjs.core
  (:gen-class)
  (:require [clojure.core.async :refer [chan >!! <! alts!! timeout go put! >!]]
            [gjs.auction-translator :refer [translate]]
            [gjs.smack :refer :all]
            [gjs.ui-window :refer :all])
  (:import (javax.swing SwingUtilities)
           (org.jivesoftware.smack.chat ChatManager Chat)
           (java.awt.event WindowAdapter)))

(def xmpp-hostname "localhost")
(def xmpp-servicename "auction")


(def auction-resource "Auction")
(def status-joining "Joining")
(def status-lost "Lost")
(def status-bidding "Bidding")

(def join-command-format "SOLVersion: 1.1; Command: JOIN;")
(def bid-command-format "SOLVersion: 1.1; Command: BID; Price: %d;")

(def arg-hostname 0)
(def arg-username 1)
(def arg-password 2)
(def arg-item-id 3)

(def item-as-a-login "auction-%s")

(def auction-id-format
  (str item-as-a-login "@%s/" auction-resource))

(defn start-user-interface [ui]
  (SwingUtilities/invokeAndWait
    #(start-ui ui)))

(defn connect-to [hostname username password]
  (-> (new-host-connection hostname xmpp-servicename)
      (do-login username password auction-resource)))

(defn auction-id [item-id connection]
  (format auction-id-format item-id (.getServiceName connection)))

(defn create-auction-chat [connection auction-id listner]
  (-> connection
      ChatManager/getInstanceFor
      (.createChat auction-id listner)))

(defn close-connection-on-closed [conn]
  (proxy [WindowAdapter] []
    (windowClosed [e]
      (.disconnect conn))))

(defn -main
  [& args]

  (let [ui (create-main-window)
        connection (connect-to (nth args arg-hostname)
                               (nth args arg-username)
                               (nth args arg-password))
        auction-id (auction-id (nth args arg-item-id) connection)
        auction-channel (chan)
        austion-listner (new-message-listener auction-channel)
        ^Chat chat (create-auction-chat connection auction-id austion-listner)
        events (chan)]
    (start-user-interface ui)
    (translate auction-channel events)
    (.sendMessage chat join-command-format)
    (go (let [[_] (<! events)]
          (SwingUtilities/invokeLater #(show-status ui status-lost))))
    (.addWindowListener (ui 0) (close-connection-on-closed connection))))