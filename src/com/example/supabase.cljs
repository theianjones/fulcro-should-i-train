(ns com.example.supabase
  (:require ["@supabase/supabase-js" :refer (createClient)]
            [com.example.utils :refer [obj->clj]]
            [clojure.walk :as w]))

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
