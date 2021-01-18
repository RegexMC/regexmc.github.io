const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const config = require('../config.json');
const utils = require('../utils');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = (discordClient, hypixelClient, message, args) => {
    //just using logs channel and dyno cause i can lol
    utils.getChannelById(discordClient, "713259200574390274").messages.fetch({
        limit: 1
    }).then(messages => {
        var msgs = Array.from(messages.values());
        message.channel.send(msgs[0].embeds[0]);
    });
}