(def f (clojure.core/future
         (do (println "doing some expensive work...")
             (Thread/sleep 5000)
             (println "done")
             10)))
(println "You'll see me before the future finishes")
@f
(println "I could be doing something else. Instead I'm waiting.")

;; doing some expensive work...
;; You'll see me before the future finishes
;; done

;; done
;; I could be doing something else. Instead I'm waiting.
