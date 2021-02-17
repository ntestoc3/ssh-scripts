#!/usr/bin/env spire

(load-file "hy.clj")

(defmacro tmux-run
  [& cmds]
  (let [cmd-lines (map #(format "(pane.send-keys %s)"
                                (pr-str %1))
                       cmds)]
    `(eval-hy
      ~(format (slurp "tmux.tpl.hy")
               (apply str cmd-lines)))))

(defn setup-tmux
  []
  (install-pip3-lib "libtmux"))

(when (= *file*
         (first *command-line-args*))
  (reconnect!)
  (cond
    (= "setup" (second *command-line-args*))
    (setup-tmux)

    :else
    (let [r (-> `(tmux-run ~@(rest *command-line-args*))
                eval
                :out-lines)]
      (flush)
      (clojure.pprint/pprint r))))
