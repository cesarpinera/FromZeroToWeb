# From zero to web app in Clojure
Cesar Pinera
cesar.pinera at gmail.com

## Objective
We will install a working development environment for Clojure, and then we'll build a web app that will query a MySQL database.

## What is Clojure
Clojure is a modern Lisp dialect that runs on the Java Virtual Machine. 

 - Code is data. Data is code. 
 - Lisp is a programming language designed to be extended in a very elegant way.
 - Build into the language. Don't be limited by what the language can express out of the box. You own it.
 - Clojure has many exciting concepts, borrowing and building upon some very interesting ideas. You don't have to use all of them at the same time. Take what you need.
 - It opens your mind to many different possibilities.  
 - This is not a tutorial on Lisp
 - Java interop is a Good Thing
 - Dynamic and intelligent community
 
## Installation 
### Basics
 1. A Java SDK, 6.0+
 1. Leinengen 
 1. An editor. I use Emacs. 

### Leiningen
 - https://github.com/technomancy/leiningen
 - "Leiningen is for automating Clojure projects without setting your hair on fire." 
 - Leiningen is pronounced LINE-ing-en

Use it to create your project structure, declare your dependencies, and have them downloaded automatically.

 1. Download the script 
 1. Make it executable
 1. Run it for the fist time
**Hint** Make sure you've set your proxy environment variables properly. 
 
### Emacs
 - You don't actually need an IDE for Clojure, but it really helps when you have one. 
 - Emacs and Lisp = Love. 

**clojure-mode** is Emacs mode for Clojure. http://github.com/technomancy/clojure-mode

Download clojure-mode.el to ~/.emacs.d/

