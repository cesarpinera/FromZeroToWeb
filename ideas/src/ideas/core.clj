(ns ideas.core
  (:use korma.db
        korma.core
        [clj-time.core :exclude [extend]]
        clj-time.coerce
        clj-time.format
        compojure.core
        hiccup.core
        hiccup.form)
  (:require [compojure.route :as route]
             [compojure.handler :as handler]))

(defdb ideasdb (mysql {:db "ideas"
                       :user "ideas"
                       :password "ideas" }))


(defentity idea)

(defn create-idea [text]
  (let [an-idea {:idea text, :timestamp (to-long (now))}]
    (insert idea (values an-idea))
    an-idea))

(defn ideas []
  (select idea (order :timestamp :DESC)))

(defroutes main-routes
  (GET "/" []
    (html
     [:html
      [:head
       [:title "Ideas"]]
      [:body
       [:h1 "New idea"]
       (form-to [:post "/"]
         (label "new-idea" "Did you have an idea?")
         (text-area "new-idea")
         (submit-button "Save it!"))
       [:h1 "All ideas"]
       [:ul
        (map (fn [an-idea]
               (html [:li (str "On "
                               (unparse (formatters :rfc822) (from-long (Long/parseLong (:timestamp an-idea))))
                               " you had this idea: "
                               (:idea an-idea))]))
             (ideas))]]]))

  (POST "/" {params :params}
    (println params)
    (let [new-idea (params :new-idea)]
      (create-idea new-idea))
    {:status 302 
     :headers {"Location" "/"}}) 

  (route/not-found "Page not found"))

(def app
  (handler/site main-routes))