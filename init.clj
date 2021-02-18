#!/usr/bin/env spire

(load-file "common.clj")

(load-file "./xsscatch.clj")
(load-file "./port_scanners.clj")
(load-file "./bb.clj")
(load-file "./burp_collaborator.clj")
(load-file "./bounty_tools.clj")

;;;;;;;;; init script

(defn update-bounty-tools
  []

  (setup-amass)

  (setup-ffuf)

  (setup-aquatone)

  (setup-arjun)

  (setup-norecon)

  (setup-findomain)

  (setup-nuclei)

  (setup-subfinder)

  (setup-gau)

  )


(comment

  (make-data-dir)

  (setup-xss-catch)

  (install-java)

  (setup-dirsearch)

  (setup-bb)

  (setup-port-scanners)

  (setup-amass)

  (setup-ffuf)

  (setup-aquatone)

  ;; 设置PATH
  (shell {:cmd "echo 'export PATH=\"$HOME/.local/bin:$PATH\"' > $HOME/.profile"
          :creates ["$HOME/.profile"]})

  (shell {:cmd "source $HOME/.profile"})

  (setup-arjun)

  (def d (:burp-domain conf))

  (make-cert d)

  (setup-burp-server d)

  (stop-burp-server)

  ;; $HOME会转义
  (debug (line-in-file :present
                       {:path "$HOME/.profile"
                        :regexp #"^export PATH"
                        :line "export PATH=\"\\$HOME/.local/bin:$PATH\""}))

  (shell {:cmd "docker-compose stop"
          :dir "/data/XSS-Catcher/"})

  (debug (shell {:cmd "ss -nltp"}))

  (debug (sudo
          (shell {:cmd "iptables -L -t nat "})))

  )
