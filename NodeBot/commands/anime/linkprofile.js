const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = async (discordClient, hypixelClient, message, args) => {
    var mongoUtil = require("../../mongoUtil");
    var db = mongoUtil.getDb();
    var userCollection = db.collection("users");

    const query = {
        discord_id: Number(message.author.id)
    }

    var user = await userCollection.findOne(query);

    if (user) {
        message.reply("You are currently linked to https://anilist.com/user/" + user.anilist_id + "\nTo change what anilist account you are linked to, open http://uuwuu.xyz/public/clearcookies and then re-login.");
    } else {
        message.reply("Link your anilist account at http://uuwuu.xyz");
    }
}