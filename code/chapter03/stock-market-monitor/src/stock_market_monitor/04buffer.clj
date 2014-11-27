(ns stock-market-monitor.04buffer
  (:import (java.util.concurrent TimeUnit)
           (rx Observable))
  (:require [rx.lang.clojure.interop :as rx]))

(def values (range 10))

(doseq [buffer (partition 5 1 values)]
  (prn buffer))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))

(-> (Observable/from ^clojure.lang.PersistentVector (vec (range 10)))
    (.buffer 5 1)
    (.subscribe
     (rx/action [price]
                (prn (str "Value: " price)))))
