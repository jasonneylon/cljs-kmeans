(ns cljs-kmeans.core
  (:require
   [reagent.core :as reagent]
   [devtools.core :as devtools]
   ))


(defn random-point []
  {:x (rand-int 1000) :y (rand-int 500)})

(defn random-points []
  (take 200 (repeatedly random-point)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce debug?
  ^boolean js/goog.DEBUG)

(defonce app-state
  (reagent/atom
   {:text "Hello, what is your name? "
    :points (random-points)}))

(defn circle [x y color]
  [:circle {:r 2 :cx x :cy y :fill color}])

(defn graph [ratom]
  [:svg {:x 0 :y 0 :width 1000 :height 500}
   (map (fn [{:keys [x y]}] (circle x y "blue")) (:points @ratom))])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn page [ratom]
  [:div [:h1 "K-means"] (graph ratom)] )



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")
    (devtools/install!)
    ))

(defn reload []
  (reagent/render [page app-state]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (reload))
