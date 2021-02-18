#!/usr/bin/env spire

(load-file "common.clj")

;;;;; amass
(def amass-project-name "OWASP/Amass")

(defmethod get-version :amass
  [_]
  (when (exist-cmd? "amass")
    (-> (shell {:cmd "amass -version"})
        :err
        parse-version)))

(defn setup-amass
  []
  (let [last-ver (get-github-lastest-version amass-project-name)]
    (when-not (= last-ver (get-version :amass))
      (download-github-lastest-release amass-project-name
                                       "amass_linux_amd64.zip"
                                       "/tmp/amass.zip")
      (unzip "/tmp/amass.zip" "/data")
      (add-local-bin "/data/amass_linux_amd64/amass" "amass")
      (println "setup amass" (-> (get-version :amass)
                                 (:full))
               "over!"))))

;;;;; findomain
(def findomain-project-name "Findomain/Findomain")
(defmethod get-version :findomain
  [_]
  (when (exist-cmd? "findomain")
    (-> (shell {:cmd "findomain --version"})
        :out
        parse-version)))

(defn setup-findomain
  []
  (let [last-ver (get-github-lastest-version findomain-project-name)]
    (when-not (= last-ver (get-version :findomain))
      (download-github-lastest-release findomain-project-name
                                       "findomain-linux"
                                       "/data/findomain")
      (attrs {:path "/data/findomain"
              :mode 0555})
      (add-local-bin "/data/findomain" "findomain")
      (println "setup findomain" (-> (get-version :findomain)
                                 (:full))
               "over!"))))

;;;;; ffuf
(def ffuf-project-name "ffuf/ffuf")

(defmethod get-version :ffuf
  [_]
  (when (exist-cmd? "ffuf")
    (-> (shell {:cmd "ffuf -V"})
        :out
        parse-version)))

(defn setup-ffuf
  []
  (let [last-ver (get-github-lastest-version ffuf-project-name)]
    (when-not (= last-ver (get-version :ffuf))
      (download-github-lastest-release ffuf-project-name
                                       (format "ffuf_%s_linux_amd64.tar.gz" (:full last-ver))
                                       "/tmp/ffuf.tgz")
      (shell {:cmd "rm -rf /data/ffuf"})
      (mkdir {:path "/data/ffuf"})
      (shell {:cmd "tar xzf /tmp/ffuf.tgz -C /data/ffuf"})
      (add-local-bin "/data/ffuf/ffuf" "ffuf")
      (println "setup ffuf" (-> (get-version :ffuf)
                                (:full))
               "over!"))))

;;;;;; aquatone
(def aquatone-project-name "ntestoc3/aquatone")

(defmethod get-version :aquatone
  [_]
  (when (exist-cmd? "aquatone")
    (-> (shell {:cmd "aquatone -version"})
        :out
        parse-version)))

(defn setup-aquatone
  []
  (let [last-ver (get-github-lastest-version aquatone-project-name)]
    (when-not (= last-ver (get-version :aquatone))
      (download-github-lastest-release aquatone-project-name
                                       (format "aquatone_linux_amd64_%s.zip" (:full last-ver))
                                       "/tmp/aquatone.zip")
      (unzip "/tmp/aquatone.zip" "/data/aquatone")
      (add-local-bin "/data/aquatone/aquatone" "aquatone")
      (println "setup aquatone " (-> (get-version :aquatone)
                                     (:full))
               "over!"))))

;;;; dirsearch
(defmethod get-version :dirsearch
  [_]
  (when (exist-cmd? "dirsearch")
    (-> (shell {:cmd "dirsearch --version"})
        :out
        parse-version)))

(defn setup-dirsearch
  []
  (when-not (exist-cmd? "dirsearch")
    (install-pip3)
    (shell {:cmd "git clone https://github.com/maurosoria/dirsearch"
            :dir "/data/"})
    (shell {:cmd "pip3 install -r requirements.txt"
            :dir "/data/dirsearch"})
    (add-local-bin "/data/dirsearch/dirsearch.py" "dirsearch"))
  (println "update dirsearch dict.")
  (upload {:src  "./dirsearch/dicc.txt"
           :dest "/data/dirsearch/db/dicc.txt"})
  (println "setup dirsearch done!"))

;;;; arjun
(defmethod get-version :arjun
  [_]
  (when (exist-cmd? "arjun")
    (-> (pip3-lib-info "arjun")
        :version
        parse-version)))

(defn setup-arjun
  []
  (when-not (exist-cmd? "arjun")
    (install-pip3)
    (shell {:cmd "pip3 install arjun"})
    (println "setup arjun " (-> (get-version :arjun)
                                (:full))
             "over!")))

;;;; norecon
(defmethod get-version :norecon
  [_]
  (when (exist-cmd? "norecon")
    (-> (pip3-lib-info "norecon")
        :version
        parse-version)))

(defn setup-norecon
  []
  (when-not (= (get-version :norecon)
               (get-pypi-project-last-version "norecon"))
    (install-pip3-lib "norecon")

    (upload {:src "./norecon/"
             :recurse true
             :preserve true
             :dest "/data/norecon"})

    (docker-compose "/data/norecon")
    (line-in-file :present
                  {:path "$HOME/.profile"
                   :regexp #"^export CHROME_DEV_TOOLS"
                   :line "export CHROME_DEV_TOOLS=\"ws://localhost:3666\""})
    (line-in-file :present
                  {:path "$HOME/.profile"
                   :regexp #"^export USE_CHROME_REMOTE"
                   :line "export USE_CHROME_REMOTE=true"})

    (when-let [token (:wxpusher-token conf)]
      (println "setting nowx token.")
      (shell {:cmd "nowx"
              :stdin token}))

    (println "setup norecon " (-> (get-version :norecon)
                                  (:full))
             "over!")))

;;;;; nuclei
(def nuclei-project-name "projectdiscovery/nuclei")
(defmethod get-version :nuclei
  [_]
  (when (exist-cmd? "nuclei")
    (-> (shell {:cmd "nuclei -version"})
        :err
        parse-version)))

(defn setup-nuclei
  []
  (let [last-ver (get-github-lastest-version nuclei-project-name)]
    (when-not (= last-ver (get-version :nuclei))
      (download-github-lastest-release nuclei-project-name
                                       (format "nuclei_%s_linux_amd64.tar.gz"
                                               (:full last-ver))
                                       "/tmp/nuclei.tgz")
      (shell {:cmd "rm -rf /data/nuclei"})
      (mkdir {:path "/data/nuclei"})
      (shell {:cmd "tar xzf /tmp/nuclei.tgz -C /data/nuclei"})
      (add-local-bin "/data/nuclei/nuclei" "nuclei")
      (println "setup nuclei" (-> (get-version :nuclei)
                                  (:full))
               "over!"))))

;;;;; subfinder
(def subfinder-project-name "projectdiscovery/subfinder")
(defmethod get-version :subfinder
  [_]
  (when (exist-cmd? "subfinder")
    (-> (shell {:cmd "subfinder -version"})
        :err
        parse-version)))

(defn setup-subfinder
  []
  (let [last-ver (get-github-lastest-version subfinder-project-name)]
    (when-not (= last-ver (get-version :subfinder))
      (download-github-lastest-release subfinder-project-name
                                       (format "subfinder_%s_linux_amd64.tar.gz"
                                               (:full last-ver))
                                       "/tmp/subfinder.tgz")
      (shell {:cmd "rm -rf /data/subfinder"})
      (mkdir {:path "/data/subfinder"})
      (shell {:cmd "tar xzf /tmp/subfinder.tgz -C /data/subfinder"})
      (add-local-bin "/data/subfinder/subfinder" "subfinder")
      (println "setup subfinder" (-> (get-version :subfinder)
                                  (:full))
               "over!"))))

;;;;; gau
(def gau-project-name "lc/gau")
(defmethod get-version :gau
  [_]
  (when (exist-cmd? "gau")
    (-> (shell {:cmd "gau -version"})
        :out
        parse-version)))

(defn setup-gau
  []
  (let [last-ver (get-github-lastest-version gau-project-name)]
    (when-not (= last-ver (get-version :gau))
      (download-github-lastest-release gau-project-name
                                       (format "gau_%s_linux_amd64.tar.gz"
                                               (:full last-ver))
                                       "/tmp/gau.tgz")
      (shell {:cmd "rm -rf /data/gau"})
      (mkdir {:path "/data/gau"})
      (shell {:cmd "tar xzf /tmp/gau.tgz -C /data/gau"})
      (add-local-bin "/data/gau/gau" "gau")
      (println "setup gau" (-> (get-version :gau)
                                     (:full))
               "over!"))))

;;;;;;;;;;; cli setup
(load-file "./port_scanners.clj")

(defn setup-bounty-tools
  []
  (install-and-start-docker)
  (make-data-dir)

  (setup-port-scanners)

  (setup-amass)

  (setup-ffuf)

  (setup-aquatone)

  (setup-findomain)

  (setup-norecon)

  (setup-nuclei)

  (setup-subfinder)

  (setup-gau)

  (setup-arjun)
  )

(defn print-version-info
  []
  (let [nmap-version (:full (get-version :nmap))
        masscan-version (:full (get-version :masscan))
        amass-version (:full (get-version :amass))
        ffuf-version (:full (get-version :ffuf))
        aquatone-version (:full (get-version :aquatone))
        findomain-version (:full (get-version :findomain))
        norecon-version (:full (get-version :norecon))
        nuclei-version (:full (get-version :nuclei))
        subfinder-version (:full (get-version :subfinder))
        gau-version (:full (get-version :gau))
        arjun-version (:full (get-version :arjun))
        ]
    (flush)
    (println "amass version:" amass-version)
    (println "aquatone version:" aquatone-version)
    (println "arjun version:" arjun-version)
    (println "ffuf version:" ffuf-version)
    (println "findomain version:" findomain-version)
    (println "gau version:" gau-version)
    (println "masscan version:" masscan-version)
    (println "nmap version:" nmap-version)
    (println "norecon version:" norecon-version)
    (println "nuclei version:" nuclei-version)
    (println "subfinder version:" subfinder-version)
    ))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "setup")]
    (reconnect!)
    (case action
      "setup"
      (setup-bounty-tools)

      "info"
      (print-version-info)

      (println "unsupport action:" action))))



