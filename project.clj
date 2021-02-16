
(require '[clojure.string :as str])

(load-file "./hy.clj")

(defn norecon
  [project & domains]
  (tmux-run
   "cd /data/project"
   (format "norecon -v2 -p %s %s ; nowx %s_over,return:$?"
           project
           (str/join " " domains)
           project)))
