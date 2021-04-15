const resolve = (f) => {
  // the below line should work, but causes the node server to blow up with:

  // SHADOW import error /Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs.tools.reader.impl.inspect.js

  // /Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:318
  //   (let [ty (type obj)
  //   ^
  // Error: No protocol method ISwap.-swap! defined for type cljs.core/Atom: [object Object]
  //     at Object.cljs.core/missing-protocol [as missing_protocol] (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:318:3)
  //     at o (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:867:1)
  //     at cljs$core$ISwap$_swap_BANG_$dyn (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:869:36)
  //     at Function.cljs.core/-swap! [as cljs$core$IFn$_invoke$arity$4] (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:867:1)
  //     at Object.cljs$core$_swap_BANG_ [as _swap_BANG_] (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:867:1)
  //     at Function.cljs$core$IFn$_invoke$arity$4 (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/cljs/core.cljs:4526:6)
  //     at Object.shadow$cljs$devtools$client$shared$IRemote$remote_msg$arity$2 (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/shadow/cljs/devtools/client/shared.cljs:279:5)
  //     at Object.shadow$cljs$devtools$client$shared$remote_msg [as remote_msg] (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/shadow/cljs/devtools/client/shared.cljs:14:1)
  //     at WebSocket.<anonymous> (/Users/alexisvincent/Code/kepler/transit/portal/.shadow-cljs/builds/api/dev/out/cljs-runtime/shadow/cljs/devtools/client/node.cljs:63:9)
  //     at WebSocket.emit (node:events:369:20)

	// delete require.cache[require.resolve(f)];
	return require(f);
}

export const requestListener = (clamDir) => (req, res) => {
	try{
		resolve(clamDir + "/.clam/builds/api/dist/handler.js")(req, res);
	} catch (e) {
    res.end("NOT YET READY")
	}
}
