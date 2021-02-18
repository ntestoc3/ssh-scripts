#!/usr/bin/env spire
;; 配置http下载服务器

(load-file "common.clj")

(def hftp-path "/data/hftp_nginx")

(defn setup-hftp
  [domain port]
  (when-not (and domain port)
    (throw (IllegalArgumentException. (format "setup-hftp no %s."
                                              (if domain
                                                "port"
                                                "domain")))))

  (install-and-start-docker)

  (mkdir {:path "/data/html"})

  (mkdir {:path (str hftp-path "/conf.d")})
  ;; 修改docker-compose配置
  (upload {:content (selmer "./hftp_nginx/default.conf"
                            {:domain domain
                             :port port})
           :dest (str hftp-path "/conf.d/default.conf")})
  (upload {:src "./hftp_nginx/docker-compose.yml"
           :dest (str hftp-path "/docker-compose.yml")})

  (docker-compose hftp-path))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "up")]
    (reconnect!)
    (cond
      (= "up" action)
      (setup-hftp (:hftp-domain conf)
                  (:hftp-port conf))

      (= "recreate" action)
      (do (docker-compose hftp-path "rm" "-s" "-f")
          (setup-hftp (:hftp-domain conf)
                      (:hftp-port conf)))

      (#{"start" "stop" "logs" "ps"} action)
      (docker-compose hftp-path action)

      :else
      (println "unsupport action:" action))))
