(ns stock-market-monitor.03frp-price-monitor
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

(defn make-price-obs [company-code]
  (Observable/create
   (rx/fn [observer]
     (.onNext observer (share-price company-code))
     (.onCompleted observer))))

(defn -main [& args]
  (show! main-frame)
  (let [price-obs (-> (Observable/interval 500 TimeUnit/MILLISECONDS)
                      (.flatMap (rx/fn [_]  (make-price-obs "XYZ"))))]
    (.subscribe price-obs
                (rx/action [price]
                           (text! price-label (str "Price: " price))))))
