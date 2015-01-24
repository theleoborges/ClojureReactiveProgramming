(ns om-pm.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :refer [put! chan <! filter< map<]]
            [om-pm.util :refer [set-transfer-data! get-transfer-data! move-card!
                                card-seq
                                next-down next-up
                                next-column previous-column
                                first-card]]
            [goog.events :as events])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:import (goog.events EventType)))

(enable-console-print!)

;; This is the project answer for exercise 6.1
;; A number of utility functions wwere added to the util namespace
;; There are comments in this file explaining the overall approach



(def cards [{:id 1
             :title "Groceries shopping"
             :description "Almond milk, mixed nuts, eggs..."
             :priority 10}
            {:id 2
             :title "Expenses"
             :description "Submit last client's expense report"
             :priority 9}])

(defn card-by-id [id]
  (first (filterv #(= id (:id %)) cards)))

(def app-state
  (atom {:cards cards
         :columns [{:title "Backlog"
                    :cards (mapv :id cards)}
                   {:title "In Progress"
                    :cards []}
                   {:title "Done"
                    :cards []}]
         :selected []}))

;; Here we make use of Om's new reference cursors so we
;; don't need to pass the needed state all the way down the components

;; To learn more about Om' Ref Cursors, check out the advanced tutorial
;; on Om's github page: https://github.com/swannodette/om/wiki/Advanced-Tutorial

(defn selected-card []
  (om/ref-cursor (:selected (om/root-cursor app-state))))

;; We change card-view to 'observe' the ref cursor above and highlight the selected card

(defn card-view [column {:keys [id title description] :as card} owner]
  (reify
    om/IRender
    (render [_]
      (let [[selected-id _] (om/observe owner (selected-card))
            style (if (= selected-id id)
                    #js {:border "2px solid red"}
                    #js {:border "1px solid black"})]
        (dom/li #js {:style style
                     :draggable true
                     :onDragStart (fn [e]
                                    (set-transfer-data! e "cardId" id)
                                    (set-transfer-data! e "sourceColumn" column))}
                (dom/span nil title)
                (dom/p nil description))))))

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

(defn listen [el type]
  (let [c (chan)]
    (events/listen el type #(put! c %))
    c))

(def UP          38)
(def RIGHT       39)
(def DOWN        40)
(def LEFT        37)
(def MOVE-RIGHT  78)
(def MOVE-LEFT   80)

(def VALID-KEYS #{UP RIGHT DOWN LEFT MOVE-RIGHT MOVE-LEFT})

;; We use core.async to create a channel that contains every key pressed in the application

(def keyup-chan  (map< #(.-keyCode %)
                       (listen js/document EventType.KEYUP)))

(defn try-move-card! [id source destination transfer-chan]
  (let [data {:card-id            id
              :source-column      source
              :destination-column destination}]
    (when (every? (comp not nil? val) data)
      (put! transfer-chan data))
    []))

(defn handle-keydown [[id column :as card] key-code columns transfer-chan]
  (if id
    (condp = key-code
      DOWN        (next-down id card-seq)
      UP          (next-up   id card-seq)
      RIGHT       (next-down id card-seq)
      LEFT        (next-up   id card-seq)
      MOVE-RIGHT  (try-move-card! id column (next-column column columns) transfer-chan)
      MOVE-LEFT   (try-move-card! id column (previous-column column columns) transfer-chan))
    (first-card columns)))

(defn project-view [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:transfer-chan   (chan)
       ;; THe keyup-chan is then filtered for keys of intrest to us
       :valid-keys-chan (filter< VALID-KEYS keyup-chan)})

    om/IWillMount
    (will-mount [_]
      (let [transfer-chan   (om/get-state owner :transfer-chan)
            valid-keys-chan (om/get-state owner :valid-keys-chan)
            selected-card   (selected-card)]
        (go-loop []
          (let [transfer-data (<! transfer-chan)]
            (om/transact! app :columns #(move-card! % transfer-data))
            (recur)))

        ;; we set up another go loop for the new channel
        (go-loop []
          (let [key-code (<! valid-keys-chan)
                columns  @(:columns app)
                card-seq (card-seq columns)]
            (om/transact! selected-card
                          #(handle-keydown % key-code columns transfer-chan))
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
