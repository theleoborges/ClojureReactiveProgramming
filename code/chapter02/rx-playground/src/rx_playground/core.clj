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
