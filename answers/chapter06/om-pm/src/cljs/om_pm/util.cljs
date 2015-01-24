(ns om-pm.util)

(defn set-transfer-data! [e key value]
  (.setData (-> e .-nativeEvent .-dataTransfer)
            key value))

(defn get-transfer-data! [e key]
  (-> (-> e .-nativeEvent .-dataTransfer)
      (.getData key)))

(defn column-idx [title columns]
  (first (keep-indexed (fn [idx column]
                         (when  (= title (:title column))
                           idx))
                       columns)))

(defn move-card! [columns {:keys [card-id source-column destination-column]}]
  (let [from (column-idx source-column      columns)
        to   (column-idx destination-column columns)]
    (-> columns
        (update-in [from :cards] (fn [cards]
                                   (remove #{card-id} cards)))
        (update-in [to   :cards] (fn [cards]
                                   (conj cards card-id))))))

(defn card-seq [columns]
  (mapcat (fn [{:keys [title cards]}]
            (map vector cards (repeat title))) columns))

(def first-card (comp first card-seq))

(defn next-sibling [pred coll]
  (first (drop 1 (drop-while pred coll))))

(defn previous-sibling [pred coll]
  (last (take-while pred coll)))

(defn next-down [id cards]
  (if-let [card (next-sibling (fn [[id' column']]
                                (not= id' id)) cards)]
    card
    []))

(defn next-up [id cards]
  (if-let [card (previous-sibling (fn [[id' column']]
                                    (not= id' id)) cards)]
    card
    []))

(defn next-column [title columns]
  (next-sibling #(not= % title) (map :title columns)))

(defn previous-column [title columns]
  (previous-sibling #(not= % title) (map :title columns)))
