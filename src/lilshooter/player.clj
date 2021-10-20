(ns lilshooter.player
  (:require [arcadia.core :as a]
            [arcadia.linear :refer [v2 v2+ v2*]]
            [arcadia.2D :refer [position! position move-and-slide]])
  (:import [Godot ResourceLoader GD Mathf Vector2 Input Node2D]))

(def p (atom nil))
(def speed (atom 500))
(def log? (atom true))

(def fire-cooldown? (atom false))
(def fire-debounce (atom 0.2))

(declare fire)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hooks
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ready [node key]
  (reset! p node)
  (a/log "player ready node: " node "key:" key))

(defn process [node key dt]
  (when @log?
    (reset! p node)
    (a/log "player process node: " node "key:" key "dt:" dt)
    (reset! log? false)))

(defn physics-process [node key dt]
  (let [x (cond
            (Input/IsActionPressed "ui_right") 1
            (Input/IsActionPressed "ui_left")  -1
            :else                              0)
        y (cond
            (Input/IsActionPressed "ui_up")   -1
            (Input/IsActionPressed "ui_down") 1
            :else                             0)
        v-diff
        (-> (Vector2. x y)
            (.Normalized)
            (v2* @speed))]

    (move-and-slide node v-diff)

    (.LookAt node (.GetGlobalMousePosition node))

    (when (and
            (Input/IsActionPressed "ui_action")
            (not @fire-cooldown?))
      (fire node))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Funcs
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO move to bullet.clj ?
;; does this work outside the repl?
(def bullet-scene (a/load-scene "entities/bullet.tscn"))
(def bullet-speed (atom 2000))
(def bullets (atom []))

(def current-bullet 0)
(def max-bullets 10)

(defn fire [player-node]
  (let [gun-pos            (-> (a/get-node player-node "gun")
                               (.GetGlobalPosition))
        player-rot-degrees (.RotationDegrees player-node)
        player-rotation    (.Rotation player-node)
        bullet-inst        (-> bullet-scene (a/instance))
        tree-root          (-> player-node (a/tree) (.GetRoot))]

    ;; TODO write chainable versions of these functions
    (position! bullet-inst gun-pos)
    (.SetRotationDegrees bullet-inst player-rot-degrees)
    (.ApplyImpulse bullet-inst
                   (new Vector2)
                   (-> (new Vector2 @bullet-speed 0)
                       (.Rotated player-rotation)))

    (.CallDeferred tree-root "add_child" bullet-inst)

    bullet-inst
    ))


(comment

  (fire @p)

  @log?
  @p

  (.QueueFree @p)

  (reset! log? true)

  (let [v-diff (new Vector2)]
    (a/log "v-diff" v-diff)
    )

  (a/log "hi")

  (Input/IsActionPressed "ui_right")

  (Node2D.GetGlobalMousePosition)

  (reset! speed 5500)
  (reset! speed 500)
  (reset! speed 1500)
  (reset! speed 1050))
