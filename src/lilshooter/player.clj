(ns lilshooter.player
  (:require
   [clojure.string]
   [arcadia.core :as a]
   [arcadia.linear :refer [v2*]]
   [arcadia.2D :refer [position! move-and-slide]]
   [lilshooter.bullet :as bullet]
   [util])
  (:import [Godot Vector2 Input]))

(def p (atom nil))
(def speed (atom 500))
(def log? (atom true))

(declare area2D-body-entered)

(declare fire)
(def fire-cooldown? (atom false))
(def fire-debounce 0.2)

(defn start-fire-cooldown []
  (reset! fire-cooldown? true)
  (a/timeout fire-debounce
             (fn [] (reset! fire-cooldown? false))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Hooks
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn ready [player-node key]
  (reset! p player-node)
  (a/log "player ready node: " player-node "key:" key)

  ;; connect callback to player's hitbox
  (a/connect (a/find-node player-node "Area2D") "body_entered" area2D-body-entered))

(defn process [player-node key dt]
  (when @log?
    (reset! p player-node)
    (a/log "player process node: " player-node "key:" key "dt:" dt)
    (reset! log? false))

  (when (and
          (Input/IsActionPressed "ui_action")
          (not @fire-cooldown?))
    (fire player-node)
    (start-fire-cooldown)))

(defn physics-process [player-node _key _dt]
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

    (.LookAt player-node (.GetGlobalMousePosition player-node))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Kill
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill [_player-node]
  ;; (a/destroy player-node)
  ;; quick restart
  (util/reload-scene))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; collision cb
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn area2D-body-entered
  "Called when something enters the player's Area2D"
  [body]
  (when (clojure.string/includes? (.Name body) "enemy")
    (kill @p)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fire
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def bullet-speed (atom 2000))
(def bullets (atom []))
(def max-bullets 3)

(defn kill-bullet [inst]
  (bullet/kill inst))

(defn fire [player-node]
  ;; TODO refactor more of this into the bullet namespace?
  (let [gun-pos            (-> (a/get-node player-node "gun")
                               (.GetGlobalPosition))
        player-rot-degrees (.RotationDegrees player-node)
        player-rotation    (.Rotation player-node)
        bullet-inst        (bullet/inst)
        tree-root          (-> player-node (a/tree) (.GetRoot))]

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
    (swap! bullets
           (fn [bs]
             (let [;; add new bullet to front
                   new-bs               (concat
                                          ;; Storing weak refs b/c these can be freed upon
                                          ;; impact with enemies
                                          [(Godot.Object/WeakRef bullet-inst)]
                                          bs)
                   ;; split via max-bullets
                   [to-keep to-destroy] (split-at max-bullets new-bs)
                   to-keep              (vec to-keep)]

               ;; destroy excess bullets
               (doall (for [b to-destroy] (bullet/kill b)))

               ;; return new list of bullets
               to-keep)))))


(comment
  (fire @p)

  @bullets
  (count @bullets)

  @p

  (Node2D.GetGlobalMousePosition)

  (reset! speed 5500)
  (reset! speed 500)
  (reset! speed 1500)
  (reset! speed 1050))
