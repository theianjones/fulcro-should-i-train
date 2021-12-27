(ns com.example.ui
  (:require
   [com.example.mutations :as mut :refer [clear-readiness-form readiness-ident]]
   [com.fulcrologic.fulcro.algorithms.form-state :as fs]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
   [com.fulcrologic.fulcro.ui-state-machines :as uism]
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
    (div :.flex.justify-between.items-center.my-4.mx-4.lg:mx-0
         (div :.text-grey-100 "Should I Train?")
         (when-not load-user
           (div :.flex.flex-end
                (when (:user/authenticated? user)
                  (div :.flex
                       (p :.mr-2.text-grey-100 (str "Hello " (or (:user/name user) "from the ui/Root component") "!"))
                       (button :.text-grey-100 {:onClick #((on-signout)
                                                           (dr/change-route! this ["login"]))} "Sign out")))
                (when (not (:user/authenticated? user))
                  (button :.text-grey-100 {:onClick #(github-signin)} "Sign in with GitHub")))))))

(def ui-header (comp/factory Header))

(defn field [{:keys [label valid? error-message input-class id] :as props}]
  (let [input-props (-> props
                        (dissoc :label :valid? :error-message :input-class :options))]
    (div
     (dom/label :.relative.block.bg-white.border.rounded-lg.shadow-sm.px-6.py-4.cursor-pointer.sm:flex.sm:justify-between.focus:outline-none.text-gray-700
                {:htmlFor label}
                label)
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
   :initial-state {}
   :route-segment ["quiz" :quiz-id]
   :will-enter (fn [app {:keys [quiz-id]}]
                 (dr/route-deferred [:quiz/id quiz-id]
                                    #(df/load! app [:quiz/id quiz-id] Quiz {:post-mutation `dr/target-ready
                                                                            :post-mutation-params {:target [:quiz/id quiz-id]}})))}
  (when id
    (div
     (div :.flex.items-center.gap-1.mb3
          (h2 :.text-3xl.text-gray-100.mb-2 label)
          (p :.text-gray-100 "v" version))
     (dom/form
      (map ui-question questions)
      (button :.text-grey-100.bg-gray-700.p-3.rounded.hover:bg-gray-900
              {:onClick (fn [evt]
                          (.preventDefault evt)
                          (comp/transact! this [(mut/create-readiness-response {:quiz/id id})]))} "Submit")))))

(def ui-quiz (comp/factory Quiz {:keyfn :quiz/id}))

(defsc Login [_ _]
  {:route-segment ["login"]
   :query []
   :ident (fn [] [:component/id :login])}
  (div :.grid.md:grid-cols-2.grid-cols-1
       (div :.md:grid-span-1.md:min-h-screen.w-full.py-8.bg-gray-600.rounded.mb-16
            (div :.flex.flex-col.place-content-center.h-full.mb-8
                 (h1 :.md:mb-24.mb-12.self-center.w-96.text-3xl
                     "Track how you feel so you know when to train.")
                 (button :.self-center.text-grey-100.font-bold.bg-blue-600.py-3.px-8.rounded-md.border.border-transparent.hover:bg-indigo-700.h-12
                         {:onClick #(github-signin)}
                         "Sign in with GitHub")))
       (div :.md:grid-span-1.md:m-auto.mx-4
            (div :.flex.flex-col.text-grey-100.max-w-lg.h-full.place-content-center
                 (h2 :.text-2xl.font-medium.mb-8.px-2
                     "Your body is giving you the signals you need...")
                 (p :.max-w-prose.mb-5.px-4
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")
                 (p :.max-w-prose.mb-5.px-4
                    "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.")
                 (p :.max-w-prose.mb-5.px-4
                    "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.")
                 (h2 :.text-2xl.font-medium.mb-8.px-2
                     "No fancy gear required, just answer these questions")
                 (p :.max-w-prose.mb-5.px-4
                    "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.")
                 (p :.max-w-prose.mb-5.px-4
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.")))))

(dr/defrouter TopRouter [this props]
  {:router-targets [Login Quiz]})

(def ui-top-router (comp/factory TopRouter))

(defsc Main [this {:main/keys [header router user] :as props}]
  {:query [{:main/router (comp/get-query TopRouter)}
           {:main/header (comp/get-query Header)}
           {:main/user   (comp/get-query User)}
           [::uism/asm-id ::TopRouter]]
   :ident (fn [] [:component/id :main])
   :initial-state {:main/router {} :main/header {} :main/user {}}}
  (tap> props)
  (let [top-router-state (or (uism/get-active-state this ::TopRouter) :initial)]
    (if (= :initial top-router-state)
      nil
      (div :.container.mx-auto.text-gray-100
           (ui-header (comp/computed header {:user user
                                             :on-signout #(comp/transact! this [(mut/delete-root-user nil)])}))
           (ui-top-router router)))))

(def ui-main (comp/factory Main))

(defsc Root [_ {:root/keys [main]}]
  {:query         [{:root/main (comp/get-query Main)}]
   :initial-state {:root/main {}}}
  (ui-main main))
