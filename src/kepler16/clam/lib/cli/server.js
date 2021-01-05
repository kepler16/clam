const resolve = (f) => {
	// delete require.cache[require.resolve(f)];
	return require(f);
}

export const requestListener = (clamDir) => (req, res) => {
	console.log (clamDir)
	try{
		resolve(clamDir + "/api/dist/handler.js")(req, res);
	} catch (e) {
    res.end("NOT YET READY")
	}
}
