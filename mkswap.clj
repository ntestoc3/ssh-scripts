#!/usr/bin/env spire

(load-file "common.clj")

(defn mkswap
  "创建swap分区，默认大小为4G"
  ([] (mkswap "4G"))
  ([size]
   (when-not (exist-file? "/swapfile")
     (sudo
      (shell {:cmd (format "fallocate -l %s /swapfile"
                           size)})
      (shell {:cmd "chmod 600 /swapfile"})
      (shell {:cmd "mkswap /swapfile"})
      (shell {:cmd "swapon /swapfile"})
      (line-in-file :present
                    {:path "/etc/fstab"
                     :regexp #"^/swapfile"
                     :line "/swapfile swap swap sw 0 0"})))))

(when (= *file*
         (first *command-line-args*))
  (reconnect!)
  (if-let [size (second *command-line-args*)]
    (mkswap size)
    (mkswap)))
