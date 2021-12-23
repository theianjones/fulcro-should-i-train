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
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 form h3 input label li ol p ul select option]]
   [com.example.supabase :refer [github-signin github-signout]]
   [com.example.data :refer [readiness-data]]))

(defsc User [_ _]
  {:query [:user/avatar-url :user/email :user/id :user/name :user/username :user/authenticated?]})

(defsc Header [this props {:keys [on-signout user]}]
  {:query [[df/marker-table :load-user]]
   :ident (fn [] [:component/id :header])
   :initial-state {}}
  (let [load-user (get props [df/marker-table :load-progress])]
    (div :.flex.justify-between.items-center.my-4
         (div :.text-grey-100 "Should I Train?")
         (when-not load-user
           (div :.flex.flex-end
                (when (:user/authenticated? user) (div :.flex
                                                       (p :.mr-2.text-grey-100 (str "Hello " (or (:user/name user) "from the ui/Root component") "!"))
                                                       (button :.text-grey-100 {:onClick #(on-signout)} "Sign out")))
                (when (not (:user/authenticated? user)) (button :.text-grey-100 {:onClick #(github-signin)} "Sign in with GitHub")))))))

(def ui-header (comp/factory Header))

(defn field [{:keys [label valid? error-message input-class id] :as props}]
  (let [input-props (-> props
                        (dissoc :label :valid? :error-message :input-class :options))]
    (div
     (dom/label :.relative.block.bg-white.border.rounded-lg.shadow-sm.px-6.py-4.cursor-pointer.sm:flex.sm:justify-between.focus:outline-none.text-gray-700 {:htmlFor label} label)
     (input-class input-props)
     (dom/div {:classes [(when valid? "hidden")]}
              error-message))))

(defsc Question [this {:question/keys [id label byline options]}]
  {:query [:question/id
           :question/label
           :question/byline
           :ui/selected
           {:question/options [:option/label
                               :option/value]}]
   :ident (fn [] [:question/id id])
   :initial-state {}}
  (div :.mb-4.p-4.bg-gray-700.rounded.max-w-fit
       (h2 :.text-xl.text-gray-100 label)
       (p :.text-gray-300.mb-2.italic byline)
       (map (fn [o]
              (field {:input-class  input
                      :name         id
                      :label        (:option/label o)
                      :value        (str (:option/value o))
                      :id           (:option/label o)
                      :key          (:option/value o)
                      :type         "radio"
                      :autoComplete "off"
                      :onChange     #(m/set-integer! this :ui/selected :event %)}))
            options)))

(def ui-question (comp/factory Question {:keyfn :question/id}))

(defsc Quiz [this {:quiz/keys [version id label questions]}]
  {:query [:quiz/version :quiz/id :quiz/label
           {:quiz/questions (comp/get-query Question)}
           fs/form-config-join]
   :ident (fn [] [:quiz/id id])
   :initial-state {:quiz/questions []}}
  (when id
    (div
     (div :.flex.items-center.gap-1.mb3
          (h2 :.text-3xl.text-gray-100.mb-2 label)
          (p :.text-gray-100 "v" version))
     (dom/form
      (map ui-question questions)))))

(def ui-quiz (comp/factory Quiz {:keyfn :quiz/id}))

(defsc Root [this {:root/keys [user] :as props}]
  {:query [[df/marker-table :load-progress] :new-thing
           {:root/user (comp/get-query User)}
           [df/marker-table :load-user]
           {:header (comp/get-query Header)}
           {:quiz (comp/get-query Quiz)}]
   :initial-state {:root/user {} :header {} :quiz {}}}
  (div :.container.mx-auto.text-gray-100
       (ui-header (comp/computed (:header props) {:on-signout #(comp/transact! this [(mut/delete-root-user nil)]) :user user}))
       (when (:quiz props)
         (ui-quiz (:quiz props)))))
