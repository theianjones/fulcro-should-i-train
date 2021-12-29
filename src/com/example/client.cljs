(ns com.example.client
  (:require
   [com.example.app :refer [app]]
   [com.example.ui :as ui]
   [com.example.mutations :as mut]
   [com.fulcrologic.fulcro.application :as app]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]))

(def readiness-quiz-id "375623e0-92dd-4815-b507-4d577a55f37c")

(defn load-readiness-quiz! [app]
  (df/load! app :readiness-quiz ui/Quiz {:target (targeting/replace-at [:quiz])}))

(defn ^:export init
  "Called by shadow-cljs upon initialization, see shadow-cljs.edn"
  []
  (println "Initializing the app...")
  (app/set-root! app ui/Root {:initialize-state? true})
  (dr/initialize! app) ; make ready, if you want to use dynamic routing...
  (df/load! app :current-user ui/User {:target (targeting/replace-at [:root/user])
                                       :marker :load-user
                                       :post-action (fn [{:keys [state]}]
                                                      (let [authenticated? (get-in @state [:root/user :user/authenticated?])]
                                                        (if authenticated?
                                                          (dr/change-route! app ["dashboard"])
                                                          (dr/change-route! app ["login"]))))})
  (app/mount! app
              (app/root-class app)
              "app"
              {:initialize-state? false}))

(defn ^:export refresh
  "Called by shadow-cljs upon hot code reload, see shadow-cljs.edn"
  []
  (println "Refreshing after a hot code reload...")
  (comp/refresh-dynamic-queries! app)
  (app/mount! app (app/root-class app) "app"))
