# Clam
## DO NOT USE THIS!!! EVERYTHING WILL CHANGE! EVERYTHING YOU BUILD WITH THIS WILL PROBABLY BREAK IN THE FUTURE

Basically trying to make a nextjs for clojurescript. We're very much trying to provide the same things for the same reasons.

Check out the [template](https://github.com/kepler16/clam-template) for an example of usage.

# notes
- We rely on `deps.edn`, no Leiningen support
- Clam uses `shadow-cljs` under the hood
- We assume a `pages` directory in the root of the clam project (where your site entry points will go). This directory uses file based routing, so the location of the `.cljs` files in it matter.
- You need to make sure you have `pages` and `.clam/cp` in your deps.edn `:paths` entry.
- Clam analyses code in the `pages` dir and generates complimentary code in `.clam/cp`
- Clam apps are react apps that execute in a browser and are Serverside rendered via serverless functions in a node process.


# crash course on internals
Clam leverages shadow-cljs for compilation, watching and hot-reloading. The integration is scripted so no shadow-cljs.edn is required.

Running `npx clam dev` runs a node script which:
- Crawls directories upwards to find the `clam.edn` file, marking the clam project root
- Does some directory preparation
- Starts an node http server that can execute serverless functions
- Kicks off a jvm which runs the clam build watcher

The clam build watcher watches the `pages` directory and runs the clojurescript analyzer against the `.cljs` files, which it then uses to create optimised shadow-cljs build configurations.

The build watcher kicks off a shadow-cljs server process and starts 2 watch processes. Once for the browser `:clam/site` and one for the serverless node environment `:clam/api`. Both of these are attachable with a repl.

# Development Story
- Clone `clam template`
- run `npm install`
- run `npx clam dev`
- Connect to a remote repl from your editor (port should be auto picked up by the editor from `.shadow-cljs/nrepl.port` file). Attach to the shadow-cljs build: Either `:clam/api` or `clam/site` or both, depending on where you want your repl. Note that because there's no shadow-cljs.edn file, emacs (and probably others) won't auto pick up these builds so you will need to manually type them in.

You should have a hot reloadable development story. To add a new page to the site, just make a `.cljs` file in the corresponding place and

# Vercel
Adding `npx clam release --vercel` to your `package.json` build script will generate release files in a way that just works with vercel.

# Roadmap
- [X] hot reloading
- [X] file based routing
- [X] serverside rendering
- [X] Document and head manipulation
- [ ] react wrapper
- [ ] extensible serverless functions
- [ ] React Fast Refresh
- [ ] better & dynamic file based routing
- [ ] dynamic module loading and progressive loading
- [ ] route prefetching
- [ ] SSG (build time generation / rendering) for static site generation
