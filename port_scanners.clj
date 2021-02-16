#!/usr/bin/env spire

(load-file "common.clj")

(defmethod get-version :nmap
  [_]
  (when (exist-cmd? "nmap")
    (-> (shell {:cmd "nmap -V"})
        :out
        parse-version)))

(defmethod get-version :masscan
  [_]
  (when (exist-cmd? "masscan")
    (-> (try (shell {:cmd "masscan -V"})
             (catch Exception e
               (ex-data e)))
        :out
        parse-version)))

(defn setup-port-scanners
  []
  (check-install "nmap")
  (check-install "masscan")
  (sudo
   (shell {:cmd "setcap cap_net_raw=eip $(which nmap)"})
   (shell {:cmd "setcap cap_net_raw=eip $(which masscan)"})))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "setup")]
    (reconnect!)
    (case action
      "setup"
      (setup-port-scanners)

      (println "unknown action:" action))))
