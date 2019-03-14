(ns ^:figwheel-hooks fontard.core
  (:require
   [cljs.reader :refer [read-string]]
   [clojure.string :as str]
   [reagent.core :as reagent :refer [atom]]))

;; -----
;; fn's
(defn prepare-input
  "Prepares input (that could be in *.c format) for clojure string parsing
  replaces tabs with 0x00 (empty column)"
  [s]
  (let [block-start (str/index-of s "{")
        block-end   (str/index-of s "}" block-start)
        s           (if (and block-start block-end)
                      (subs s (inc block-start) block-end)
                      s)]
    (-> s
        (#(str/replace % #"/" ";"))
        (#(str/replace % #"\t" "0x00, ")))))

(defn str->int
  ([s] (str->int s 10))
  ([s base]
   (js/parseInt s base)))

(defn int->str
  ([i] (int->str i 10))
  ([i base]
   (.toString i base)))

(defn add-leading-zeros [s len]
  (str (apply str (repeat (- len (count s)) "0")) s))

(defn get-matrix [input]
  (->> (str/split input #",")
       (map read-string)
       (map #(int->str % 2))
       (map #(add-leading-zeros % 8))
       (map reverse)
       (mapv (partial apply str))))

;; -----
;; initial app-state
(def initial-input "0x20, 0x12, 0x0A, 0x06, 0x1E,")
(defonce app-state (atom {:title "Fontard"
                          :input initial-input
                          :matrix (get-matrix initial-input)}))

;; -------------------------
;; Views

(defn input []
  [:div
   [:textarea
    {:value (:input @app-state)
     :on-change (fn [e] (swap! app-state assoc :input (.. e -target -value)))
     :rows 15
     :cols 80}]
   [:br]
   [:button
    {:on-click (fn [_] (swap! app-state assoc :matrix (get-matrix (prepare-input (:input @app-state)))))}
    "draw"]])

(defn board []
  (let [box-width      10
        matrix         (:matrix @app-state)
        [cols rows]    [(count matrix) (count (first matrix))]
        [width height] [(* cols box-width) (* rows box-width)]]
    [:svg {:width width :height height}
     (for [y    (range rows)
           x    (range cols)
           :let [led-on? (= (get-in matrix [x y]) "1")]]
       [:rect {:key    (str x y)
               :width  box-width
               :height box-width
               :x      (* x box-width)
               :y      (* y box-width)
               :style  {:fill         (if led-on? "red" "black")
                        :stroke       "gray"
                        :stroke-width 0.6}}])]))

(defn main-page []
  [:div
   #_[:pre (pr-str @app-state)]
   [input]
   [board]])


(defn get-app-element []
  (js/document.getElementById "app"))

(defn mount [el]
  (reagent/render-component [main-page] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
