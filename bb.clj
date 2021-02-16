#!/usr/bin/env spire

(load-file "common.clj")

;;;;; babashka
(def bb-project-name "babashka/babashka")

(defmethod get-version :bb
  [_]
  (when (exist-cmd? "bb")
    (-> (shell {:cmd "bb --version"})
        :out
        parse-version)))

(defn setup-bb
  []
  (let [last-ver (get-github-lastest-version bb-project-name)]
    (when-not (= last-ver (get-version :bb))
      (curl {:url "https://raw.githubusercontent.com/babashka/babashka/master/install"
             :output "/tmp/bb-install"})
      (attrs {:path "/tmp/bb-install"
              :mode 0777})
      (check-install "unzip")
      (sudo
       (debug (shell {:cmd "/tmp/bb-install"})))
      (rm "/tmp/bb-install")
      (println "setup bb" (-> (get-version :bb)
                              (:full))
               "over!"))))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "setup")]
    (reconnect!)
    (case action
      "setup"
      (setup-bb)

      (println "unknown action:" action))))
