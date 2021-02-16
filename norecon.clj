#!/usr/bin/env spire
;; 配置norecon

(load-file "common.clj")
(load-file "./port_scanners.clj")
(load-file "./bounty_tools.clj")

(defn setup-norecon-all
  []
  (install-and-start-docker)
  (make-data-dir)

  (setup-port-scanners)

  (setup-amass)

  (setup-ffuf)

  (setup-aquatone)

  (setup-findomain)

  (setup-norecon))

(defn print-version-info
  []
  (let [nmap-version (:full (get-version :nmap))
        masscan-version (:full (get-version :masscan))
        amass-version (:full (get-version :amass))
        ffuf-version (:full (get-version :ffuf))
        aquatone-version (:full (get-version :aquatone))
        findomain-version (:full (get-version :findomain))
        norecon-version (:full (get-version :norecon))]
    (flush)
    (println "nmap version:" nmap-version)
    (println "masscan version:" masscan-version)
    (println "amass version:" amass-version)
    (println "ffuf version:" ffuf-version)
    (println "aquatone version:" aquatone-version)
    (println "findomain version:" findomain-version)
    (println "norecon version:" norecon-version)))

(when (= *file*
         (first *command-line-args*))
  (let [action (or (second *command-line-args*)
                   "setup")]
    (reconnect!)
    (case action
      "setup"
      (setup-norecon-all)

      "info"
      (print-version-info)

      (println "unsupport action:" action))))

