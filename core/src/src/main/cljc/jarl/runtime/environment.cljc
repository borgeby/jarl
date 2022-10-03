(ns jarl.runtime.environment)

(def ^:dynamic *env*
  #?(:clj (System/getenv)
     :cljs (if (= *target* "nodejs")
             (into {} (map #(vector % (aget js/process.env %)) (.keys js/Object js/process.env)))
             {})))

(defn env-var [name]
  (get *env* name))
