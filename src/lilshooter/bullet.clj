(ns lilshooter.bullet
  (:require [arcadia.core :as a]))

(def scene (a/load-scene "entities/bullet.tscn"))

(defn inst [] (a/instance scene))

(defn kill
  "Expects a weak-ref."
  [bullet-inst]
  (when-let [b (and bullet-inst (.GetRef bullet-inst))]
    (when (and (a/obj b)
               (not (a/is-queued-for-deletion? b)))
      (a/destroy b))))
