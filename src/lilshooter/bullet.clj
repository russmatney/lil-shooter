(ns lilshooter.bullet
  (:require [arcadia.core :as a]))


(defn kill [bullet-inst]
  (a/log "killing bullet attempt" bullet-inst)
  (when (and bullet-inst
             (a/obj bullet-inst)
             (not (a/is-queued-for-deletion? bullet-inst)))
    (a/log "killing bullet clj" bullet-inst)
    (a/destroy bullet-inst)))
