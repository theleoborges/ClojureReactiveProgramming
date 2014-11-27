(ns stock-market-monitor.05frp-price-monitor-rolling-avg
  (:import (java.util.concurrent TimeUnit)
           (rx Observable))
  (:use seesaw.core)
  (:require [rx.lang.clojure.interop :as rx]))

(native!)

(def main-frame (frame :title "Stock price monitor"
                       :width 200 :height 100
                       :on-close :exit))

(def price-label       (label "Price: -"))
(def running-avg-label (label "Running average: -"))

(config! main-frame :content
         (border-panel
          :north  price-label
          :center running-avg-label
          :border 5))

(defn share-price [company-code]
  (Thread/sleep 200)
  (rand-int 1000))

(defn avg [numbers]
  (float (/ (reduce + numbers)
            (count numbers))))

(defn make-price-observable [_]
  (Observable/create
   (rx/fn [observer]
     (.onNext observer (share-price "XYZ"))
     (.onCompleted observer))))

(defn -main [& args]
  (show! main-frame)
  (let [price-obs (-> (Observable/interval 500 TimeUnit/MILLISECONDS)
                      (.flatMap (rx/fn* make-price-observable))
                      (.publish))
        sliding-buffer-obs (.buffer price-obs 5 1)]
    (.subscribe price-obs
                (rx/action [price]
                           (text! price-label (str "Price: " price))))
    (.subscribe sliding-buffer-obs
                (rx/action [buffer]
                           (text! running-avg-label (str "Running average: " (avg buffer)))))
    (.connect price-obs)))
