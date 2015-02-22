(ns reagi-game.util)

(defn flip [f]
  (comp (partial apply f) reverse list))
