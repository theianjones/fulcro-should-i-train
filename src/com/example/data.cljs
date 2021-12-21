(ns com.example.data)

(def readiness-data
  {:version 0,
   :id "375623e0-92dd-4815-b507-4d577a55f37c",
   :questions
   [{:value "How many days in a row have you trained?",
     :id "trained",
     :options
     [{:label "4+ days", :value 1}
      {:label "3 days", :value 2}
      {:label "2 days", :value 3}
      {:label "1 days", :value 4}
      {:label "Coming off a rest day", :value 5}]}
    {:value "How sore are you?",
     :id "soreness",
     :options
     [{:value "Can't walk, send help", :score 1}
      {:value "I'm pretty sore and it impacts my day", :score 2}
      {:value "I'm Kinda sore, but it's nothing I can't handle",
       :score 3}
      {:value "I feel awesome", :score 4}]}
    {:value "How excited are you to train today?",
     :id "excited",
     :options
     [{:value "I actually don't want to train", :score 1}
      {:value "I'll train, but ONLY because I have to", :score 2}
      {:value "I want to and I'll do my best", :score 3}
      {:value "I am PUMPED to train", :score 4}]}
    {:value "Whats your mood like?",
     :id "mood",
     :options
     [{:value "Leave me alone", :score 1}
      {:value "Eh, I'm alright. A little stressed", :score 2}
      {:value "I feel good", :score 3}]}
    {:value "What are your hormones like?",
     :id "hormones",
     :byline "Sex drive and appetite",
     :options
     [{:value "Both are...lacking", :score 1}
      {:value "I'm lacking in one", :score 2}
      {:value "I'm hungry...for both", :score 3}]}
    {:value "How’s your immune system?",
     :id "immune",
     :byline "sickness, gastrointestinal, stool",
     :options
     [{:value "I'm actually sick", :score 0}
      {:value "I'm having issues with digestion and poop", :score 1}
      {:value "I'm having issues with either digestion or poop",
       :score 2}
      {:value "No issues", :score 3}]}
    {:value "How many hours of sleep did you get last night?",
     :id "hours",
     :options
     [{:value "Less than 7 hours", :score 0}
      {:value "Between 7 and 8 hours", :score 1}
      {:value "I got more than 8 hours", :score 2}]}
    {:value "What was your sleep quality like?",
     :id "quality",
     :options
     [{:value "I tossed. I turned. It wasn't good.", :score 0}
      {:value "I got restful sleep without much disturbance.",
       :score 1}]}]})
