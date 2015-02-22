(ns aws-dash.core
  (:require [aws-dash.observables :as obs]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def app-state (atom {:instances []}))

(defn instance-view [{:keys [instance-id type status]} owner]
  (reify
    om/IRender
    (render [this]
      (dom/tr nil
              (dom/td nil instance-id)
              (dom/td nil type)
              (dom/td nil status)))))

(defn instances-view [instances owner]
  (reify
    om/IRender
    (render [this]
      (apply dom/table #js {:style #js {:border "1px solid black;"}}
             (dom/tr nil
                     (dom/th nil "Id")
                     (dom/th nil "Type")
                     (dom/th nil "Status"))
             (om/build-all instance-view instances)))))

(om/root
 (fn [app owner]
   (dom/div nil
            (dom/h1 nil "Stack Resource Statuses")
            (om/build instances-view (:instances app))))
 app-state
 {:target (. js/document (getElementById "app"))})



(def resources (obs/stack-resources))

(.subscribe (-> (.merge  (obs/rds-instance-status resources)
                         (obs/ec2-instance-status resources))
                (.reduce conj []))
            #(swap! app-state assoc :instances %))
