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

(defn field [{:keys [label valid? error-message input-class id] :as props}]
  (let [input-props (-> props
                        (dissoc :label :valid? :error-message :input-class :options))]
    (div :.ui.field
         (dom/label {:htmlFor id} label)
         (input-class input-props)
         (dom/div :.ui.error.message {:classes [(when valid? "hidden")]}
                  error-message))))

(def t (-> readiness-data
           :questions
           first))

#_[{[:quiz/id "375623e0-92dd-4815-b507-4d577a55f37c"]
    [:quiz/version :quiz/id :quiz/label
     {:quiz/questions [:question/id :question/label
                       {:question/options [:option/label :option/value]}]}]}]

(defsc Quiz [this {:quiz/keys [version id label questions]}]
  {:query [:quiz/version :quiz/id :quiz/label
           {:quiz/questions [:question/id
                             :question/label
                             {:question/options [:option/label
                                                 :option/value]}]}
           fs/form-config-join]
   :ident (fn [] [:quiz/id id])
   :initial-state {}}
  (div
   (div :.flex.items-center.gap-1.mb3
        (h2 :.text-lg.text-zinc-800 label)
        (p :.text-gray-500 "v" version))
   (map (fn [q]
          (div {:key  (:question/id q)}
               (h2 (:question/label q))
               (map (fn [o]
                      (field {:input-class dom/input
                              :name (:question/id q)
                              :label (:option/label o)
                              :value (:option/value o)
                              :id (:option/value o)
                              :key (:option/value o)
                              :type "radio"
                              :autoComplete "off"}))
                    (:question/options q))))
        questions)

   #_(field {:input-class   select
             :options       (:options t)
             :label         (:value t)
             :value         (or trained (-> t
                                            :options
                                            first
                                            :score))
             :autoComplete  "off"
             :onChange      #(m/set-integer! this :readiness/trained :event %)})))

(def ui-quiz (comp/factory Quiz {:keyfn :quiz/id}))

(defsc Root [this {:root/keys [user] :as props}]
  {:query [[df/marker-table :load-progress] :new-thing
           {:root/user (comp/get-query User)}
           [df/marker-table :load-user]
           {:header (comp/get-query Header)}
           {:quiz (comp/get-query Quiz)}]
   :initial-state {:root/user {} :header {}}}
  (div :.container.mx-auto.text-zinc-800
       (ui-header (comp/computed (:header props) {:on-signout #(comp/transact! this [(mut/delete-root-user nil)]) :user user}))
       (ui-quiz (:quiz props))))
