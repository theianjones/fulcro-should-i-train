(ns com.example.ui
  (:require
   [com.example.mutations :as mut]
   [com.fulcrologic.fulcro.algorithms.merge :as merge]
   [com.fulcrologic.fulcro.algorithms.tempid :as tempid]
   [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
   [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
   [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
   [com.fulcrologic.fulcro.raw.components :as rc]
   [com.fulcrologic.fulcro.data-fetch :as df]
   [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
   [com.example.supabase :refer [github-signin github-signout]]))

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

(defsc Root [this {:root/keys [user] :as props}]
  {:query [[df/marker-table :load-progress] :new-thing
           {:root/user (comp/get-query User)}
           [df/marker-table :load-user]
           {:header (comp/get-query Header)}]
   :initial-state {:root/user {} :header {}}}
  (div
   (ui-header (comp/computed (:header props) {:on-signout #(comp/transact! this [(mut/delete-root-user nil)]) :user user}))
   (p (str "Hello " (or (:user/name user) "from the ui/Root component") "!"))
   (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
        (p "Invoke a load! that fails and display the error:")
        (when-let [m (get props [df/marker-table :load-progress])]
          (dom/p "Progress marker: " (str m)))
        (button {:onClick #(df/load! this :i-fail (rc/nc '[*]) {:marker :load-progress})} "I fail!"))
   (div {:style {:border "1px dashed", :margin "1em", :padding "1em"}}
        (p "Simulate creating a new thing with server-assigned ID, leveraging Fulcro's tempid support:")
        (button {:onClick #(let [tmpid (tempid/tempid)]
                             (comp/transact! this [(mut/create-random-thing {:tmpid tmpid})]))}
                "I create!")
        (when-let [things (:new-thing props)]
          (p (str "Created a thing with the ID: " (first (keys things))))))))