Add it to your .emacs init file

    (add-to-list 'load-path "~/.emacs.d/")  
	(require 'clojure-mode)

### Create your first project
A very basic web application that will allow us to enter ideas (one line of text each) and will display them as a list. 

Create the new project  

    $ lein new ideas

Set up the *REPL* (Read-Eval-Print-Loop) for use within Emacs  

    project.clj --  
	
	(defproject ideas "1.0.0-SNAPSHOT"  
	:description "Ideas web app"  
	:dependencies [[org.clojure/clojure "1.3.0"]]  
	:plugins [[lein-swank "1.4.4"]])`
	
Open *project.clj* in Emacs. M-x clojure-jack-in  

    ; SLIME 20100404  
    user>

Try the REPL!

### The ideas database
We'll use a MySQL database with just one table: idea. 

The table will have two columns: idea and timestamp.  

    create database ideas;  
	use ideas;  
	create table idea (idea TEXT, timestamp BIGINT);  
	create user ideas@'localhost' IDENTIFIED BY 'ideas';  
	grant all privileges on ideas.* to ideas@'localhost';  

### Database dependencies
**Korma** is a *Domain Specific Language* for Clojure to use relational databases. http://sqlkorma.com/

Korma is made of pure, pure love.

We'll also add the MySQL connector for Java. 

In *project.clj*  

    (defproject ideas "1.0.0-SNAPSHOT"  
      :description "Ideas web app"  
      :dependencies [[org.clojure/clojure "1.3.0"]  
                             [korma "0.3.0-beta7"]  
                             [mysql/mysql-connector-java "5.1.6"]]  
      :plugins [[lein-swank "1.4.4"]])

Run lein deps

### Define the entity model
In src/ideas/core.clj

    (ns ideas.core
    (:use korma.db
            korma.core))
    
    (defdb ideasdb (mysql {:db "ideas"
	                              :user "ideas"
                                  :password "ideas" }))

Try it in the REPL. Compile/Load File ^C-^K

    user> (in-ns 'ideas.core)
    #<Namespace ideas.core>
    ideas.core> ideasdb
    {:pool #<Delay@6d4a4067: :pending>, :options {:naming {:keys #<core$identity clojure.core$identity@9f8297b>, :fields #<core$identity clojure.core$identity@9f8297b>}, :delimiters [\` \`]}}

Add the entity

    (defentity idea)
	
Verify it in the REPL

   ideas.core> (select idea)
   []

Insert our first idea ever

    ideas.core> (insert idea (values {:idea "This is my first idea!" :timestamp 1}))
    nil
    ideas.core> (select idea)
    [{:idea "This is my first idea!", :timestamp 1}]

### Add a time framework
Time is always a pain. Fortunately there's a Clojure wrapper for Joda time, clj-time https://github.com/seancorfield/clj-time

Add this dependency to project.clj

    [clj-time "0.4.2"]
	
Add it to the namespace

    [clj-time.core :exclude [extend]]
    clj-time.coerce
	
Let's try to create a time instance for the current time and turn it into a numeric representation.

    ideas.core> (now)
    #<DateTime 2012-06-01T05:08:42.895Z>
    ideas.core> (to-long (now))
    1338527327822
	
### Create the function that will create an idea and will store it in a database

	(defn create-idea [text]
	  (let [an-idea {:idea text, :timestamp (to-long (now))}]
		(insert idea (values an-idea))
		an-idea))

Compile and try it:

	ideas.core> (create-idea "This is my very second idea")
	{:idea "This is my very second idea", :timestamp 1338528020795}
	
### Create the function that will retrieve all the ideas from the database

	(defn ideas []
	  (select idea (order :timestamp :DESC)))

### The web framework
**Compojure** is a small web framework for Clojure. https://github.com/weavejester/compojure/wiki

It is based on **Ring**, which abstracts the HTTP interface. https://github.com/ring-clojure/ring

**hiccup** will allow us to output HTML in a Clojure idiomatic way. Very neat. https://github.com/weavejester/hiccup

Add compojure and ring to project.clj.

	(defproject ideas "1.0.0-SNAPSHOT"
	  :description "Ideas web app"
	  :dependencies [[org.clojure/clojure "1.3.0"]
					 [korma "0.3.0-beta7"]  
					 [mysql/mysql-connector-java "5.1.6"]
					 [clj-time "0.4.2"]
					 [compojure "1.1.0"]
					 [hiccup "1.0.0"]]
	  :plugins [[lein-swank "1.4.4"]
				[lein-ring "0.7.1"]]
	  :ring {:handler ideas.core/app})
	  
	  
Add compojure and hiccup to the namespace. Also add clj-time.format to convert from long to DateTime

	(ns ideas.core
	  (:use korma.db
			korma.core
			[clj-time.core :exclude [extend]]
			clj-time.coerce
			clj-time.format
			compojure.core
			hiccup.core)
	  (:require [compojure.route :as route]
				 [compojure.handler :as handler]))

Define our first routes

	(defroutes main-routes
	  (GET "/" []
		(html
		 [:html
		  [:head
		   [:title "Ideas"]]
		  [:body
		   [:h1 "All ideas"]
		   [:ul
			(map (fn [an-idea]
				   (html [:li (str "On "
								   (unparse (formatters :rfc822) (from-long :timestamp an-idea)))
								   " you had this idea: "
								   (:idea an-idea))]))
				 (ideas))]]]))

	  (route/not-found "Page not found"))

Add the app that will be served by ring

	(def app
	  (handler/site main-routes))
	  
Run it

    $ lein ring server
	
### Add a form and process the POST
Add hiccup.form to the namespace

	(:use korma.db
			korma.core
			[clj-time.core :exclude [extend]]
			clj-time.coerce
			clj-time.format
			compojure.core
			hiccup.core
			hiccup.form)
		
Create the form in the HTML body

	       [:h1 "New idea"]
		   (form-to [:post "/"]
			 (label "new-idea" "Did you have an idea?")
			 (text-area "new-idea")
			 (submit-button "Save it!"))

Add a route to the POST /

	  (POST "/" {params :params}
		(println params)
		(let [new-idea (params :new-idea)]
		  (create-idea new-idea))
		{:status 302 
		 :headers {"Location" "/"}}) 

### Final words
This session is only intended to help you get a dev environment working, in order to get the mundane out of the way. Now the real fun begins: **learn yourself a Lisp for Greater Good!**

Recommended reading:

