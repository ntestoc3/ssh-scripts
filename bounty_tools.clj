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
      (mkdir {:path "/data/nuclei"})
      (shell {:cmd "rm -rf /data/nuclei"})
      (shell {:cmd "tar xzf /tmp/nuclei.tgz -C /data/nuclei"})
      (add-local-bin "/data/nuclei/nuclei" "nuclei")
      (println "setup nuclei" (-> (get-version :nuclei)
                                  (:full))
               "over!"))))


