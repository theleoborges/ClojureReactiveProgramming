;; Exercise 1.1

(def repeat     js/Rx.Observable.repeat)
(def rainbow-colours (-> (.scan time
                                (cycle ["red"
                                        "orange"
                                        "yellow"
                                        "green"
                                        "blue"
                                        "indigo"
                                        "violet"])
                                (fn [acc _] (next acc)))
                         (.map #(first %))
                         (.flatMap #(repeat % 20))))






(-> (.zip sine-wave rainbow-colours #(vector % %2))
    (.take 600)
    (.subscribe (fn [[{:keys [x y]} colour]]
                  (fill-rect x y colour))))
