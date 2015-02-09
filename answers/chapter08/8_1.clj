;; Exercise 8.1

(defn resouces-stream [_]
  (let [resources (obs/stack-resources)]
    (-> (.merge  (obs/rds-instance-status resources)
                 (obs/ec2-instance-status resources))
        (.reduce conj []))))

(.subscribe (.flatMap (.interval js/Rx.Observable 100)
                      resources-stream)
            #(swap! app-state assoc :instances %))
