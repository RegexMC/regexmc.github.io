const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const config = require('../config.json');
const fetch = require("node-fetch");

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = (discordClient, hypixelClient, message, args) => {
    fetch(`https://osu.ppy.sh/api/get_user?u=${message.content.trim().substr(">osu ".length)}&k=${config.osu_api_key}`).then((response) => response.json())
        .then((data) => {
            var user = data[0];
            var embed = new Discord.MessageEmbed();
            embed.setAuthor(message.author.username, message.author.avatarURL());
            embed.setTitle(user.username);
            embed.setURL(`https://osu.ppy.sh/users/${user.user_id}`);
            embed.setThumbnail(`http://s.ppy.sh/a/${user.user_id}`);
            embed.addField("PP", user.pp_raw, false);
            embed.addField("Global Ranking", `#${user.pp_rank}`, false);
            embed.addField("Country Ranking", `#${user.pp_country_rank}`, true);
            embed.addField("Time Played", `${user.total_seconds_played}`, false);
            message.channel.send({
                embed
            });
        }).catch((err) => {
            message.reply("Something went wrong. Invalid user?");
        });
}