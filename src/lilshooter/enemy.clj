(ns lilshooter.enemy
  (:require
   [clojure.string]
   [arcadia.core :as a]
   [arcadia.2D :refer [scale! position position!]]
   [arcadia.linear :refer [v2+ v2- v2div]]
   [util])
  (:import [Godot GD Color Vector2 KinematicBody2D]))

(def colors
  [(new Color "be4444") ;; red
   (new Color "be8744") ;; orange
   (new Color "2f8918") ;; green
   (new Color "811889") ;; purple
   (new Color "891846") ;; magenta
   ])

(def p (or p (atom nil)))
(def e (or e (atom nil)))
(def es (or es (atom {})))
(def log? (atom true))

(def initial-state
  {:health      2
   :dying       false
   :player-node nil})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; set color
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO consider a sprite or has-sprite namespace
(defn set-random-color [node]
  (let [sprite (a/find-node node "Sprite")
        idx    (mod (GD/Randi) (count colors))]
    (.SetModulate sprite (get colors idx))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ready
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ready [enemy-node _key]
  (a/log "enemy ready")
  (let [player-node (-> enemy-node a/parent (a/find-node "player"))]
    (reset! p player-node)
    (-> initial-state
        (assoc :player-node player-node)
        (->>
          (a/set-state enemy-node))))

  (reset! e enemy-node)
  (swap! es #(assoc % (.Name enemy-node) enemy-node))
  (set-random-color enemy-node))

(comment
  (-> @e a/parent (a/find-node "player"))
  (util/reload-scene)
  (set-random-color @e)

  (a/state @e)

  (doall
    (->> @es
         vals
         (map set-random-color)))

  @es
  (set-random-color @e))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; process
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn process [enemy-node _key _dt]
  (when @log?
    (a/log "enemy process")
    (reset! e enemy-node)
    (reset! log? false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; physics-process
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare move)
(defn physics-process [enemy-node _key _dt]
  (if (a/state enemy-node :dying)
    (scale! enemy-node (new Vector2 1.5 1.5))
    (move enemy-node)))

(comment
  (scale! @e (new Vector2 12.03 12.04)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; physics-process
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^Vector2 move-and-collide
  "Calls the `.MoveAndCollide` method on a `KinematicBody2D`.
   This function exists because `(.MoveAndCollide ...)` requires
   that all C# optional parameters are provided."
  [^KinematicBody2D o
   ^Vector2 v
   & {:keys [infinite-inertia?
             exclude-raycast-shapes?
             test-only?]
      :or   {infinite-inertia?       true
             exclude-raycast-shapes? true
             test-only?              false}}]
  (.MoveAndCollide o
                   v
                   infinite-inertia?
                   exclude-raycast-shapes?
                   test-only?))

(defn move
  "These little buggers squeeze through walls and sometimes
  jump them entirely with this impl - especially if the step
  factor decreases."
  [e]
  ;; TODO check for valid player-node?
  ;; TODO refactor player-node into a 'target'
  ;; consider a line of sight something
  (let [step-factor           50
        {:keys [player-node]} (a/state e)
        curr-pos              (position e)
        diff                  (v2- (position player-node) curr-pos)
        diff-step             (v2div diff step-factor)
        new-pos               (v2+ curr-pos diff-step)]
    (position! e new-pos)

    (.LookAt e (position player-node))

    (move-and-collide e (Vector2.))))

(comment
  (move @e)

  (a/state @e)
  (util/reload-scene)

  @es
  (->> @es
       vals
       (map (fn [e]
              (let [{:keys [player-node]} (a/state e)
                    curr-pos              (position e)
                    diff                  (v2- (position player-node) curr-pos)
                    diff-step             (v2div diff 20)
                    new-pos               (v2+ curr-pos diff-step)]
                (position! e new-pos)
                (.LookAt e (position player-node))

                (move-and-collide e (Vector2.)))))))
