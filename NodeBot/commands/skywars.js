const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const fetch = require('node-fetch');
const usernamesFile = './cache/usernames.json';
const usernames = require('../cache/usernames.json');
const fs = require('fs');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = async (discordClient, hypixelClient, message, args) => {
    if (args.length > 0) {
        var start = new Date();
        var id;

        if (/[0-9a-fA-F]{8}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{4}-?[0-9a-fA-F]{12}/.test(args[0])) {
            id = args[0];
        } else {
            var usernamesCache = usernames.players.find(obj => obj.username === args[0]);
            if (usernamesCache != null) {
                id = usernamesCache.uuid;
            } else {
                await fetch(`https://api.mojang.com/users/profiles/minecraft/${args[0]}`).then(data => data.json())
                    .then(player => {
                        id = player.id;
                        usernames.players.push({
                            "uuid": player.id,
                            "username": args[0]
                        });
                    });
                fs.writeFile(usernamesFile, JSON.stringify(usernames), function writeJSON(err) {
                    if (err) return console.log(err);
                });
            }
        }

        hypixelClient.player.uuid(id).then(player => {
            var stats = player.stats.SkyWars;
            var selectedIcon = stats.selected_prestige_icon;
            var level = getSwLevel(stats.skywars_experience);

            // provided by Shmeado
            var pres_icons = {"default": "⋆",
            "iron_prestige": "✙", "gold_prestige": "❤", "diamond_prestige": "☠",
            "emerald_prestige": "✦", "sapphire_prestige": "✌", "ruby_prestige": "❦",
            "crystal_prestige": "✵", "opal_prestige": "❣", "amethyst_prestige": "☯",
            "rainbow_prestige": "✺", "mythic_prestige": "ಠ_ಠ", "favor_icon": "⚔",
            "angel_1": "★", "angel_2": "☆", "angel_3": "⁕", "angel_4": "✶", "angel_5": "✳", "angel_6": "✴",
            "angel_7": "✷", "angel_8": "❋", "angel_9": "✼", "angel_10": "❂", "angel_11": "❁", "angel_12": "☬",
            "omega_icon": "Ω"}

            const embed = new Discord.MessageEmbed();
            embed.setTitle("Skywars Stats");
            embed.setColor(6170266);
            embed.setFooter("Developed by @RegexMC");
            embed.setThumbnail("https://crafatar.com/avatars/" + id);
            embed.setAuthor(`[${level.toFixed(2)}${pres_icons[selectedIcon]}] ` + args[0], "https://plancke.io/favicon.ico", "https://plancke.io/hypixel/player/stats/" + args[0]);
            embed.setDescription("Overall");

            embed.addField("Kills", stats.kills, true);
            embed.addField("Deaths", stats.deaths, true);
            embed.addField("K/D", (stats.kills / stats.deaths).toFixed(3), true);

            embed.addField("Wins", stats.wins, true);
            embed.addField("Losses", stats.losses, true);
            embed.addField("W/L", (stats.wins / stats.losses).toFixed(3), true);

            embed.addField("Experience", stats.skywars_experience, true);
            embed.addField("Heads", stats.heads, true);
            embed.addField("Time", stats.time_played, true);

            embed.addField("Shards", stats.shard, true);
            embed.addField("Opals", stats.opals, true);
            embed.addField("Corruption", calculateCorruption(stats), true);

            var end = new Date();
            message.channel.send("Took " + (end - start) + "ms", {
                embed
            });
        })
    }
}

/**
 * @author pollefeys
 * @param {*} stats 
 */
function calculateCorruption(stats) {
    let totalChance = stats.angel_of_death_level;
    if (stats.packages.includes("favor_of_the_angel")) {
        totalChance++;
    }
    if (stats.angels_offering == 1) {
        totalChance++;
    }
    return totalChance;
}

function getSwLevel(xp) {
    var xps = [0, 20, 70, 150, 250, 500, 1000, 2000, 3500, 6000, 10000, 15000];
    if (xp >= 15000) {
        return (xp - 15000) / 10000 + 12;
    } else {
        for (i = 0; i < xps.length; i++) {
            if (xp < xps[i]) {
                return 1 + i + (xp - xps[i - 1]) / (xps[i] - xps[i - 1]);
            }
        }
    }
}