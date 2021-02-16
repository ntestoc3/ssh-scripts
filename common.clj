(require '[clojure.string :as str])
(require '[clojure.data.json :as json])

(use 'spire.default)

(def home (System/getenv "HOME"))
(defn expand-home
  [path]
  (str/replace path "~" home))

;; .private_conf.edn保存私有信息配置
(def conf (some-> (expand-home "~/.private_conf.edn")
                  (slurp)
                  (clojure.edn/read-string)))


(defmacro ls [& args]
  (let [cmd (str "ls " (str/join " " args))]
    (debug (shell {:cmd cmd}))))

(defn- parse-host-block
  [block]
  (let [lines (str/split block #"\n\s*")]
    (->> lines
         (map (fn [line]
                (let [[k v] (str/split line #"\s+")]
                  [(-> (str/lower-case k)
                       keyword)
                   v])))
         (into {}))))

(defn parse-ssh-config
  [data]
  (->> (str/split data #"\n\n")
       (map parse-host-block)))

(defn remap-config
  "修正ssh config为spire格式"
  [cfg]
  (cond-> (clojure.set/rename-keys cfg {:host :name
                                        :user :username
                                        :identityfile :identity})
    (:identityfile cfg)
    (update :identity expand-home)
    (:port cfg)
    (update :port clojure.edn/read-string)))

(def default-config (->> (expand-home "~/.ssh/config")
                         slurp
                         parse-ssh-config
                         (map (fn [cfg]
                                (let [host-cfg (remap-config cfg)]
                                  [(-> (:name host-cfg)
                                       keyword)
                                   (dissoc host-cfg :name)])))
                         (into {})))

(defn reconnect!
  []
  (empty!)
  (when-let [server (some->> (:default-ssh-server conf)
                             (get default-config))]
    (push-ssh! server)))

(defn exist-cmd?
  [cmd]
  (try (shell {:cmd (str "which " cmd)})
       true
       (catch Exception _
         false)))

(defn exist-file?
  [path]
  (try (stat path)
       true
       (catch Exception _
         false)))

(defn make-data-dir
  []
  (when-not (exist-file? "/data")
    (sudo
     (shell {:cmd "mkdir -p /data"})
     (shell {:cmd "chmod 777 /data"}))))

(defn install-and-start-docker
  []
  (when-not (exist-cmd? "docker")
    (sudo (apt :update)
          (apt :install "docker-compose")
          (service :started  {:name "docker"})
          (shell {:cmd "usermod -aG docker $USER"}))
    (shell {:cmd "newgrp docker"})))

(defn install-java
  []
  (when-not (exist-cmd? "java")
    (sudo
     (apt-key :present {:public-key-url "https://adoptopenjdk.jfrog.io/adoptopenjdk/api/gpg/key/public"
                        :fingerprint "8ED1 7AF5 D7E6 75EB 3EE3  BCE9 8AC3 B291 7488 5C03"})
     (apt-repo :present {:repo (str "deb https://adoptopenjdk.jfrog.io/adoptopenjdk/deb "
                                    (name (get-fact [:system :codename]))
                                    " main")})
     (apt :update)
     (apt :install "openjdk-11-jre"))))

(defn install-pip3
  []
  (when-not (exist-cmd? "pip3")
    (sudo
     (apt :update)
     (apt :install "python3-pip"))))

(defn install-pip3-lib
  [lib]
  (install-pip3)
  (shell {:cmd (format "pip3 install %s"
                       lib)}))

(defn pip3-lib-info
  [lib]
  (try
    (->> (shell {:cmd (format "pip3 show %s" lib)})
         :out-lines
         (map #(let [[k v] (-> %1
                               (str/split #":" 2)
                               )]
                 [(-> (str/lower-case k)
                      keyword)
                  (str/trim v)]))
         (into {}))
    (catch Exception _
      nil)))

(defn parse-version
  [s]
  (let [version (re-find #"\d[\.\d]*" s)]
    (-> version
        (str/split #"\.")
        (->> (map clojure.edn/read-string)
             (zipmap [:major :minor :rev]))
        (assoc :full version))))

(comment
  (parse-version "test 1")

  (parse-version "This 1.2.3 ")

  )

(defmulti get-version
  identity)

(defmethod get-version :default
  [bin]
  (println "get-version unsupport target" bin))

(defmethod get-version :java
  [_]
  (when (exist-cmd? "java")
    (-> (shell {:cmd "java -version"})
        :err
        parse-version)))

(defmethod get-version :python3
  [_]
  (when (exist-cmd? "python3")
    (-> (shell {:cmd "python3 -V"})
        :out
        parse-version)))

(defn check-install
  ([bin] (check-install bin bin))
  ([bin package-name]
   (when-not (exist-cmd? bin)
     (sudo (apt :update)
           (apt :install package-name)))))

(defn get-github-lastest-info
  [group-project-name]
  (-> (format "https://api.github.com/repos/%s/releases/latest" group-project-name)
      (slurp)
      (json/read-str :key-fn keyword)))

(defn get-github-lastest-version
  [group-project-name]
  (some-> (get-github-lastest-info group-project-name)
          :tag_name
          parse-version))

(defn download-github-lastest-release
  [group-project-name release-name save-path]
  (let [download-path (format "https://github.com/%s/releases/latest/download/%s"
                              group-project-name
                              release-name)]
    (println "github download " download-path "to" save-path)
    (debug (curl {:url download-path
                  :output save-path}))))

(defn add-local-bin
  [path bin-name]
  (let [bin-path (str "/usr/local/bin/" bin-name)]
    (when-not (exist-file? bin-path)
      (println "add usr local binary:" path "-->" bin-name)
      (sudo
       (shell {:cmd (format "ln -s %s %s"
                            path
                            bin-path)})))))
(defn unzip
  [zip-file target-dir]
  (check-install "unzip")
  (mkdir {:path target-dir})
  (shell {:cmd (format "unzip -o %s -d %s"
                       zip-file
                       target-dir)}))

;;;;; make domain cert
(defn make-cert
  [domain]
  (check-install "certbot")
  (install-pip3)

  (upload {:src "./dnshook.sh"
           :dest "/data/dnshook.sh"
           :mode 0777})

  (upload {:src "./dnsclean.sh"
           :dest "/data/dnsclean.sh"
           :mode 0777})

  (sudo
   (shell {:cmd "pip3 install dnslib"})
   ;; https://github.com/putsi/privatecollaborator/blob/22b81e3429b82ec86023cf155e7f3394f471fa00/install.sh#L90
   (shell {:cmd (format
                 "certbot certonly --manual-auth-hook /data/dnshook.sh -m %s --manual-cleanup-hook /data/dnsclean.sh \\
    -d '*.%s' \\
    --server https://acme-v02.api.letsencrypt.org/directory \\
    --manual --agree-tos --no-eff-email --manual-public-ip-logging-ok --preferred-challenges dns-01"
                 (:email conf)
                 domain)
           :creates [(format "/etc/letsencrypt/live/%s/cert.pem"
                             domain)]})
   (shell {:cmd (format
                 "certbot certonly --manual-auth-hook /data/dnshook.sh -m %s --manual-cleanup-hook /data/dnsclean.sh \\
    -d '%s, *.%s' \\
    --server https://acme-v02.api.letsencrypt.org/directory \\
    --manual --agree-tos --no-eff-email --manual-public-ip-logging-ok --preferred-challenges dns-01 \\
    --expand"
                 (:email conf)
                 domain
                 domain)})))

(defn remove-cert
  [domain]
  (sudo
   (shell {:cmd (format "certbot delete --cert-name %s"
                        domain)})))

(defn get-if-info
  "获取主网卡信息"
  []
  (-> (shell {:cmd "ip route get 1"})
      :out
      (->> (re-find #"via\s+([\d\.]+)\s+dev\s+(.*)\s+src\s+([\d\.]+)\s"))
      rest
      (->> (zipmap [:gateway :dev :ip]))))

(defn nat
  [dev op {:keys [src dest type]
           :or {type "tcp"}}]
  (let [op (case op
             :check "-C"
             :append "-A"
             :delete "-D"
             :insert "-I"
             (throw (IllegalArgumentException. (str op))))
        cmd (format "iptables -t nat %s PREROUTING -i %s -p %s --dport %d -j REDIRECT --to-port %d"
                    op
                    dev
                    type
                    dest
                    src)]
    (println cmd)
    (sudo (shell {:cmd cmd}))))

(defn nat-exist?
  [dev info]
  (try (nat dev :check info)
       true
       (catch Exception _
         false)))

(defn nat-ports
  "`ports-maps`: 包含端口映射信息的map列表[{:src 21 :dest 2222 :type \"tcp\"} ...]
               tcp 21 映射到 2222

  DOCKER的规则最先执行，云服务器收到的地址为内网地址，因此匹配到DOCKER规则,后面的规则不生效,
  因此要使用:insert操作"
  [op ports-maps]
  (let [dev (-> (get-if-info)
                :dev)]
    (doseq [info ports-maps]
      (case op
        :append
        (when-not (nat-exist? dev info)
          (nat dev op info))

        :insert
        (when-not (nat-exist? dev info)
          (nat dev op info))

        :delete
        (when (nat-exist? dev info)
          (nat dev op info))

        (throw (IllegalArgumentException. (str op)))))))

;;;;;;;;; docker compose
(defn docker-compose
  "docker-compose命令，
  只提供target的话为up -d"
  ([target] (docker-compose target "up" "-d"))
  ([target action & args]
   (shell {:cmd (str "docker-compose "
                     (name action)
                     " "
                     (str/join " " args))
           :dir target})))


(defn systemctl
  [target action]
  (sudo
   (shell {:cmd (format "systemctl %s %s"
                        action
                        target)})))

;;;;;;;;;; str utils
(defn rand-string
  "生成长度为`n`的随机字符串"
  [n]
  (->> (range n)
       (map (fn [_] (rand-nth "ABCDEFGHIJKLMNOPQRZTUVWXYZabcdefghijklmnopqrztuvwxyz0123456789")))
       (apply str)))
