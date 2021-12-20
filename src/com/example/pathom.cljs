(ns com.example.pathom
  "The Pathom parser that is our (in-browser) backend.

   Add your resolvers and 'server-side' mutations here."
  (:require
   [com.wsscode.pathom.core :as p]
   [com.wsscode.pathom.connect :as pc]
   [com.example.supabase :refer [get-user-from-client client]]
   [clojure.set :as set]))

(pc/defresolver index-explorer
  "This resolver is necessary to make it possible to use 'Load index' in Fulcro Inspect - EQL"
  [env _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (-> (get env ::pc/indexes)
       (update ::pc/index-resolvers #(into {} (map (fn [[k v]] [k (dissoc v ::pc/resolve)])) %))
       (update ::pc/index-mutations #(into {} (map (fn [[k v]] [k (dissoc v ::pc/mutate)])) %)))})

(pc/defresolver i-fail
  [_ _]
  {::pc/input  #{}
   ::pc/output [:i-fail]}
  (throw (ex-info "Fake resolver error" {})))

(pc/defresolver person
  [_ {id :person/id}]
  {::pc/input  #{:person/id}
   ::pc/output [:person/id :person/name]}
  {:person/id id, :person/name (str "Joe #" id)})

(pc/defresolver user-authenticated? [env {:keys [current-user]}]
  {::pc/input #{:current-user}
   ::pc/output [:user/authenticated?]}
  (prn current-user (-> current-user
                        :user/id
                        nil?
                        not))
  {:user/authenticated? (-> current-user
                            :user/id
                            nil?
                            not)})

(pc/defresolver current-user
  [env _]
  {::pc/input #{}
   ::pc/output [{:current-user [:user/id :user/email :user/name :user/username :user/avatar-url]}]}
  (let [user (get-user-from-client (::client env))
        user-id (:id user)
        user-meta (select-keys (:user_metadata user) [:email :name :user_name :avatar_url])
        renamed-meta (set/rename-keys user-meta {:email :user/email :name :user/name :user_name :user/username :avatar_url :user/avatar-url})]
    {:current-user
     (conj {:user/id user-id} renamed-meta)}))

(pc/defmutation create-random-thing [env {:keys [tmpid] :as params}]
  ;; Fake generating a new server-side entity with
  ;; a server-decided actual ID
  ;; NOTE: To match with the Fulcro-sent mutation, we
  ;; need to explicitly name it to use the same symbol
  {::pc/sym 'com.example.mutations/create-random-thing
   ::pc/params [:tempid]
   ::pc/output [:tempids]}
  (println "SERVER: Simulate creating a new thing with real DB id 123" tmpid)
  {:tempids {tmpid 123}})

(def my-resolvers-and-mutations
  "Add any resolvers you make to this list (and reload to re-create the parser)"
  [index-explorer create-random-thing i-fail person current-user user-authenticated?])

(defn new-parser
  "Create a new Pathom parser with the necessary settings"
  []
  (p/parallel-parser
   {::p/env     {::p/reader [p/map-reader
                             pc/parallel-reader
                             pc/open-ident-reader]
                 ::pc/mutation-join-globals [:tempids]
                 ::client client}
    ::p/mutate  pc/mutate-async
    ::p/plugins [(pc/connect-plugin {::pc/register my-resolvers-and-mutations})
                 p/error-handler-plugin
                 p/request-cache-plugin
                 (p/post-process-parser-plugin p/elide-not-found)]}))
