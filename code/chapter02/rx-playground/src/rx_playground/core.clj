(ns rx-playground.core)

(import '(rx Observable))
(require '[rx.lang.clojure.interop :as rx])

;;
;; Creating Observables
;;

(def obs (Observable/just 10))

(.subscribe obs
            (rx/action [value]
                       (prn (str "Got value: " value))))


(-> (Observable/from [1 2 3 4 5 6 7 8 9 10])
    (.subscribe (rx/action [n]
                           (prn n))))

(-> (Observable/range 1 10)
    (.subscribe (rx/action [n]
                           (prn n))))


(import '(java.util.concurrent TimeUnit))

(def  repl-out *out*)
(defn prn-to-repl [& args]
  (binding [*out* repl-out]
    (apply prn args)))


(def subscription (-> (Observable/interval 100 TimeUnit/MILLISECONDS)
                      (.subscribe (rx/action [n]
                                             (prn-to-repl n)))))

(Thread/sleep 1000)

(.unsubscribe subscription)

(defn just-obs [v]
  (Observable/create
   (rx/fn [observer]
     (.onNext observer v)
     (.onCompleted observer))))

(-> (just-obs 20)
    (.subscribe (rx/action [n]
                           (prn n))))


;;
;; Manipulating observables
;;

(-> (Observable/interval 1 TimeUnit/MICROSECONDS)
    (.filter (rx/fn [n] (even? n)))
    (.take 5)
    (.reduce (rx/fn* +))
    (.subscribe (rx/action [n]
                           (prn-to-repl n))))


(defn musicians []
  (Observable/from ["James Hetfield" "Dave Mustaine" "Kerry King"]))

(defn bands     []
  (Observable/from ["Metallica" "Megadeth" "Slayer"]))

(defn uppercased-bands []
  (.map (bands) (rx/fn [s] (.toUpperCase s))))

(-> (Observable/zip (musicians)
                    (uppercased-bands)
                    (rx/fn* vector))
    (.subscribe (rx/action [[musician band]]
                           (prn-to-repl (str musician " - from: " band)))))


;;
;; Flatmapping
;;

(defn factorial [n]
  (reduce * (range 1 (inc n))))

(defn all-positive-integers []
  (Observable/interval 1 TimeUnit/MICROSECONDS))

(defn fact-obs [n]
  (Observable/create
   (rx/fn [observer]
     (.onNext observer (factorial n))
     (.onCompleted observer))))

(-> (fact-obs 2)
    (.subscribe (rx/action [fac]
                           (prn-to-repl fac))))

(-> (all-positive-integers)
    (.filter (rx/fn* even?))
    (.flatMap (rx/fn* fact-obs))
    (.take 5)
    (.subscribe (rx/action [fac]
                           (prn-to-repl fac))))

(defn repeat-obs [n]
  (Observable/from ^clojure.lang.PersistentVector
                   (vec (repeat 2 n))))

(-> (repeat-obs 5)
    (.subscribe (rx/action [v]
                           (prn-to-repl v))))

(-> (all-positive-integers)
    (.flatMap (rx/fn* repeat-obs))
    (.take 6)
    (.subscribe (rx/action [fac]
                           (prn-to-repl fac))))
