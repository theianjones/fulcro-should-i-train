(ns com.example.mutations
  "Client-side mutations.

   'Server-side' mutations could normally be also defined here, only with
   `#?(:clj ...)` but here even the 'server' runs in the browser so we must
   define them in another ns, which we do in `...pathom`."
  (:require
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.components :as comp]
   [com.fulcrologic.fulcro.mutations :refer [defmutation]]
   [com.example.supabase :refer [github-signout]]))

(defmutation create-random-thing [{:keys [tmpid]}]
  (action [{:keys [state] :as env}]
          (swap! state assoc-in [:new-thing tmpid] {:id tmpid, :txt "A new thing!"}))
  (remote [_] true))

(defmutation delete-root-user [_]
  (action [{:keys [state] :as env}]
          (github-signout)
          (swap! state assoc :root/user {:user/authenticated? false})))

(def readiness-ident [:component/id :readiness])

(defn readiness-class [] (comp/registry-key->class :com.example.ui/ReadinessForm))

(defn clear-readiness-form*
  "Mutation helper: Updates state map with a cleared readiness form that is configured for form state support."
  [state-map]
  (-> state-map
      (assoc-in readiness-ident
                {:readiness/trained ""})
      (fs/add-form-config* (readiness-class) readiness-ident)))

(defmutation clear-readiness-form [_]
  (action [{:keys [state]}]
          (swap! state clear-readiness-form*)))

;; implement this when we want redo
#_(defmutation use-quiz-form [{:quiz/keys [id]}]
    (action [{:keys [state]}]
            (swap! state (fn [s]
                           (-> s
                               (fs/add-form-config* ui/Quiz [:quiz/id id]))))))
