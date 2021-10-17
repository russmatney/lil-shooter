(ns lilshooter.player
  (:require [arcadia.core :as a]
            [arcadia.linear :refer [v2 v2+ v2*]]
            [arcadia.2D :refer [position! position move-and-slide]]
            )
  (:import [Godot GD Mathf Vector2 Input Node2D]))

(def p (atom nil))
(def speed (atom 500))
(def log? (atom true))

(defn ready [node key]
  (reset! p node)
  (a/log "player ready node: " node "key:" key)
  )

(defn process [node key dt]
  (when @log?
    (a/log "player process node: " node "key:" key "dt:" dt)
    (reset! log? false)
    )
  )

(comment
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
  (reset! speed 1050)
  )


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

    )
  )
