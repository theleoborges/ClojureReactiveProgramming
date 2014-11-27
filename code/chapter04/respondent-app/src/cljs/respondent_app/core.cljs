(ns respondent-app.core
  (:require [respondent.core :as r]
            [dommy.core :as dommy])
  (:use-macros
   [dommy.macros :only [sel1]]))


(def mouse-pos-stream (r/event-stream))
(set! (.-onmousemove js/document)
      (fn [e]
        (r/deliver mouse-pos-stream [(.-pageX e) (.-pageY e)])))

(r/subscribe mouse-pos-stream
             (fn [[x y]]
               (dommy/set-text! (sel1 :#mouse-xy)
                                (str "(" x "," y ")"))))
