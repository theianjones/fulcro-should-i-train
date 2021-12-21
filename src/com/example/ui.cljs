(ns com.example.ui
  (:require
   [com.example.mutations :as mut :refer [clear-readiness-form readiness-ident]]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul select option]]
   [com.example.supabase :refer [github-signin github-signout]]
   [com.example.data :refer [readiness-data]]))

(defsc User [_ _]
  {:query [:user/avatar-url :user/email :user/id :user/name :user/username :user/authenticated?]})

(defsc Header [this props {:keys [on-signout user]}]
  {:query [[df/marker-table :load-user]]
   :ident (fn [] [:component/id :header])
   :initial-state {}}
  (let [load-user (get props [df/marker-table :load-progress])]
    (div :.flex.justify-between.items-center
         (div :.text-zinc-800 "Should I Train?")
         (when-not load-user
           (div :.flex.flex-end
                (when (:user/authenticated? user) (div :.flex
                                                       (p :.mr-2 (str "Hello " (or (:user/name user) "from the ui/Root component") "!"))
                                                       (button {:onClick #(on-signout)} "Sign out")))
                (when (not (:user/authenticated? user)) (button {:onClick #(github-signin)} "Sign in with GitHub")))))))

(def ui-header (comp/factory Header))

(defn field [{:keys [label valid? error-message input-class options] :as props}]
  (let [input-props (-> props
                        (assoc :name label)
                        (dissoc :label :valid? :error-message :input-class :options))]
    (div :.ui.field
         (dom/label {:htmlFor label} label)
         (input-class input-props
                      (when options (map #(option {:value (:value %) :key (:value %)} (:label %)) options)))
         (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
                  error-message))))

(def t (-> readiness-data
           :questions
           first))

(defsc ReadinessForm [this {:readiness/keys [trained]}]
  {:query [:readiness/trained fs/form-config-join]
   :ident (fn [] readiness-ident)
   :initial-state (fn [_]
                    (fs/add-form-config ReadinessForm {:readiness/trained ""}))
   :form-fields #{:readiness/trained}
   :componentDidMount (fn [this]
                        (comp/transact! this [(clear-readiness-form nil)]))}
  (div
   (h3 "Readiness Form")
   (field {:input-class   select
           :options       (:options t)
           :label         (:value t)
           :value         (or trained (-> t
                                          :options
                                          first
                                          :score))
           :autoComplete  "off"
           :onChange      #(m/set-integer! this :readiness/trained :event %)})))

(def ui-readiness-form (comp/factory ReadinessForm))

(defsc Root [this {:root/keys [user] :as props}]
  {:query [[df/marker-table :load-progress] :new-thing
           {:root/user (comp/get-query User)}
           [df/marker-table :load-user]
           {:header (comp/get-query Header)}
           {:readiness-form (comp/get-query ReadinessForm)}]
   :initial-state {:root/user {} :header {}}}
  (div
   (ui-header (comp/computed (:header props) {:on-signout #(comp/transact! this [(mut/delete-root-user nil)]) :user user}))
   (ui-readiness-form (:readiness-form props))))
