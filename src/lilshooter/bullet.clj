(ns lilshooter.bullet
  (:require [arcadia.core :as a]))


(defn kill
  "Expects a weak-ref."
  [bullet-inst]
  (when-let [b (and bullet-inst (.GetRef bullet-inst))]
    (a/log "killing bullet attempt" b)
    (when (and (a/obj b)
               (not (a/is-queued-for-deletion? b)))
      (a/log "killing bullet clj" b)
      (a/destroy b))))
