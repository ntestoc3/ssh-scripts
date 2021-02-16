#!/usr/bin/env spire

(load-file "common.clj")

(defn setup-xss-catch
  [domain]
  (install-and-start-docker)
  (make-data-dir)

  (when-not
      (exist-file? "/data/XSS-Catcher/.git")
    ;; clone code
    (sudo
     (apt :install "git"))
    (shell {:cmd "git clone https://github.com/daxAKAhackerman/XSS-Catcher"
            :dir "/data/"}))

  ;; 修改docker-compose配置
  (upload {:src  "./XSS-Catcher/docker-compose.yml"
           :dest "/data/XSS-Catcher/docker-compose.yml"})

  ;; 修改nginx配置
  (mkdir {:path "/data/XSS-Catcher/conf.d"})
  (upload {:content (selmer "./XSS-Catcher/default.conf"
                            {:domain domain})
           :dest "/data/XSS-Catcher/conf.d/default.conf"})
  (upload {:src "~/keys/domain.crt"
           :dest "/data/XSS-Catcher/conf.d/domain.crt"})
  (upload {:src "~/keys/domain.key"
           :dest "/data/XSS-Catcher/conf.d/domain.key"})

  ;; 启动docker
  (shell {:cmd "docker-compose up -d"
          :dir "/data/XSS-Catcher/"}))

(defn stop-xss-catcher
  []
  (shell {:cmd "docker-compose stop"
          :dir "/data/XSS-Catcher/"}))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "start")]
    (reconnect!)
    (case action
      "stop"
      (stop-xss-catcher)

      "start"
      (setup-xss-catch (:xss-catch-domain conf))

      (println "unknown action:" action))))
