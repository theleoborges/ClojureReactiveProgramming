(ns rx-playground.core)


(require '[rx.lang.clojure.core :as rx])
(import '(rx Observable))

;;
;; Creating Observables
;;

(def obs (rx/return 10))

(rx/subscribe obs
              (fn [value]
                (prn (str "Got value: " value))))


(-> (rx/seq->o [1 2 3 4 5 6 7 8 9 10])
    (rx/subscribe prn))

(-> (rx/range 1 10)
    (rx/subscribe prn))


(import '(java.util.concurrent TimeUnit))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))


(def subscription (rx/subscribe (Observable/interval 100 TimeUnit/MILLISECONDS)
                                prn-to-repl))

(Thread/sleep 1000)

(rx/unsubscribe subscription)

(defn just-obs [v]
  (rx/observable*
   (fn [observer]
     (rx/on-next observer v)
     (rx/on-completed observer))))

(rx/subscribe (just-obs 20) prn)


;;
;; Manipulating observables
;;

(rx/subscribe (->> (Observable/interval 1 TimeUnit/MICROSECONDS)
                   (rx/filter even?)
                   (rx/take 5)
                   (rx/reduce +))
                   prn-to-repl)


(defn musicians []
  (rx/seq->o ["James Hetfield" "Dave Mustaine" "Kerry King"]))

(defn bands     []
  (rx/seq->o ["Metallica" "Megadeth" "Slayer"]))

(defn uppercased-obs []
  (rx/map (fn [s] (.toUpperCase s)) (bands)))

(-> (rx/map vector
            (musicians)
            (uppercased-obs))
    (rx/subscribe (fn [[musician band]]
                    (prn-to-repl (str musician " - from: " band)))))


;;
;; Mapcatting / Flatmapping
;;

(defn factorial [n]
  (reduce * (range 1 (inc n))))

(defn all-positive-integers []
  (Observable/interval 1 TimeUnit/MICROSECONDS))

(defn fact-obs [n]
  (rx/observable*
   (fn [observer]
     (rx/on-next observer (factorial n))
     (rx/on-completed observer))))

(rx/subscribe (fact-obs 5) prn-to-repl)


(rx/subscribe (->> (all-positive-integers)
                   (rx/filter  even?)
                   (rx/flatmap fact-obs)
                   (rx/take 5))
              prn-to-repl)

(defn repeat-obs [n]
  (rx/seq->o (repeat 2 n)))

(-> (repeat-obs 5)
    (rx/subscribe prn-to-repl))


(rx/subscribe (->> (all-positive-integers)
                   (rx/flatmap repeat-obs)
                   (rx/take 6))
              prn-to-repl)



;;
;; Error handling
;;

;; onError
(defn exceptional-obs []
  (rx/observable*
   (fn [observer]
     (rx/on-next observer (throw (Exception. "Oops. Something went wrong")))
     (rx/on-completed observer))))

(rx/subscribe (->> (exceptional-obs)
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))
;; Exception Oops. Something went wrong  rx-playground.core/exceptional-obs/fn--1505

(rx/subscribe (->> (exceptional-obs)
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v))
              (fn [e] (prn-to-repl "error is " e)))

;; "error is " #<Exception java.lang.Exception: Oops. Something went wrong>


;; catch
(rx/subscribe (->> (exceptional-obs)
                   (rx/catch Exception e
                       (rx/return 10))
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))

;; "result is " 11

(rx/subscribe (->> (exceptional-obs)
                   (rx/catch Exception e
                     (rx/seq->o (range 5)))
                   (rx/map inc))
              (fn [v] (prn-to-repl "result is " v)))

;; "result is " 1
;; "result is " 2
;; "result is " 3
;; "result is " 4
;; "result is " 5

;; retry
(defn retry-obs []
  (let [errored (atom false)]
    (rx/observable*
     (fn [observer]
       (throw (Exception. "Oops. Something went wrong"))))))

(rx/subscribe (retry-obs)
              (fn [v] (prn-to-repl "result is " v)))

;; Exception Oops. Something went wrong  rx-playground.core/retry-obs/fn--1476

(rx/subscribe (->> (retry-obs)
                   (.retry))
              (fn [v] (prn-to-repl "result is " v)))

;; "result is " 20

;;
;; Backpressure
;;


(defn fast-producing-obs []
  (rx/map inc (Observable/interval 1 TimeUnit/MILLISECONDS)))

(defn slow-producing-obs []
  (rx/map inc (Observable/interval 500 TimeUnit/MILLISECONDS)))

(rx/subscribe (->> (rx/map vector
                           (fast-producing-obs)
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))

;; "error is " #<MissingBackpressureException rx.exceptions.MissingBackpressureException>

;; throttle

(rx/subscribe (->> (rx/map vector
                           (.sample (fast-producing-obs) 200
                                    TimeUnit/MILLISECONDS)
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))

;; 204
;; 404
;; 604
;; 807
;; 1010
;; 1206
;; 1407
;; 1613
;; 1813
;; 2012


;; onBackpressureBuffer


(rx/subscribe (->> (rx/map vector
                           (.onBackpressureBuffer (fast-producing-obs))
                           (slow-producing-obs))
                   (rx/map (fn [[x y]]
                             (+ x y)))
                   (rx/take 10))
              prn-to-repl
              (fn [e] (prn-to-repl "error is " e)))

;; 2
;; 4
;; 6
;; 8
;; 10
;; 12
;; 14
;; 16
;; 18
;; 20
