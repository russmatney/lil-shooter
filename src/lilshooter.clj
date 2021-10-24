(ns lilshooter
  (:require [arcadia.core :as a]
            [arcadia.linear :refer [v2 v2+]]
            [arcadia.2D :refer [position! position]])
  (:import [Godot GD Mathf]))

(defn sin [t]
  (Mathf/Sin t))

(defn cos [t]
  (Mathf/Cos t))

(def elapsed (atom 0))
(def base-position (atom (v2 0 0)))

(defn do-update [node key dt]
  (a/log "do-update")
  (a/log "node" node)
  (a/log "key" key)
  (a/log "dt" dt)
  ;; (swap! elapsed #(+ % dt))
  ;; (let [origin  (a/state node :initial-pos)
  ;;       t       (* @elapsed (a/state node :speed))
  ;;       x       (* 267 (* (cos (* 2 t)) (sin t)))
  ;;       y       (* 128 (sin t))
  ;;       new-pos (v2+ (v2 x y) origin)]
  ;;   (position! node new-pos))
  )


(defn ready [node _key]
  (a/log "ready")
  ;; (def --n node)
  ;; (a/log "node" node)
  ;; (a/log "key" key)
  ;; (a/set-state node :initial-pos (position node))
  ;; (a/set-state node :speed (GD/RandRange 0.5 2))
  )

(comment
  (a/log "hi there")
  (a/log --n)
  (a/log (:name --n))

  )
