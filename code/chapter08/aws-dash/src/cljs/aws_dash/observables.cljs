(ns aws-dash.observables
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cognitect.transit :as t]))


(def r (t/reader :json))

(def  aws-endpoint "http://localhost:3001")
(defn aws-uri [path]
  (str aws-endpoint path))


(defn observable-seq [uri transform]
  (.create js/Rx.Observable
           (fn [observer]
             (go (let [response      (<! (http/get uri {:with-credentials? false}))
                       data          (t/read r (:body response))
                       transformed   (transform data)]
                   (doseq [x transformed]
                     (.onNext observer x))
                   (.onCompleted observer)))
             (fn [] (.log js/console "Disposed")))))

(defn describe-stacks []
  (observable-seq (aws-uri "/cloudFormation/describeStacks")
                  (fn [data]
                    (map (fn [stack] {:stack-id   (stack "StackId")
                                     :stack-name (stack "StackName")})
                         (data "Stacks")))))


(defn describe-stack-resources [stack-name]
  (observable-seq (aws-uri "/cloudFormation/describeStackResources")
                  (fn [data]
                    (map (fn [resource]
                           {:resource-id (resource "PhysicalResourceId")
                            :resource-type (resource "ResourceType")} )
                         (data "StackResources")))))

(defn describe-instances [instance-ids]
  (observable-seq (aws-uri "/ec2/describeInstances")
                  (fn [data]
                    (let [instances (mapcat (fn [reservation] (reservation "Instances")) (data "Reservations"))]
                      (map (fn [instance]
                             {:instance-id  (instance "InstanceId")
                              :type        "EC2"
                              :status      (get-in instance ["State" "Name"])})
                           instances)))))

(defn describe-db-instances [instance-id]
  (observable-seq (aws-uri (str "/rds/describeDBInstances/" instance-id))
                  (fn [data]
                    (map (fn [instance]
                           {:instance-id (instance "DBInstanceIdentifier")
                            :type        "RDS"
                            :status      (instance "DBInstanceStatus")})
                         (data "DBInstances")))))

(defn stack-resources []
  (-> (describe-stacks)
      (.map #(:stack-name %))
      (.flatMap describe-stack-resources)))

(defn ec2-instance-status [resources]
  (-> resources
      (.filter #(= (:resource-type %) "AWS::EC2::Instance"))
      (.map #(:resource-id %))
      (.reduce conj [])
      (.flatMap describe-instances)))

(defn rds-instance-status [resources]
  (-> resources
      (.filter #(= (:resource-type %) "AWS::RDS::DBInstance"))
      (.map #(:resource-id %))
      (.flatMap describe-db-instances)))
