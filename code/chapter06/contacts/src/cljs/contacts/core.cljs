(ns contacts.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn update-contact! [e contact key]
  (om/update! contact key (.. e -target -value)))

(defn contact-details-form-view [{{:keys [name phone email id] :as contact} :contact
                                  editing :editing-cursor}
                                 owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:style #js {:display (if (get editing 0) "" "none")}}
               (dom/h2 nil "Contact details")
               (if contact
                 (dom/div nil
                          (dom/input #js {:type "text"
                                          :value name
                                          :onChange #(update-contact! % contact :name)})
                          (dom/input #js {:type "text"
                                          :value phone
                                          :onChange #(update-contact! % contact :phone)})
                          (dom/input #js {:type "text"
                                          :value email
                                          :onChange #(update-contact! % contact :email)})
                          (dom/button #js {:onClick #(om/update! editing 0 false)}
                                      "Save"))
               (dom/div nil "No contact selected"))))))


(defn contact-details-view [{{:keys [name phone email id] :as contact} :contact
                             editing :editing-cursor}
                            owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:style #js {:display (if (get editing 0) "none" "")}}
             (dom/h2 nil "Contact details")
             (if contact
               (dom/div nil
                        (dom/h3 #js {:style #js {:margin-bottom "0px"}} (:name contact))
                        (dom/span nil (:phone contact)) (dom/br nil)
                        (dom/span nil (:email contact)) (dom/br nil)
                        (dom/button #js {:onClick #(om/update! editing 0 true)}
                                    "Edit"))
               (dom/span nil "No contact selected"))))))

(defn details-panel-view [data owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:style #js {:float "right"
                                :width "50%"}}
               (om/build contact-details-view data)
               (om/build contact-details-form-view data)))))

(defn select-contact! [contact selected-id-cursor]
  (om/update! selected-id-cursor 0 (:id contact)))

(defn contact-summary-view [{:keys [name phone] :as contact} owner]
  (reify
    om/IRender
    (render [_]
      (dom/li #js {:onClick #(select-contact! @contact
                                              (om/get-shared owner :selected-id-cursor))}
              (dom/span nil name)
              (dom/span nil phone)))))

(defn contacts-view [{:keys [contacts selected-id-cursor]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:style #js {:float "left"
                                :width "50%"}}
               (apply dom/ul nil
                      (om/build-all contact-summary-view (vals contacts)
                                    {:shared {:selected-id-cursor selected-id-cursor}}))))))

(defn contacts-app [data owner]
  (reify
    om/IRender
    (render [_]
      (let [[selected-id :as selected-id-cursor] (:selected-contact-id data)]
        (dom/div nil
                 (om/build contacts-view
                           {:contacts            (:contacts data)
                            :selected-id-cursor selected-id-cursor})
                 (om/build details-panel-view
                           {:contact        (get-in data [:contacts selected-id])
                            :editing-cursor (:editing data)}))))))

(def app-state
  (atom {:contacts {1 {:id    1
                       :name  "James Hetfield"
                       :email "james@metallica.com"
                       :phone "+1 XXX XXX XXX"}
                    2 {:id    2
                       :name  "Adam Darski"
                       :email "the.nergal@behemoth.pl"
                       :phone "+48 XXX XXX XXX"}}
         :selected-contact-id []
         :editing [false]}))

(om/root
  contacts-app
  app-state
  {:target (. js/document (getElementById "app"))})
