(ns core-async-playground.backpressure
  (:require [clojure.core.async
             :refer [go chan <! >! buffer dropping-buffer sliding-buffer go-loop close!] :as async]))



;; fixed buffer

(def result (chan (buffer 5)))
(go-loop []
  (<! (async/timeout 1000))
  (when-let [x (<! result)]
    (prn "Got value: " x)
    (recur)))

(go  (doseq [n (range 5)]
       (>! result n))
     (prn "Done putting values!")
     (close! result))

;; "Done putting values!"
;; "Got value: " 0
;; "Got value: " 1
;; "Got value: " 2
;; "Got value: " 3
;; "Got value: " 4


(def result (chan (buffer 2)))
(go-loop []
  (<! (async/timeout 1000))
  (when-let [x (<! result)]
    (prn "Got value: " x)
    (recur)))

(go  (doseq [n (range 5)]
       (>! result n))
     (prn "Done putting values!")
     (close! result))

;; "Got value: " 0
;; "Got value: " 1
;; "Got value: " 2
;; "Done putting values!"
;; "Got value: " 3
;; "Got value: " 4


;; dropping buffer


(def result (chan (dropping-buffer 2)))
(go-loop []
  (<! (async/timeout 1000))
  (when-let [x (<! result)]
    (prn "Got value: " x)
    (recur)))

(go  (doseq [n (range 5)]
       (>! result n))
     (prn "Done putting values!")
     (close! result))

;; "Done putting values!"
;; "Got value: " 0
;; "Got value: " 1


;; sliding buffer

(def result (chan (sliding-buffer 2)))
(go-loop []
  (<! (async/timeout 1000))
  (when-let [x (<! result)]
    (prn "Got value: " x)
    (recur)))

(go  (doseq [n (range 5)]
       (>! result n))
     (prn "Done putting values!")
     (close! result))

;; "Done putting values!"
;; "Got value: " 3
;; "Got value: " 4
