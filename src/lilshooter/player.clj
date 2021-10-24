(ns lilshooter.player
  (:require [arcadia.core :as a]
            [arcadia.linear :refer [v2 v2+ v2*]]
            [arcadia.2D :refer [position! position move-and-slide]]
            [lilshooter.bullet :as bullet])
  (:import [Godot ResourceLoader GD Mathf Vector2 Input Node2D]))

(def p (atom nil))
(def speed (atom 500))
(def log? (atom true))

(declare fire)
(def fire-cooldown? (atom false))

;; TODO move to some shared util
(defn reload-scene []
  (-> (a/tree)
      (.ReloadCurrentScene)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hooks
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ready [player-node key]
  (reset! p player-node)
  (a/log "player ready node: " player-node "key:" key))

(defn process [player-node key dt]
  (when @log?
    (reset! p player-node)
    (a/log "player process node: " player-node "key:" key "dt:" dt)
    (reset! log? false))

  (when (and
          (Input/IsActionPressed "ui_action")
          (not @fire-cooldown?))
    (fire player-node))

  ;; (a/log "player process")
  )

(defn physics-process [player-node key dt]
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

    (move-and-slide player-node v-diff)

    (.LookAt player-node (.GetGlobalMousePosition player-node))

    ;; (a/log "player physics-process")
    ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Kill
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill [_player-node]
  ;; (a/destroy player-node)

  (reload-scene)
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fire
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; TODO move to bullet.clj ?
;; does this work outside the repl?
(def bullet-scene (a/load-scene "entities/bullet.tscn"))
(def bullet-speed (atom 2000))
(def bullets (atom []))

(def max-bullets 3)

(def fire-debounce 0.2)
(def bullet-kill-time 1)

(defn reset-fire-debounce []
  (a/log "fire-cooldown release")
  (reset! fire-cooldown? false)
  (a/log "finished resetting fire-cooldown"))

(defn kill-bullet [inst]
  (bullet/kill inst))

(defn fire [player-node]
  (reset! fire-cooldown? true)
  (a/log "\nfire")
  (let [gun-pos            (-> (a/get-node player-node "gun")
                               (.GetGlobalPosition))
        player-rot-degrees (.RotationDegrees player-node)
        player-rotation    (.Rotation player-node)
        bullet-inst        (-> bullet-scene (a/instance))
        tree-root          (-> player-node (a/tree) (.GetRoot))]
    (a/log "bullets" (count @bullets) (seq @bullets))
    (a/log "new-b inst" bullet-inst)

    ;; TODO write chainable versions of these functions
    (position! bullet-inst gun-pos)
    (.SetRotationDegrees bullet-inst player-rot-degrees)
    (.ApplyImpulse bullet-inst
                   (new Vector2)
                   (-> (new Vector2 @bullet-speed 0)
                       (.Rotated player-rotation)))

    ;; TODO create a/add-child to deferred variant
    ;; (a/add-child tree-root bullet-inst)
    (.CallDeferred tree-root "add_child" bullet-inst)

    ;; update bullets to the n most recent
    ;; destroy any excess bullets
    ;; (swap! bullets
    ;;        (fn [bs]
    ;;          (a/log "updating bullets" bs)
    ;;          (let [;; add new bullet to front
    ;;                new-bs               (concat [bullet-inst] bs)
    ;;                _                    (a/log "new-bs" new-bs)
    ;;                ;; split via max-bullets
    ;;                [to-keep to-destroy] (split-at max-bullets new-bs)
    ;;                to-keep              (vec to-keep)]

    ;;            (a/log "to-destroy" (seq to-destroy))

    ;;            ;; destroy excess bullets
    ;;            (doall
    ;;              (for [b to-destroy] (bullet/kill b)))

    ;;            (a/log "to-keep" to-keep)

    ;;            ;; return new list of bullets
    ;;            to-keep)))

    (a/timeout fire-debounce reset-fire-debounce)
    (a/log "queued kill for bullet" bullet-inst)
    (a/timeout bullet-kill-time
               (fn [] (kill-bullet (Godot.Object/WeakRef bullet-inst))))


    ;; (a/connect (.CreateTimer
    ;;              tree-root
    ;;              bullet-kill-time)
    ;;            "timeout"
    ;;            (fn []
    ;;              (a/log "bullet-kill timer up")
    ;;              ;; (when bullet-inst
    ;;              ;;   (a/log "should kill bullet inst" bullet-inst))
    ;;              ;; (kill-bullet bullet-inst)
    ;;              ))

    (a/log "fire finished")
    ))



(comment
  ;; (reset! fire-debounce 0.5)
  ;; (reset! fire-debounce 0.05)
  (.CreateTimer
    (a/tree)
    bullet-kill-time)

  (fire @p)

  (reload-scene)

  (cons "yo" [])
  (cons "yo" ["hi" "bye" "guy"])
  (let [[keep kill] (split-at 2 ["hi" "bye" "guy" "dude" "w/e"])]
    kill
    )
  (let [[keep kill] (split-at 2 ["hi"])]
    kill
    )

  @bullets
  (count @bullets)

  (get @bullets 1)

  @p

  (reset! log? true)
  (Node2D.GetGlobalMousePosition)

  (reset! speed 5500)
  (reset! speed 500)
  (reset! speed 1500)
  (reset! speed 1050))
