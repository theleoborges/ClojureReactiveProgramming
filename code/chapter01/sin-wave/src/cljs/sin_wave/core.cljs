(ns sin-wave.core)

;; helper functions and RxJS wrappers
(def concat    js/Rx.Observable.concat)
(def defer     js/Rx.Observable.defer)
(def interval  js/Rx.Observable.interval)

(defn degrees-to-radians [n]
  (* (/ Math/PI 180)
     n))


(def canvas (.getElementById js/document "myCanvas"))
(def ctx    (.getContext canvas "2d"))

;; Clear canvas before doing anything else
(.clearRect ctx 0 0 (.-width canvas) (.-height canvas))


(def time (interval 10))

(def sine-wave
  (-> time
      (.map (fn [x]
              (let [sin (Math/sin (degrees-to-radians x))
                    y   (- 100 (* sin 75))]
                {:x x
                 :y y
                 :sin sin})))))

(def colour (.map sine-wave
                  (fn [{:keys [sin]}]
                    (if (< sin 0)
                      "red"
                      "blue"))))

(def red  (.map time (fn [_] "red")))
(def blue (.map time (fn [_] "blue")))

(def mouse-click (.fromEvent js/Rx.Observable canvas "click"))

(def cycle-colour
  (concat (.takeUntil red mouse-click)
          (defer #(concat (.takeUntil blue mouse-click)
                          cycle-colour))))

(defn draw-rect [x y colour]
  (set! (.-fillStyle ctx) colour)
  (.fillRect ctx x y 2 2))

;; zip with cycle-colour to let user control colour change
(-> (.zip sine-wave colour #(vector % %2))
    (.take 600)
    (.subscribe (fn [[{:keys [x y]} colour]]
                  (draw-rect x y colour))))
