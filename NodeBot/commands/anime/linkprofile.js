const Discord = require("discord.js");
const { Client } = require("@zikeji/hypixel");

module.exports = {
	cooldown: 5,
	admin: false,
	/**
	 * @param {Discord.Client} discordClient
	 * @param {Client} hypixelClient
	 * @param {Discord.Message} message
	 * @param {String[]} args
	 */
	async run(discordClient, hypixelClient, message, args) {
		var mongoUtil = require("../../mongoUtil");
		var db = mongoUtil.getDb();
		var userCollection = db.collection("users");

		const query = {
			discord_id: message.author.id
		};

		var user = await userCollection.findOne(query);

		if (user) {
			var embed = new Discord.MessageEmbed();
			embed.setColor(32768);
			embed.setTitle("You are currently linked to " + user.anilist_username);
			embed.setURL("https://anilist.com/user/" + user.anilist_username);
			embed.setDescription("To change what anilist account you are linked to, open http://uuwuu.xyz/public/clearcookies and then re-login");
			message.reply({ embed });
		} else {
			var embed = new Discord.MessageEmbed();
			embed.setColor(16753920);
			embed.setTitle("Link your anilist account at http://uuwuu.xyz");
			message.reply({ embed });
		}
	}
};
