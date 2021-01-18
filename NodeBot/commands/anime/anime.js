const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const utils = require('../../utils');
const request = require('request');
const fs = require('fs');
const path = require('path');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = (discordClient, hypixelClient, message, args) => {
    if (args.length != 0) {
        var query;
        var queryBody = fs.readFileSync(path.resolve("./data/anilist/queries/anime"), "utf8")

        if (args.length == 1 && /[0-9]+/.test(args[0])) {
            //id
            query = `{Media(id: ${args[0]}) {` + queryBody;
        } else {
            //anime name
            query = `{Media(search: "${message.content.substring(">anime ".length)}", type: ANIME) {` + queryBody;
        }

        query = query + '}}';

        var options = {
            uri: 'https://graphql.anilist.co',
            method: 'POST',
            json: {
                query
            }
        };

        request(options, function (error, response, body) {
            if (!error && response.statusCode == 200) {
                var embed = formEmbed(body.data.Media);
                embed = utils.getAuthor(message, embed);
                message.reply({
                    embed
                });
            }
        });
    } else {
        message.reply("Please include an anime or anime id");
    }
}

/**
 * @param {{id: number, title: {english: String, romaji: String}, type: String, status: String, averageScore: Number, favourites: Number, description: String, episodes: Number, chapters: Number, volumes: Number, startDate: {year: Number, month: Number, day: Number}, endDate: {year: Number, month: Number, day: Number}, airingSchedule: {nodes: [{}]}, coverImage: {large: String, color: String}}} media 
 * @returns {Discord.MessageEmbed}
 */
function formEmbed(media) {
    var id = media.id;
    var englishTitle = media.title.english;
    var romajiTitle = media.title.romaji;

    var embed = new Discord.MessageEmbed();

    if (romajiTitle == null) {
        embed.setTitle(englishTitle);
    } else {
        if (englishTitle == null) {
            embed.setTitle(romajiTitle)
        } else {
            embed.setTitle(romajiTitle);
            embed.setDescription(`${englishTitle}\n${media.description.substring(0, 203)+"\u2026"}`);
        }
    }
    if (embed.description == null) embed.setDescription(media.description.substring(0, 203) + "\u2026");

    embed.setURL(`https://anilist.co/anime/${id}/`)
    embed.setColor(`0x${media.coverImage.color.substring(1)}`);
    embed.setThumbnail(media.coverImage.large);

    embed.addField("Status", media.status, true);
    embed.addField("Started Airing", `${media.startDate.year}/${media.startDate.month}/${media.startDate.day}`, true);
    embed.addField(media.status != "RELEASING" ? "Finished Airing" : "Finishes Airing", `${media.endDate.year}/${media.endDate.month}/${media.endDate.day}`, true);

    embed.addField("Score", media.averageScore, true);
    embed.addField("Favourites", media.favourites, true);
    embed.addField("Episodes", media.episodes, true);

    var usernames = JSON.parse(fs.readFileSync(path.resolve("./data/anilist/usernames.json"), "utf8"));
    var users = usernames.users;
    var self = users.filter(u => u.id === message.author.id);
    if (self) {
        var username = self[0].username;

    }
    //TODO: if they have anilist profile linked, get their stats like watched etc. and add it 
    //      make some options for the link- in this case whether to show this information
    return embed;
}