(import libtmux)
(import time)
(setv srv (libtmux.Server))
(setv sess (if (srv.has-session "spire")
               (srv.find-where {"session_name" "spire"})
               (srv.new-session "spire")))
(setv w (or (sess.find-where {"window_name" "bash"})
            (sess.new-window)))
(setv pane (first w.panes))
%s
(-> (pane.cmd "capture-pane" "-p")
    (. stdout)
    (->> (.join "\n")))

