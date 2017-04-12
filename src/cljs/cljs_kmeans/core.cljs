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


(defn distance [a b]
  (.sqrt 
    js/Math 
    (+ 
      (.pow js/Math  (-  (:x a)  (:x b)) 2)
      (.pow js/Math  (-  (:y a)  (:y b)) 2))))

(defn assign-colors [clusters]
  (map (fn [cluster color] (assoc cluster :color color)) clusters cluster-colors))

(defn assign-to-cluster [points centroids]
  (->> (for [point points]
    (apply min-key :distance 
           (map (fn [centroid] 
                  {:centroid centroid :point point :distance (distance centroid point)}) centroids)))
     (group-by :centroid)
     (map (fn [[centroid groups]] {:centroid centroid :points (map :point groups)}))
     assign-colors))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce debug?
  ^boolean js/goog.DEBUG)

(defonce app-state
  (let [rps (random-points)
        k 6
        centroids (take k rps)]
  (reagent/atom
   {:points rps
    :k k
    ; :clusters [{:centroid  {:x 1 :y 2}},  {:centroid  {:x 2 :y 1}}]
    :clusters (assign-to-cluster rps centroids)})))

(defn circle [x y color size]
  [:circle {:r size :cx x :cy y :fill color}])

(defn draw-points [points]
  (map (fn [{:keys [x y]}] (circle x y "black" 2)) points))

(defn draw-clusters [clusters]
  (for [{:keys [centroid points color]} clusters]
    (cons 
      (circle (:x centroid) (:y centroid) color 4)
      (map (fn [{:keys [x y]}] (circle x y color 2)) points))))

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
