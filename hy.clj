#!/usr/bin/env spire
(require '[clojure.java.io :as io])

(load-file "common.clj")

(defmacro eval-hy
  [& code]
  (let [c (apply str code)]
    `(shell {:cmd "hy - "
             :stdin (format "(print (do %s))"
                            ~c) })))

(defn eval-hy-file
  [path]
  (let [save-path (format "/tmp/hy_%s"
                          (-> (io/file path)
                              (.getName)))]
    (upload {:src path
             :dest save-path})
    (shell {:cmd (str "hy " save-path)})))

(comment

  (debug
   (eval-hy
    (import libtmux)
    (setv srv (libtmux.Server))
    (setv sess
          (if (pos? (len srv.sessions))
            (first srv.sessions)
            (srv.new-session)))
    (setv ws (filter (fn [w] (= w.name "bash")) sess.windows))
    (setv w
          (if (pos? ws)
            (first ws)
            (sess.new-window)))
    (setv pane (first w.panes))
    )))
