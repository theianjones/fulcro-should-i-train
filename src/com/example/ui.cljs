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
   [com.fulcrologic.fulcro.dom :as dom :refer [button img div form h1 h2 form h3 input label li ol p ul select option]]
   [com.example.supabase :refer [github-signin github-signout]]
   [com.example.data :refer [readiness-data]]))

(defsc User [_ _]
  {:query [:user/avatar-url :user/email :user/id :user/name :user/username :user/authenticated?]})

(defsc Header [this props {:keys [on-signout user]}]
  {:query [:header [df/marker-table :load-user]]
   :ident (fn [] [:component/id :header])
   :initial-state {}}
  (let [load-user (get props [df/marker-table :load-progress])]
    (div :.flex.justify-between.items-center.my-4.mx-4.lg:mx-0
         (div :.flex.cursor-pointer {:onClick #(dr/change-route! this (if (:user/authenticated? user) ["dashboard"] ["login"]))}
              (img :.mr-2 {:src "https://res.cloudinary.com/dpspogkzf/image/upload/v1640649667/shoulditrain--logo_sjaews.svg"})
              (div :.text-grey-100 "shoulditrain.today"))
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

(defn field [{:keys [label valid? error-message input-class label-class-name] :as props}]
  (let [input-props (-> props
                        (dissoc :label :valid? :error-message :input-class :options))]
    (div
     (dom/label {:htmlFor label
                 :className (str "rounded-lg shadow-sm px-6 py-4 cursor-pointer sm:flex sm:justify-between focus:outline-none"
                                 " "
                                 label-class-name)}
                label)
     (input-class input-props)
     (dom/div {:classes [(when valid? "hidden")]}
              error-message))))

(defsc Question [this {:question/keys [id label byline options] selected :ui/selected}]
  {:query [:question/id
           :question/label
           :question/byline
           :ui/selected
           {:question/options [:option/label
                               :option/value]}]
   :ident :question/id
   :initial-state {}}
  (div :.mb-4.p-4.bg-gray-800.rounded
       (h2 :.text-xl.text-gray-100 label)
       (p :.text-gray-300.mb-2.italic byline)
       (map (fn [o]
              (div :.mb-2
                   (field {:input-class  input
                           :name         id
                           :label        (:option/label o)
                           :value        (str (:option/value o))
                           :id           (:option/label o)
                           :key          (:option/value o)
                           :type         "radio"
                           :autoComplete "off"
                           :className    "hidden"
                           :label-class-name (if (= selected (:option/value o)) "bg-blue-600 text-white" "border bg-white text-gray-700")
                           :onChange     #(m/set-integer! this :ui/selected :event %)})))
            options)))

(def ui-question (comp/factory Question {:keyfn :question/id}))

(defsc Quiz [this {:quiz/keys [version id label questions]}]
  {:query [:quiz/version :quiz/id :quiz/label
           {:quiz/questions (comp/get-query Question)}]
   :ident :quiz/id
   :initLocalState (fn [_ _] {:current-index 0})
   :initial-state {}
   :route-segment ["quiz" :quiz-id]
   :will-enter (fn [app {:keys [quiz-id]}]
                 (dr/route-deferred [:quiz/id quiz-id]
                                    #(df/load! app [:quiz/id quiz-id] Quiz {:post-mutation `dr/target-ready
                                                                            :post-mutation-params {:target [:quiz/id quiz-id]}})))}
  (let [current-index  (comp/get-state this :current-index)
        curr-question  (get questions current-index)
        value          (:ui/selected curr-question)
        last-question? (= current-index (dec (count questions)))]
    (when id
      (div
       (div :.flex.items-center.gap-1.mb3.justify-center.mb-8
            (h2 :.text-3xl.text-gray-100.mb-2 label)
            (p :.text-gray-100 "v" version))
       (dom/form :.lg:mx-64
                 (ui-question curr-question)
                 (div :.flex.gap-1.flex-row-reverse.justify-between
                      (when last-question?
                        (button {:onClick (fn [evt]
                                            (.preventDefault evt)
                                            (comp/transact! this [(mut/create-readiness-response {:quiz/id id})]))
                                 :disabled (nil? value)
                                 :className (str "p-3 rounded bg-blue-600 text-grey-100 disabled:opacity-75 "
                                                 (when (not (nil? value)) "hover:bg-blue-800"))}
                                "Submit"))
                      (when (not last-question?)
                        (button {:onClick (fn [evt]
                                            (.preventDefault evt)
                                            (comp/set-state! this {:current-index (inc current-index)}))
                                 :disabled (nil? value)
                                 :className (str "p-3 rounded bg-blue-600 text-grey-100 disabled:opacity-75 "
                                                 (when (not (nil? value)) "hover:bg-blue-800"))}
                                "Next Question"))
                      (when (not (zero? current-index))
                        (button {:onClick (fn [evt]
                                            (.preventDefault evt)
                                            (comp/set-state! this {:current-index (dec current-index)}))
                                 :className (str "p-3 rounded text-grey-100 bg-gray-800 hover:bg-gray-800")}
                                "Back"))))))))

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

(defn is-today? [date-str]
  (let [today (js/Date.)
        date (js/Date. date-str)]
    (and (= (.getDate today) (.getDate date))
         (= (.getMonth today) (.getMonth date))
         (= (.getFullYear today) (.getFullYear date)))))

(defsc Dashboard [this {:keys [readiness-quiz current-user] :root/keys [response]}]
  {:route-segment ["dashboard"]
   :query [{:readiness-quiz [:quiz/id]}
           {[:root/response '_] [:response/total]}
           {:current-user [{:responses [:response/id :response/total :response/created-at]}]}]
   :ident (fn [] [:component/id :dashboard])
   :initial-state {}
   :will-enter (fn [app _]
                 (dr/route-deferred [:component/id :dashboard]
                                    #(df/load! app
                                               [:component/id :dashboard]
                                               Dashboard
                                               {:post-mutation `dr/target-ready
                                                :post-mutation-params {:target [:component/id :dashboard]}})))}
  (let [todays-response (or response (first (filter (fn [r]
                                                      (is-today? (:response/created-at r))) (:responses current-user))))
        total (:response/total todays-response)]
    (div :.container.flex.flex-col.items-center.mt-10.gap-y-4
         (when (nil? total)
           (comp/fragment
            (h1 :.text-3xl "Find your Readiness score")
            (button :.text-grey-100.font-bold.bg-blue-600.py-3.px-8.rounded-md.border.border-transparent.hover:bg-blue-800.h-12
                    {:onClick #(dr/change-route! this ["quiz" (:quiz/id readiness-quiz)])}
                    "Take Quiz")))
         (when total
           (comp/fragment
            (h1 :.text-3xl "Readiness Quiz Reported")
            (p :.text-4xl total))))))

(dr/defrouter TopRouter [this props]
  {:router-targets [Login Quiz Dashboard]})

(def ui-top-router (comp/factory TopRouter))

(defsc Main [this {:main/keys [header router] current-user :root/user}]
  {:query [{:main/router (comp/get-query TopRouter)}
           {:main/header (comp/get-query Header)}
           {[:root/user '_] (comp/get-query User)}
           [::uism/asm-id ::TopRouter]]
   :ident (fn [] [:component/id :main])
   :initial-state {:main/router {} :main/header {}}}
  (let [top-router-state (or (uism/get-active-state this ::TopRouter) :initial)]
    (if (= :initial top-router-state)
      nil
      (div :.container.mx-auto.text-gray-100
           (ui-header (comp/computed header {:user current-user
                                             :on-signout #(comp/transact! this [(mut/delete-root-user nil)])}))
           (ui-top-router router)))))

(def ui-main (comp/factory Main))

(defsc Root [_ {:root/keys [main user]}]
  {:query         [{:root/main (comp/get-query Main)}
                   {:root/user (comp/get-query User)}]
   :initial-state {:root/main {} :root/user {}}}
  (ui-main main))
