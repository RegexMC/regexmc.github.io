const os = require('os');
const osutils = require('os-utils');
const Discord = require('discord.js');
const {
    Client
} = require('@zikeji/hypixel');
const config = require('../../config.json');
const utils = require('../../utils');
const osu = require('node-os-utils');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = (discordClient, hypixelClient, message, args) => {
    var embed = new Discord.MessageEmbed();
    embed.setTitle("System Info");
    embed = utils.getAuthor(message, embed);

    const total = Object.values(os.cpus()[0].times).reduce(
        (acc, tv) => acc + tv, 0
    );
    const usage = process.cpuUsage();
    const currentCPUUsage = (usage.user + usage.system) * 1000;

    embed.addField("Uptime", utils.formatTime(os.uptime() / 60), false);
    embed.addField("CPU Usage", "temp", false);
    embed.addField("Memory Usage", `${utils.roundTo(os.freemem()/(1024 * 1024), 0)}/${utils.roundTo(os.totalmem()/(1024 * 1024), 0)}`, false);
    embed.addField("Bot Uptime", utils.formatTime(osutils.processUptime() / 60), false);
    embed.addField("Bot CPU Usage", `${((currentCPUUsage / total)/1000).toFixed(1)}%`, false);
    embed.addField("Bot Memory Usage", `${process.memoryUsage().heapUsed / (1024 * 1024)}MB`, false);
    //^^ bot mem usage wrong

    message.reply({
        embed
    });

    /*var cpu = osu.cpu;
    cpu.usage()
        .then(info => {
            console.log(info)
        });*/
    //^^ innacurate
}