;; 01
(defn do-something-important []
  (let [f (future (do (prn "Calculating...")
                      (Thread/sleep 10000)))]
    (prn "Perhaps the future has done its job?")
    (prn @f)
    (prn "You will only see this in about 10 seconds...")))

(do-something-important)

;; 02
(defn do-something-important [callback]
  (let [f (future (let [answer 42]
                    (Thread/sleep 10000)
                    (callback answer)))]
    (prn "Perhaps the future has done its job?")
    (prn "You should see this almost immediately and then in 10 secs...")
    f))

(do-something-important (fn [answer]
                          (prn "Future is done. Answer is " answer)))


;; 03
(import 'java.util.concurrent.ArrayBlockingQueue)

(defn producer [c]
  (prn "Taking a nap")
  (Thread/sleep 5000)
  (prn "Now putting a name in queue...")
  (.put c "Leo"))

(defn consumer [c]
  (prn "Attempting to take value from queue now...")
  (prn (str "Got it. Hello " (.take c) "!")))

(def chan (ArrayBlockingQueue. 10))

(future (consumer chan))
(future (producer chan))
