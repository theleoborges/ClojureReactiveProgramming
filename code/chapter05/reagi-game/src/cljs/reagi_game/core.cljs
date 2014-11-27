(ns reagi-game.core
  (:require [monet.canvas :as canvas]
            [reagi.core :as r]
            [clojure.set :as set]
            [reagi-game.entities :as entities
             :refer [move-forward! move-backward! rotate-left! rotate-right! fire!]]))

(def canvas-dom (.getElementById js/document "canvas"))

(def monet-canvas (canvas/init canvas-dom "2d"))

(def ship (entities/shape-data (/ (.-width (:canvas monet-canvas)) 2)
                               (/ (.-height (:canvas monet-canvas)) 2)
                               0))

(def ship-entity (entities/ship-entity ship))

(canvas/add-entity monet-canvas :ship-entity ship-entity)
(canvas/draw-loop monet-canvas)

(def UP    38)
(def RIGHT 39)
(def DOWN  40)
(def LEFT  37)
(def FIRE  32) ;; space
(def PAUSE 80) ;; lower-case P

(def GAME-KEYS #{UP RIGHT DOWN LEFT FIRE PAUSE})

(defn from-game-keys [game-keys]
  (let [active-keys (atom #{})
        update-keys! #(swap! active-keys % (.-keyCode %2))]
    (set! (.-onkeydown js/document) (partial update-keys! conj))
    (set! (.-onkeyup js/document)   (partial update-keys! disj))

    (r/sample 50 active-keys)))

(def active-keys-stream (from-game-keys GAME-KEYS))

(defn filter-map [pred f & args]
  (->> active-keys-stream
       (r/filter (partial some pred))
       (r/map (fn [_] (apply f args)))))

(filter-map #{FIRE}  fire! monet-canvas ship)
(filter-map #{UP}    move-forward!  ship)
(filter-map #{DOWN}  move-backward! ship)
(filter-map #{RIGHT} rotate-right!  ship)
(filter-map #{LEFT}  rotate-left!   ship)

(defn pause! [_]
  (if @(:updating? monet-canvas)
    (canvas/stop-updating monet-canvas)
    (canvas/start-updating monet-canvas)))

(->> active-keys-stream
     (r/filter (partial some #{PAUSE}))
     (r/throttle 100)
     (r/map pause!))
