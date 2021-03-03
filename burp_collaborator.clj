#!/usr/bin/env spire

(load-file "common.clj")

;;;;; burp collaborator server
(def public-ip (-> (get-fact)
                   (get-in [:ssh-config :hostname])))

(defn setup-burp-server
  "polling server端口开放19443"
  [domain]
  (install-java)

  (let [metric-path (or (:burp-metric-path conf)
                        (rand-string 32)) ]
    (println "burp collaborator metric path:" metric-path)
    (upload {:content (selmer "collaborator.json"
                              {:domain domain
                               :metric-path metric-path
                               :local-ips (-> (get-if-info)
                                              :ip)
                               :public-ip public-ip})
             :dest "/data/burp/collaborator.conf"}))

  ;; copy keys
  (when-not (exist-file? "/data/burp/keys/fullchain.pem")
    (sudo
     (make-cert domain)
     (mkdir {:path "/data/burp/keys"})
     (shell {:cmd (format "cp -L /etc/letsencrypt/live/%s/*.pem /data/burp/keys"
                          domain)})
     (shell {:cmd "chmod 444 /data/burp/keys/*.pem"})))

  (when-not (exist-file? "/data/burp/burp.jar")
    (curl {:url "https://portswigger.net/burp/releases/download?product=pro&version=2021.2&type=Jar"
           :output "/data/burp/burp.jar"}))

  (sudo
   (upload {:src "./collaborator.service"
            :dest "/etc/systemd/system/collaborator.service"})
   (systemctl "collaborator" "start")))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "up")]
    (reconnect!)
    (cond
      (= "up" action)
      (if-let [domain (:burp-domain conf)]
        (do
          (println "setup burp server domain:" domain)
          (setup-burp-server (:burp-domain conf)))
        (println "not set burp domain in ~/.private_conf.edn"))

      (#{"stop" "start" "status" "restart"} action)
      (systemctl "collaborator" action)

      :else
      (println "unsupport action:" action))))
