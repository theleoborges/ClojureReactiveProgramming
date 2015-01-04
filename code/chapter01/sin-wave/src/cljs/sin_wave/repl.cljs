(ns sin-wave.repl)

(comment


  (.log js/console "hello clojurescript")


  (-> time
      (.take 5)
      (.subscribe (fn [n]
                    (.log js/console n))))


  (.log js/console (str (sine-coord 50)))

  (-> sine-wave
      (.take 5)
      (.subscribe (fn [xysin]
                    (.log js/console (str xysin)))))






  )
