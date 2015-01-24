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
