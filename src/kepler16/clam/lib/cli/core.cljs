(ns kepler16.clam.lib.cli.core
  (:require ["yargs/yargs" :as yargs]
            ["find-up" :as find-up]
            ["path" :as path]
            ["fs" :as fs]
            ["child_process" :as child-process]
            ["util" :as util]
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
                (fn [^js yargs])
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

(defn handle-release [{:keys [config-dir]} argv]
  (doto (child-process/spawn
           "clj"
           #js ["-A:dev" "-X" "kepler16.clam.lib.build.builder/release"]
           #js {:stdio "inherit"
                :cwd config-dir})
    (death/kill-process-on-death!)))

(defn handle-tailwind [{:keys [config-dir]} argv]
  (doto (child-process/spawn
           "npx"
           #js ["tailwindcss-cli@latest" "build" "./resources/css/tailwind.css" "-o" "public/static/css/tailwind.css"]
           #js {:stdio "inherit"
                :cwd config-dir})
    (death/kill-process-on-death!)))


(defn handle-dev [{:keys [config-dir]} argv]
  (doto (child-process/spawn
         "vercel"
         #js ["dev"]
         #js {:stdio "inherit"
              :cwd config-dir})
    (death/kill-process-on-death!))

  (doto (child-process/spawn
          "clj"
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
