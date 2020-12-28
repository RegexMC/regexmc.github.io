const Discord = require('discord.js');
const { Client } = require('@zikeji/hypixel');
const https = require('https');
const { request } = require('http');
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
exports.run = (discordClient, hypixelClient, message, args) => {
    
}