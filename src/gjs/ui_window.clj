(ns gjs.ui-window
  (:import (java.awt Color)
           (javax.swing.border LineBorder)
           (javax.swing JLabel JFrame)))

(def initial-text "Joining")
(def main-window-name "Auction Sniper Main")
(def sniper-status-name "sniper status")

(defn show-status [[_ label] status]
  (.setText label status))

(defn create-main-window []
  (let [frame (JFrame. "Auction Sniper")
        label (JLabel. initial-text)]
    [frame label]))

(defn start-ui [[frame label]]
  (doto label
    (.setName sniper-status-name)
    (.setBorder (LineBorder. Color/BLACK)))
  (doto frame
    (.setName main-window-name)
    (.add label)
    (.pack)
    (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
    (.setVisible true)))