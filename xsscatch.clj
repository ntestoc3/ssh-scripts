#!/usr/bin/env spire

(load-file "common.clj")

(def xss-catch-path "/data/XSS-Catcher")

(defn setup-xss-catch
  [domain]
  (install-and-start-docker)
  (make-data-dir)

  (when-not
      (exist-file? (str xss-catch-path "/.git"))
    ;; clone code
    (sudo
     (apt :install "git"))
    (shell {:cmd "git clone https://github.com/daxAKAhackerman/XSS-Catcher"
            :dir "/data/"}))

  ;; 修改docker-compose配置
  (upload {:src  "./XSS-Catcher/docker-compose.yml"
           :dest (str xss-catch-path "/docker-compose.yml")})

  ;; 修改nginx配置
  (mkdir {:path (str xss-catch-path "/conf.d")})
  (upload {:content (selmer "./XSS-Catcher/default.conf"
                            {:domain domain})
           :dest (str xss-catch-path "/conf.d/default.conf")})
  (upload {:src (expand-home "~/keys/domain.crt")
           :dest (str xss-catch-path "/conf.d/domain.crt")})
  (upload {:src (expand-home "~/keys/domain.key")
           :dest (str xss-catch-path "/conf.d/domain.key")})

  ;; 启动docker
  (docker-compose xss-catch-path))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "up")]
    (reconnect!)
    (cond
      (= "up" action)
      (setup-xss-catch (:xss-catch-domain conf))

      (= "recreate" action)
      (do (docker-compose xss-catch-path "rm" "-s" "-f")
          (setup-xss-catch (:xss-catch-domain conf)))

      (#{"start" "stop" "logs" "ps"} action)
      (docker-compose xss-catch-path action)

      :else
      (println "unsupport action:" action))))
