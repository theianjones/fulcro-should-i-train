(ns com.example.data)

(def readiness-data
  #:quiz{:version 0
         :id "375623e0-92dd-4815-b507-4d577a55f37c"
         :label "Readiness Quiz"
         :questions
         [#:question{:label "How many days in a row have you trained?"
                     :id "trained",
                     :options
                     [#:option{:label "4+ days", :value 1}
                      #:option{:label "3 days", :value 2}
                      #:option{:label "2 days", :value 3}
                      #:option{:label "1 days", :value 4}
                      #:option{:label "Coming off a rest day", :value 5}]}
          #:question{:label "How sore are you?"
                     :id "soreness",
                     :options
                     [#:option{:label "Can't walk, send help", :value 1}
                      #:option{:label "I'm pretty sore and it impacts my day", :value 2}
                      #:option{:label "I'm Kinda sore, but it's nothing I can't handle",
                               :value 3}
                      #:option{:label "I feel awesome", :value 4}]}
          #:question{:label "How excited are you to train today?"
                     :id "excited",
                     :options
                     [#:option{:label "I actually don't want to train", :value 1}
                      #:option{:label "I'll train, but ONLY because I have to", :value 2}
                      #:option{:label "I want to and I'll do my best", :value 3}
                      #:option{:label "I am PUMPED to train", :value 4}]}
          #:question{:label "Whats your mood like?"
                     :id "mood",
                     :options
                     [#:option{:label "Leave me alone", :value 1}
                      #:option{:label "Eh, I'm alright. A little stressed", :value 2}
                      #:option{:label "I feel good", :value 3}]}
          #:question{:label "What are your hormones like?"
                     :id "hormones",
                     :byline "Sex drive and appetite",
                     :options
                     [#:option{:label "Both are...lacking", :value 1}
                      #:option{:label "I'm lacking in one", :value 2}
                      #:option{:label "I'm hungry...for both", :value 3}]}
          #:question{:label "How’s your immune system?"
                     :id "immune",
                     :byline "sickness, gastrointestinal, stool",
                     :options
                     [#:option{:label "I'm actually sick", :value 0}
                      #:option{:label "I'm having issues with digestion and poop", :value 1}
                      #:option{:label "I'm having issues with either digestion or poop",
                               :value 2}
                      #:option{:label "No issues", :value 3}]}
          #:question{:label "How many hours of sleep did you get last night?"
                     :id "hours",
                     :options
                     [#:option{:label "Less than 7 hours", :value 0}
                      #:option{:label "Between 7 and 8 hours", :value 1}
                      #:option{:label "I got more than 8 hours", :value 2}]}
          #:question{:label "What was your sleep quality like?"
                     :id "quality"
                     :options
                     [#:option{:label "I tossed. I turned. It wasn't good.", :value 0}
                      #:option{:label "I got restful sleep without much disturbance."
                               :value 1}]}]})

(def quizzes [readiness-data])
