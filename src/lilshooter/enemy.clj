(ns lilshooter.enemy
  (:require
   [clojure.string]
   [arcadia.core :as a]
   [arcadia.2D :refer [scale! position position!]]
   [arcadia.linear :refer [v2- v2div]]
   [util])
  (:import [Godot GD Color Vector2 Transform2D KinematicBody2D]))

(def colors
  [(new Color "be4444") ;; red
   (new Color "be8744") ;; orange
   (new Color "2f8918") ;; green
   (new Color "811889") ;; purple
   (new Color "891846") ;; magenta
   ])

(def p (atom nil))
(def e (atom nil))
(def es (atom {}))
(def log? (atom true))

(def initial-state
  {:health 2
   :dying  false
   })

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
  (a/set-state enemy-node initial-state)
  (a/log "enemy ready")
  (reset! e enemy-node)
  (swap! es #(assoc % (.Name enemy-node) enemy-node))
  (set-random-color enemy-node)
  (when-let [player (-> enemy-node a/parent (a/find-node "player"))]
    (reset! p player)))

(comment
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
  (scale! @e (new Vector2 12.03 12.04))
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; physics-process
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ^Vector2 move-and-collide
  "Calls the `.MoveAndCollide` method on a `KinematicBody2D`.
   This function exists because `(.MoveAndCollide ...)` requires
   that all C# optional parameters are provided."
  [^KinematicBody2D o
   ^Vector2 v
   & {:keys [p-from
             p-motion
             p-margin]
      :or   {p-from   (Transform2D.)
             p-motion (Vector2.)
             p-margin 0.08}}]
  (.MoveAndCollide o
                   v
                   p-from
                   p-motion
                   p-margin))


(defn move [enemy-node]
  (let [curr-pos (position enemy-node)
        new-pos  (v2div (v2- (position @p) curr-pos) 50)]
    (position! enemy-node new-pos)

    (.LookAt enemy-node (position @p))

    (move-and-collide enemy-node (Vector2.))
    ))

(comment
  (move @e)
  )
