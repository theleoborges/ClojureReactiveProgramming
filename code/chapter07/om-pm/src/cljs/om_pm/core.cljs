(ns om-pm.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <!]]
            [om-pm.util :refer [set-transfer-data! get-transfer-data! move-card!]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(def cards [{:id 1
             :title "Groceries shopping"
             :description "Almond milk, mixed nuts, eggs..."}
            {:id 2
             :title "Expenses"
             :description "Submit last client's expense report"}])

(defn card-by-id [id]
  (first (filterv #(= id (:id %)) cards)))

(def app-state
  (atom {:cards cards
         :columns [{:title "Backlog"
                    :cards (mapv :id cards)}
                   {:title "In Progress"
                    :cards []}
                   {:title "Done"
                    :cards []}]}))

(defn card-view [column {:keys [id title description] :as card} owner]
  (om/component
   (dom/li #js {:style #js {:border "1px solid black"}
                :draggable true
                :onDragStart (fn [e]
                               (set-transfer-data! e "cardId" id)
                               (set-transfer-data! e "sourceColumn" column))}
           (dom/span nil title)
           (dom/p nil description))))

(defn handle-drop [e transfer-chan column-title]
  (.preventDefault e)
  (let [data {:card-id            (js/parseInt (get-transfer-data! e "cardId"))
              :source-column      (get-transfer-data! e "sourceColumn")
              :destination-column column-title}]
    (put! transfer-chan data)))

(defn column-view [{:keys [title cards]} owner]
  (reify
    om/IRenderState
    (render-state [this {:keys [transfer-chan]}]
      (dom/div #js {:style      #js {:border  "1px solid black"
                                     :float   "left"
                                     :height  "100%"
                                     :width   "320px"
                                     :padding "10px"}
                    :onDragOver #(.preventDefault %)
                    :onDrop     #(handle-drop % transfer-chan title)}
               (dom/h2 nil title)
               (apply dom/ul #js {:style #js {:list-style-type "none"
                                              :padding         "0px"}}
                      (om/build-all (partial card-view title)
                                    (mapv card-by-id cards)))))))

(defn project-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:transfer-chan (chan)})

    om/IWillMount
    (will-mount [_]
      (let [transfer-chan (om/get-state owner :transfer-chan)]
        (go-loop []
          (let [transfer-data (<! transfer-chan)]
            (om/transact! app :columns #(move-card! % transfer-data))
            (recur)))))

    om/IRenderState
    (render-state [this state]
      (dom/div nil
               (apply dom/ul nil
                      (om/build-all column-view (:columns app)
                                    {:shared     {:cards (:cards app)}
                                     :init-state state}))))))

(om/root project-view app-state
         {:target (. js/document (getElementById "app"))})
