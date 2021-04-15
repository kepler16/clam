const resolve = (f) => {
	delete require.cache[require.resolve(f)];
	return require(f);
}

export const requestListener = (clamDir) => (req, res) => {
	try{
		resolve(clamDir + "/.clam/builds/api/dist/handler.js")(req, res);
	} catch (e) {
    res.end("NOT YET READY")
	}
}
