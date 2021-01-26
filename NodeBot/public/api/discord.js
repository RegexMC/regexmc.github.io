const express = require("express");
const btoa = require("btoa");
const config = require("../../config.json");
const router = express.Router();
const axios = require("axios");
const DiscordOauth2 = require("discord-oauth2");
require("dotenv").config();
const path = require("path");
const mongoUtil = require("../../mongoUtil");

router.get("/logout", async function (req, res) {
	res.cookie("discord_username", "", {
		maxAge: 0
	});
	res.cookie("discord_id", "", {
		maxAge: 0
	});
	res.cookie("discord_token", "", {
		maxAge: 0
	});
	res.redirect("/");
});

router.get("/login", (req, res) => {
	if (process.env.DEV) {
		res.redirect(
			"https://discord.com/api/oauth2/authorize?client_id=802515790233731072&redirect_uri=http%3A%2F%2Flocalhost%2Fpublic%2Fapi%2Fdiscord%2Fcallback&response_type=code&scope=identify"
		);
	} else {
		res.redirect(
			"https://discord.com/api/oauth2/authorize?client_id=778936290090942514&redirect_uri=http%3A%2F%2Fuuwuu.xyz%2Fpublic%2Fapi%2Fdiscord%2Fcallback&response_type=code&scope=identify"
		);
	}
});

/**
 * Credit to MYNAMERYAN - wouldn't have got the requests working w/o him
 */
router.get("/callback", async function (req, res) {
	if (!req.query.code) throw new Error("NoCodeProvided");
	const oauth = new DiscordOauth2();

	const exchangeParams = new URLSearchParams();
	exchangeParams.append("grant_type", "authorization_code");
	exchangeParams.append("code", req.query.code);
	if (process.env.DEV) {
		exchangeParams.append("redirect_uri", "http://localhost/public/api/discord/callback");
	} else {
		exchangeParams.append("redirect_uri", "http://uuwuu.xyz/public/api/discord/callback");
	}

	const exchangeOptions = {
		headers: {
			"Content-Type": "application/x-www-form-urlencoded",
			Authorization: `Basic ${btoa(
				`${process.env.DEV ? "802515790233731072" : "778936290090942514"}:${process.env.DEV ? config.discord_dev_secret : config.discord_secret}`
			)}`
		}
	};

	axios
		.post("https://discord.com/api/oauth2/token", exchangeParams, exchangeOptions)
		.then(({ data }) => {
			// Get user ID and username using the token just received
			var token = data.access_token;

			res.cookie("discord_token", token, {
				httpOnly: true
			});

			oauth
				.getUser(token)
				.then((response) => {
					res.cookie("discord_username", response.username + "#" + response.discriminator, {
						httpOnly: true
					});
					res.cookie("discord_id", response.id, {
						httpOnly: true
					});

					mongoUtil.userSettings(response.id).then((s) => {
						if (s) {
							res.redirect(`/`);
						} else {
							var obj = {
								discord_id: response.id,
								anilist_username: "",
								anilist_id: "",
								minecraft_uuid: "",
								settings: {
									privacy: {
										show_anilist: true,
										show_osu: true,
										show_minecraft: true
									}
								}
							};

							mongoUtil.insertUser(obj).then((x) => {
								res.redirect(`/`);
							});
						}
					});
				})
				.catch((err) => {
					if (err.response) {
						console.log("API Error", err.response.data);
					} else {
						console.log("Request Error", err.message);
					}
					res.sendFile(path.join(__dirname, "../error.html"));
					return;
				});
		})
		.catch((err) => {
			if (err.response) {
				console.log("API Error", err.response.data);
			} else {
				console.log("Request Error", err.message);
			}
			res.sendFile(path.join(__dirname, "../error.html"));
			return;
		});
});

module.exports = router;
