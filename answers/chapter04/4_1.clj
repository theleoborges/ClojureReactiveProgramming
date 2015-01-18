;; Exercise 4.1

(defn take [es1 n]
  (let [count   (atom 0)
        out-es  (event-stream (chan n))
        dispose-token nil
        token   (subscribe es1
                           (fn [item]
                             (if (< @count n)
                               (do (deliver out-es item)
                                   (swap! count inc))
                               (deliver out-es ::complete))))]
    (add-watch count :token (fn [_ _ _ new-state]
                              (when (>= new-state n)
                                (dispose token))))

    out-es))

(def es1 (from-interval 500))
(def take-es (take es1 5))

(subscribe take-es #(prn "Take values: " %))

;; "Take values: " 0
;; "Take values: " 1
;; "Take values: " 2
;; "Take values: " 3
;; "Take values: " 4
