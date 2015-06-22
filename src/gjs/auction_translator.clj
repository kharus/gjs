(ns gjs.auction-translator
  (:require [clojure.core.async :refer
             [chan >!! <! alts!! timeout go put! go-loop >!]]
            [clojure.string :refer [trim split lower-case]]
            [gjs.smack :refer :all]))

(defn translate-message [message]
  (let [body (.getBody message)
        segments (split body #"\s*;\s*")
        pairs (map #(split % #"\s*:\s*") segments)
        event (map (fn [[k v]] [(keyword (lower-case k)) v]) pairs)]
    (into {} event)))

(defn translate [message channel]
  (go-loop [current (<! message)]
           (let [event (translate-message current)]
             (case (:event event)
               "CLOSE" (>! channel [:auction-closed])
               "PRICE" (>! channel [:current-price
                                    (Integer/parseInt (:currentprice event))
                                    (Integer/parseInt (:increment event))])))
           (recur (<! message))))