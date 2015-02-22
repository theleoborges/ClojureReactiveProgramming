(ns core-async-transducers.core
  (:refer-clojure :exclude [into])
  (:require [clojure.core.async :refer [go chan map< filter< into >! <! go-loop close! pipe]]))

;;
;; non-transducer sequences
;;

(->> (range 10)
     (map inc)           ;; creates a new sequence
     (filter even?)      ;; creates a new sequence
     (prn "result is "))

;; "result is " (2 4 6 8 10)


;;
;; transducer sequences
;;

(def xform
  (comp (map inc)
        (filter even?)))  ;; no intermediate sequence created

(->> (range 10)
     (sequence xform)
     (prn "result is "))

;; "result is " (2 4 6 8 10)


;; non-transducer core.async

(def result (chan 10))

(def transformed
  (->> result
       (map< inc)      ;; creates a new channel
       (filter< even?) ;; creates a new channel
       (into [])))


(go
  (prn "result is " (<! transformed)))

(go
  (doseq [n (range 10)]
    (>! result n))
  (close! result))

;; "result is " [2 4 6 8 10]



;; transducer core.async

(def result (chan 10))

(def xform (comp (map inc)
                 (filter even?)))  ;; no intermediate channels created

(def transformed (->> (pipe result (chan 10 xform))
                      (into [])))


(go
  (prn "result is " (<! transformed)))

(go
  (doseq [n (range 10)]
    (>! result n))
  (close! result))

;; "result is " [2 4 6 8 10]
