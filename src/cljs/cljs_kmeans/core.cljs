(ns cljs-kmeans.core
  (:require
   [reagent.core :as reagent]
   [devtools.core :as devtools]
   ))


(def cluster-colors ["red",  "range",  "yellow",  "green",  "purple",  "brown"])

(defn random-point []
  {:x (rand-int 1000) :y (rand-int 500)})

(defn random-points []
  (take 200 (repeatedly random-point)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce debug?
  ^boolean js/goog.DEBUG)

(defonce app-state
  (let [rps (random-points)
        k 6]
  (reagent/atom
   {:points rps
    :k k
    ; :clusters [{:centroid  {:x 1 :y 2}},  {:centroid  {:x 2 :y 1}}]
    :clusters (map (fn [point] {:centroid point}) (take k rps))})))

(defn circle [x y color]
  [:circle {:r 2 :cx x :cy y :fill color}])

(defn draw-points [points]
  (map (fn [{:keys [x y]}] (circle x y "blue")) points))

(defn draw-clusters [clusters]
  (let [centroids (map :centroid clusters)]
    (map (fn [{:keys [x y]} color] (circle x y color)) centroids cluster-colors)))

(defn graph [ratom]
  [:svg {:x 0 :y 0 :width 1000 :height 500}
   ; (draw-points (:points @ratom))
   (draw-clusters (:clusters @ratom))
   ])

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
