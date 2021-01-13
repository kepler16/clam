(ns kepler16.clam.lib.cli.core
  (:require ["yargs/yargs" :as yargs]
            ["find-up" :as find-up]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as child-process]
            ["util" :as util]
            ["express" :as express]
            ["http" :as http]
            ["./server" :as dev-server]
            ["mkdirp" :as mkdirp]
            [kepler16.clam.lib.cli.death :as death]
            [clojure.edn :as edn]
            [cljs.core.async :as a]
            [process :as process]))

(defn resolve-to [p data]
  (fn [argv]
    (a/put! p data)))

(defn yarger [p]
  (-> (yargs)
      (.command "dev", "start the dev server"
                (fn [^js yargs])
                  ;; (-> yargs
                  ;;     (.positional "port" #js {:describe "port to bind on"
                  ;;                              :default 5000})))
                (resolve-to p :dev))
      (.command "tailwind", "compile tailwind"
                (fn [^js yargs])
                (resolve-to p :tailwind))
      (.command "release", "Optimised release build"
                (fn [^js yargs]
                  (-> yargs
                      (.option "vercel" {})))
                (resolve-to p :release))
      (.demandCommand)
      (.recommendCommands)
      (.strict)
      (.help)))

(defn find-up [matcher]
  (let [config-path (find-up/sync (clj->js matcher) #js {})
        config-dir (some-> config-path path/dirname)
        config (some-> config-path
                       (fs/readFileSync #js {:encoding "utf8"})
                       (edn/read-string))]
    (when config
      {:config-path config-path
       :config-dir config-dir
       :config config})))

(defn prepare-files [config-dir]
  (try
    (fs/unlinkSync ".clam/builds/api/dist/handler.js")
    (catch js/Error e nil))
  (mkdirp/sync (str config-dir "/.clam/cp")))

(def setup-shell "
amazon-linux-extras install java-openjdk11
curl -O https://download.clojure.org/install/linux-install-1.10.1.763.sh
chmod +x linux-install-1.10.1.763.sh
./linux-install-1.10.1.763.sh
")

(def exec (util/promisify child-process/exec))

(defn setup-vercel []
  (println "installing clojure...")
  (exec setup-shell))

(defn release* [config-dir vercel?]

  (prepare-files config-dir)

  (doto (child-process/spawn
         "clojure"
         #js ["-A:dev" "-X" "kepler16.clam.lib.build.builder/release" "vercel" vercel?]
         #js {:stdio "inherit"
              :cwd config-dir})
    (death/kill-process-on-death!)))

(defn handle-release [{:keys [config-dir]} {:as x :strs [vercel]}]
  (if vercel
    (-> (setup-vercel)
        (.then
         (fn [x]
           (let [{:strs [stdout stderr]} (js->clj x)]

             (when stderr
               (js/console.error stderr))

             (when stdout
               (js/console.log stdout))

             (release* config-dir vercel))))
        (.catch (fn [e] (js/console.error e))))
    (release* config-dir vercel)))

(defn handle-tailwind [{:keys [config-dir]} argv]
  (doto (child-process/spawn
         "npx"
         #js ["tailwindcss-cli@latest" "build" "./resources/css/tailwind.css" "-o" "public/static/css/tailwind.css"]
         #js {:stdio "inherit"
              :cwd config-dir})
    (death/kill-process-on-death!)))

(defn dev-server [root]
  (let [app (express)]
    (doto app
      (.use (.static express "public"))
      (.use (dev-server/requestListener root))
      (.listen 3000))))


  ;; (doto (http/createServer (dev-server/requestListener root))
  ;;   (.listen 3000)))


(defn handle-dev [{:keys [config-dir]} argv]
  ;; (doto (child-process/spawn
  ;;        "vercel"
  ;;        #js ["dev"]
  ;;        #js {:stdio "inherit"
  ;;             :cwd config-dir})
  ;;   (death/kill-process-on-death!))
  (prepare-files config-dir)
  (dev-server config-dir)

  (doto (child-process/spawn
         "clojure"
         #js ["-A:dev" "-X" "kepler16.clam.lib.build.builder/watch"]
         #js {:stdio "inherit"
              :cwd config-dir})
    (death/kill-process-on-death!)))

(defn handle [args]
  (let [p (a/promise-chan)
        argv
        (-> (yarger p)
            (.parse args)
            (js->clj))
        context (find-up ["clam.edn"])]
    (a/go
      (let [command (a/<! p)]
        (case command
          :dev (handle-dev context argv)
          :release (handle-release context argv)
          :tailwind (handle-tailwind context argv))))))

(defn cli [args]
  (try
    (death/on-death! (fn [] (process/exit 1)))
    (handle (.splice args 2))
    (catch :default ex
      (println ex)
      (js/process.exit 1))))
