(ns gjs.auction-sniper-driver
  (:require [gjs.core :refer :all]
            [gjs.ui-window :refer :all])
  (:import (com.objogate.wl.swing.driver JFrameDriver ComponentDriver JLabelDriver)
           (com.objogate.wl.swing.gesture GesturePerformer)
           (com.objogate.wl.swing AWTEventQueueProber)
           (org.hamcrest CoreMatchers Matcher)))

(defn init-driver [timeoutmillis]
  (JFrameDriver.
    (GesturePerformer.)
    (JFrameDriver/topLevelFrame
      (into-array Matcher
                  [(ComponentDriver/named main-window-name)
                   (ComponentDriver/showingOnScreen)]))
    (AWTEventQueueProber. timeoutmillis 100)))

(defn label [driver]
  (JLabelDriver.
    driver
    (into-array Matcher
                [(ComponentDriver/named sniper-status-name)])))

(defn shows-sniper-status [driver status-text]
  (.hasText (label driver)
            (CoreMatchers/equalTo status-text)))