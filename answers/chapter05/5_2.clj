;; Exercise 5.2

(defn zip [es1 es2]
  (let [out-vec (ref [nil nil])
        out-es  (event-stream)
        emit-if-done! (fn []
                        (let [vec @out-vec]
                          (when (every? (comp not nil?) vec)
                            (deliver out-es vec)
                            (ref-set out-vec [nil nil]))))

        (subscribe es1
                   (fn [item]
                     (dosync
                      (alter out-vec (fn [[_ b]]
                                       [item b]))
                      (emit-if-done!))))
        (subscribe es2
                   (fn [item]
                     (dosync
                      (alter out-vec (fn [[a _]]
                                       [a item]))
                      (emit-if-done!))))]

    out-es))

(def es1 (from-interval 500))
(def es2 (map (from-interval 500) #(* % 2)))
(def zipped (zip es1 es2))

(def token (subscribe zipped #(prn "Zipped values: " %)))

;; "Zipped values: " [0 0]
;; "Zipped values: " [1 2]
;; "Zipped values: " [2 4]
;; "Zipped values: " [3 6]
;; "Zipped values: " [4 8]

(dispose token)
