const cacheManager = require("cache-manager");
const { Client } = require("@zikeji/hypixel");
const Discord = require("discord.js");
const fs = require("fs");
const Enmap = require("enmap");
const config = require("./config.json");
const cron = require("node-cron");
const express = require("express");
const path = require("path");
const mongoUtil = require("./mongoUtil");
require("dotenv").config();
const { default: axios } = require("axios");

const cache = cacheManager.caching({
	store: "memory",
	ttl: 5 * 60,
	max: 500
});

mongoUtil.connectToServer(function (err, client) {
	if (err) console.log(err);
	console.log("Connected to database");

	const hypixelClient = new Client(config.hypixel_api_key, {
		cache: {
			get(key) {
				return cache.get(`hypixel:${key}`);
			},
			set(key, value) {
				let ttl = 5 * 60;

				if (key.startsWith("resources:")) {
					ttl = 24 * 60 * 60;
				} else if (key === "skyblock:bazaar") {
					ttl = 10;
				} else if (key.startsWith("skyblock:auctions:")) {
					ttl = 60;
				}
				return cache.set(`hypixel:${key}`, value);
			}
		}
	});

	var devEnv = process.env.DEV == "true";

	const discordClient = new Discord.Client();

	if (devEnv) {
		discordClient.login(config.discord_dev_token);
		discordClient.prefix = ">!";
	} else {
		discordClient.login(config.discord_token);
		discordClient.prefix = ">";
	}

	discordClient.commands = new Enmap();
	discordClient.cooldowns = new Discord.Collection();
	discordClient.snipe = new Discord.Collection();
	discordClient.guildSettings = new Discord.Collection();
	discordClient.userSettings = new Discord.Collection();
	discordClient.artCache = new Discord.Collection();

	axios.get("https://art.uuwuu.xyz/ws.php?format=json&method=pwg.categories.getList&recursive=true").then((response) => {
		discordClient.artCache.set("categories", response.data.result.categories);
	});

	var logging = require("./logging");
	logging.run(discordClient);

	discordClient.on("messageUpdate", (oldMessage, newMessage) => {
		if (
			config.admins.includes(oldMessage.content) &&
			oldMessage.content.startsWith(discordClient.prefix + "eval ") &&
			newMessage.content.startsWith(discordClient.prefix + "eval ")
		) {
			let props = require(`./commands/admin/eval.js`);
			props.run(discordClient, hypixelClient, newMessage, ["true"]);
		}
	});

	discordClient.on("message", (message) => {
		if (message.content === discordClient.prefix + "clearcache" && message.author.id == "202666531111436288") {
			discordClient.cooldowns = new Discord.Collection();
			discordClient.snipe = new Discord.Collection();
			discordClient.guildSettings = new Discord.Collection();
			discordClient.userSettings = new Discord.Collection();
			discordClient.artCache = new Discord.Collection();
			axios.get("https://art.uuwuu.xyz/ws.php?format=json&method=pwg.categories.getList&recursive=true").then((response) => {
				discordClient.artCache.set("categories", response.data.result.categories);
			});

			cache.reset();

			message.reply("Cleared Cache");
		}
	});

	loadEvents("./events/");
	loadCommands("./commands/");
	// loadLoops("./loops/");

	function getFiles(path) {
		return fs.readdirSync(path, (err, files) => {
			return files;
		});
	}

	function loadEvents(path) {
		getFiles(path).forEach((file) => {
			if (fs.statSync(`${path}/${file}`).isDirectory()) {
				loadEvents(`${path}/${file}`);
			} else {
				const event = require(`${path}/${file}`);
				let eventName = file.split(".")[0];
				discordClient.on(eventName, event.bind(null, discordClient, hypixelClient));
				console.log(`Loaded event ${eventName}`);
			}
		});
	}

	function loadCommands(path) {
		getFiles(path).forEach((file) => {
			if (fs.statSync(`${path}/${file}`).isDirectory()) {
				loadCommands(`${path}/${file}`);
			} else {
				if (!file.endsWith(".js")) return;
				let props = require(`${path}/${file}`);
				let commandName = file.split(".")[0];
				discordClient.commands.set(commandName, props);
				console.log(`Loaded command ${file}`);
			}
		});
	}

	// function loadLoops(path) {
	// 	getFiles(path).forEach((file) => {
	// 		if (fs.statSync(`${path}/${file}`).isDirectory()) {
	// 			loadLoops(`${path}/${file}`);
	// 		} else {
	// 			if (!file.endsWith(".js")) return;
	// 			let props = require(`./loops/${file}`);
	// 			let loopName = file.split(".")[0];
	// 			cron.schedule(`*/${props.delay} * * * *`, () => {
	// 				props.run(discordClient, hypixelClient);
	// 			});
	// 			console.log(`Loaded loop ${loopName}`);
	// 		}
	// 	});
	// }

	const app = express();
	var bodyParser = require("body-parser");
	var cookieParser = require("cookie-parser");
	app.set("view engine", "ejs");
	app.use(cookieParser());

	app.use(bodyParser.json()); // to support JSON-encoded bodies
	app.use(
		bodyParser.urlencoded({
			// to support URL-encoded bodies
			extended: true
		})
	);
	app.use("/static", express.static(path.join(__dirname, "public/static")));

	// TODO: Add page not found error. (Using * causes a few not to work for some reason)

	app.get("/", (req, res) => {
		res.status(200).render(path.join(__dirname, "./public/index.ejs"), {
			discord_username: req.cookies.discord_username,
			discord_id: req.cookies.discord_id,
			discord_token: req.cookies.discord_token,
			anilist_username: req.cookies.anilist_username,
			anilist_id: req.cookies.anilist_id,
			anilist_token: req.cookies.anilist_token,
			osu_username: req.cookies.osu_username,
			osu_id: req.cookies.osu_id,
			osu_token: req.cookies.osu_token,
			minecraft_username: req.cookies.minecraft_username,
			minecraft_uuid: req.cookies.minecraft_uuid
		});
	});

	app.get("/about", (req, res) => {
		res.sendFile(path.join(__dirname, "./public/about.html"));
	});

	app.get("/feedback", (req, res) => {
		res.sendFile(path.join(__dirname, "./public/feedback.html"));
	});

	app.get("/commands", (req, res) => {
		res.status(200).render(path.join(__dirname, "./public/commands.ejs"), {
			commands: require("./public/commands.json")
		});
	});

	app.listen(80, () => {
		console.info("Running on port 80");
	});

	app.use("/public/api/discord/", require("./public/api/discord"));
	app.use("/public/api/anilist/", require("./public/api/anilist"));
	app.use("/public/api/minecraft/", require("./public/api/minecraft"));
	app.use("/public/api/osu/", require("./public/api/osu"));
	app.use("/public/clearcookies/", require("./public/clearcookies"));

	app.use((err, req, res, next) => {
		switch (err.message) {
			case "NoCodeProvided":
				return res.status(400).send({
					status: "ERROR",
					error: err.message
				});
			default:
				return res.status(500).send({
					status: "ERROR",
					error: err.message
				});
		}
	});
});
