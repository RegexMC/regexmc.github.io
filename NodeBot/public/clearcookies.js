const express = require("express");
const { proc } = require("node-os-utils");
const router = express.Router();
require("dotenv").config();

const catchAsync = (fn) => (req, res, next) => {
	const routePromise = fn(req, res, next);
	if (routePromise.catch) {
		routePromise.catch((err) => next(err));
	}
};

router.get(
	"/",
	catchAsync(async (req, res) => {
		Object.keys(req.cookies).forEach((cookie) => {
			res.cookie(cookie, "", {
				maxAge: 0
			});
		});
		if (process.env.DEV == "true") {
			res.redirect("http://localhost/");
		} else {
			res.redirect("http://uuwuu.xyz/");
		}
	})
);

module.exports = router;
