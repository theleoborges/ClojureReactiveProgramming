(ns core-async-playground.error-handling
  (:require [clojure.core.async
             :refer [go chan <! >! map>] :as async]))


;; First, without any special treatment

(defn get-data []
  (throw (Exception. "Bad things happen!")))

(defn process []
  (let [result (chan)]
    ;; do some processing...
    (go (>! result (get-data)))
    result))


;; The go block eats the exception so we never get a chance to handle nor do we know
;; whether the processing succeeded

(go (let [result  (<! (->> (process "data")
                           (map> #(* % %))
                           (map> #(prn %))))]
      (prn "result is: " result)))


;; Using the <? macro proposed by David Nolen...


(defn throw-err [e]
  (when (instance? Throwable e) (throw e))
  e)

(defmacro <? [ch]
  `(throw-err (async/<! ~ch)))

;; And updaing our function to put the exception in the channel

(defn process []
  (let [result (chan)]
    ;; do some processing...
    (go (>! result (try (get-data)
                        (catch Exception e
                          e))))
    result))


;; We regain control of the Exceptions and can use normal try catch statements

(go (try (let [result  (<? (->> (process "data")
                                (map> #(* % %))
                                (map> #(prn %))))]
           (prn "result is: " result))
         (catch Exception e
           (prn "Oops, an error happened! We better do something about it here!"))))

;; "Oops, an error happened! We better do something about it here!"
