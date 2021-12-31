(ns com.example.supabase
  (:require ["@supabase/supabase-js" :refer (createClient)]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

(def api-url "https://dbezknjgkanktnzqbxwl.supabase.co")

(def api-key "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlIjoiYW5vbiIsImlhdCI6MTYzOTQzMTkyMiwiZXhwIjoxOTU1MDA3OTIyfQ.ra7jdBsHq9UMIa-gQvH-IT2O6ALdD6iZ_lY7mv_EMHE")

(defonce client (atom (createClient api-url api-key)))

(defn github-signin []
  (-> @client
      .-auth
      (.signIn #js {:provider "github"})))

(defn github-signout []
  (-> @client
      .-auth
      (.signOut)))

(defn get-user-from-client [^js client]
  (-> @client
      .-auth
      .user
      (js->clj :keywordize-keys true)))

(defn select-responses [client {:user/keys [id]}]
  (go (try
        (<p! (-> @client
                 (.from "response_scores")
                 (.select "*")
                 (.eq "user_id" id)))
        (catch js/Error err (js/console.log (ex-cause err))))))

(defn insert-response [{:keys [response/total user/id]}]
  (go
    (try
      (<p! (-> @client
               (.from "response_scores")
               (.insert #js [#js {"total" total "user_id" id}])))
      (catch js/Error err (js/console.log (ex-cause err))))))

(defn insert-response-answers [{:keys [answers] response-id :response/id}]
  (go
    (try
      (<p! (-> @client
               (.from "question_answers")
               (.insert (clj->js (map (fn [answer]
                                        #js {"response_id" response-id
                                             "score" (:answer/score answer)
                                             "question_id" (:question/id answer)
                                             "user_id" (:user/id answer)}) answers)))))
      (catch js/Error err (js/console.log (ex-cause err))))))
