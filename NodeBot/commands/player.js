const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const https = require('https');
const {
    request
} = require('http');
const fetch = require('node-fetch');
const usernamesFile = './data/hypixel/usernames.json';
const usernames = require('../data/hypixel/usernames.json');
const fs = require('fs');
const utils = require('../utils');

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
            var playerName = player.playername;
            var networkExp = player.networkExp;
            var firstLogin = player.firstLogin;
            var lastLogin = player.lastLogin;
            var karma = player.karma;
            var nonLegacyAps = player.achievementPoints;
            var mostRecentGame = player.mostRecentGameType;
            var rank = utils.getPlayerRank(player);
            rank = rank == 'MVP_PLUS_PLUS' ? "[MVP++]" : rank;
            rank = rank == 'MVP_PLUS' ? "[MVP+]" : rank;
            rank = rank == 'VIP_PLUS' ? "[VIP+]" : rank;
            rank = rank == 'VIP' ? "[VIP]" : rank;
            rank = rank == null ? "" : rank;

            hypixelClient.status.uuid(id).then(playerStatus => {
                var online = playerStatus.online;

                var currentGameType = playerStatus.gameType;
                var currentGameMode = playerStatus.mode;
                var currentGameMap = playerStatus.map;

                var embed = new Discord.MessageEmbed();

                embed.setTitle((rank == "" ? "" : rank + " ") + playerName + (playerName.endsWith("s") ? "'" : "'s") + " stats");
                embed.setURL("https://plancke.io/hypixel/player/stats/" + playerName);
                embed = utils.getAuthor(message, embed);
                embed.setThumbnail("https://crafatar.com/avatars/" + id);
                embed.setColor(getRankColor(rank));
                embed.addField("Network Exp", networkExp, true);
                embed.addField("Level", utils.roundTo(utils.getHypixelLevel(networkExp), 2), true);
                embed.addField("Karma", karma, true);
                embed.addField("First Login", utils.formatEpochTime(firstLogin), true);
                embed.addField("Last Login", utils.formatEpochTime(lastLogin), true);
                embed.addField("Status", online ? "Online" : "Offline", true);

                //todo: add aps

                if (online) {
                    embed.addField("Currently Playing (Mode, Type, Map)", `${currentGameMode}, ${currentGameType}, ${currentGameMap}`, false);
                } else {
                    embed.addField("Last Played", `${mostRecentGame}`, false);
                }
                // ^ make it so no map if in SB, make it so theres no undefineds, etc.

                var end = new Date();
                message.channel.send("Took " + (end - start) + "ms", {
                    embed
                });
            });
        });
    }
}

function getRankColor(rank) {
    switch (rank) {
        case "[MVP++]":
            return 16755200;
        case "[MVP+]", "[MVP]":
            return 5636095;
        case "[VIP+]", "[VIP]":
            return 5635925;
    }
}