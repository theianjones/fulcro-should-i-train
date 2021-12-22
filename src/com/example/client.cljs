(ns com.example.client
  (:require
   [com.example.app :refer [app]]
   [com.example.ui :as ui]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(defn load-user! [app]
  (df/load! app :current-user ui/User {:target (targeting/replace-at [:root/user])
                                       :marker :load-user}))

(defn load-readiness-quiz! [app]
  (df/load! app :readiness-quiz ui/Quiz {:target (targeting/replace-at [:quiz])}))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  (println "Initializing the app...")
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app) ; make ready, if you want to use dynamic routing...
  (app/mount! app
              (app/root-class app)
              "app"
              {:initialize-state? false})
  (load-user! app)
  (load-readiness-quiz! app))

(defn ^:export refresh
  "Called by shadow-cljs upon hot code reload, see shadow-cljs.edn"
  []
  (println "Refreshing after a hot code reload...")
  (comp/refresh-dynamic-queries! app)
  (app/mount! app (app/root-class app) "app")
  (load-user! app)
  (load-readiness-quiz! app))
