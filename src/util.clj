(ns util
  (:require [arcadia.core :as a]))

(defn reload-scene []
  (-> (a/tree)
      (.ReloadCurrentScene)))
