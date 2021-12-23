(ns com.example.mutations
  "Client-side mutations.

   'Server-side' mutations could normally be also defined here, only with
   `#?(:clj ...)` but here even the 'server' runs in the browser so we must
   define them in another ns, which we do in `...pathom`."
  (:require
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as ns]
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

;; question_text -> :question/label
;; question_id -> :question/id
;; score -> :option/value
;; answer_text -> :option/label
;; user_id -> :user/id
;; [:response/id temp/id]
;;  [:answer/ids]
;; [:answer/id temp/id]
;;   :question/id
;;   :answer/score
;;   :user/id

(defn submit-form* [state quiz-id]
  (let [user (:root/user state)
        quiz (get-in state [:quiz/id quiz-id])
        questions (map (fn [q] (get-in state q)) (:quiz/questions quiz))
        response-id (tempid/tempid)
        question-scores (map (fn [q] {(:question/id q) (:ui/selected q)}) questions)
        total-score (* 4 (reduce + (mapcat vals question-scores)))
        answers (reduce (fn [acc q]
                          (let [id (tempid/tempid)
                                result {:answer/id id
                                        :answer/score (:ui/selected q)
                                        :question/id (:question/id q)
                                        :user/id (:user/id user)}]
                            (merge acc {id result})))  {} questions)
        response {response-id {:response/id response-id
                               :total total-score
                               :answer/id (map (fn [a] [:answer/id (key a)]) answers)}}]
    (merge state {:answer/id answers} {:response/id response})))

(comment
  (def app (com.fulcrologic.fulcro.application/current-state com.example.app/app))
  (submit-form* app "375623e0-92dd-4815-b507-4d577a55f37c"))

(defmutation submit-form! [{:quiz/keys [id]}]
  (action [{:keys [state] :as env}]
          (tap> env)
          (swap! state #(submit-form* % id))))

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
