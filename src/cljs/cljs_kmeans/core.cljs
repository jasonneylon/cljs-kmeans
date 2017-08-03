(ns cljs-kmeans.core
  (:use-macros [cljs.core.async.macros :only [go]])
  (:require
    [reagent.core :as reagent]
    [devtools.core :as devtools]
    [cljs.core.async :refer [<! chan close!]]))

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

(defn mean
  [numbers]
  (/  (apply + numbers)  (count numbers)))

(defn mean-point
  [points]
  {:x (mean (map :x points)) :y (mean (map :y points))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Vars

(defonce debug?
  ^boolean js/goog.DEBUG)

(defn initial-app-state
  []
  (let [rps (random-points)
        k 6
        centroids (take k rps)]
    {:points rps
     :k k
     :running false
     :iteration 0
     :clusters (assign-to-cluster rps centroids)}))

(defonce app-state
 (reagent/atom (initial-app-state)))

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
   (draw-clusters (:clusters @ratom))])


(defn update-kmean 
  [m]
  (update 
    m :clusters 
    (fn [clusters]
      (map #(assoc % :centroid (mean-point (:points %))) clusters))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Page

(defn timeout [ms]
  (let  [c  (chan)]
    (js/setTimeout  (fn  []  (close! c)) ms)
    c))

(defn run-kmean
  []
  (go 
    (dotimes [iteration 10] 
      (<! (timeout 1000))
      (swap! app-state assoc :iteration (inc iteration))
      (swap! app-state update-kmean)
      (swap! app-state #(assoc % :clusters (assign-to-cluster (:points %) (map :centroid (:clusters %))))))))

(defn reset-app-state 
  []
  (reset! app-state (initial-app-state)))

(defn update-k 
  [event]
  (swap! app-state assoc :k (-> event .-target .-value)))

(defn page [ratom]
  [:div 
   [:h1 "K-means in ClojureScript"] (graph ratom)
   [:fieldset 
    [:label {:for "kvalue"} "K value: "]
    [:input {:type "number" :id "kvalue" :value (:k @ratom) :on-change update-k}]
    [:button {:on-click run-kmean} "Run kmeans"]
    [:button {:on-click reset-app-state} "Reset"]
    [:span (str " Iteration: " (:iteration @ratom))]]] )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Initialize App

(defn dev-setup []
  (when debug?
    (enable-console-print!)
    (println "dev mode")
    (devtools/install!)))

(defn reload []
  (reagent/render [page app-state]
                  (.getElementById js/document "app")))

(defn ^:export main []
  (dev-setup)
  (reload))
