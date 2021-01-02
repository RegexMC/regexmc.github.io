const cacheManager = require("cache-manager");
const {
    Client
} = require("@zikeji/hypixel");
const Discord = require('discord.js');
const fs = require('fs');
const Enmap = require("enmap");
const config = require('./config.json');
const cron = require('node-cron');

const cache = cacheManager.caching({
    store: 'memory',
    ttl: 5*60
});
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
const discordClient = new Discord.Client();

discordClient.login(config.discord_token);
discordClient.commands = new Enmap();

//not working w/ the file ??!?!?!? newMessage is null in it
discordClient.on('messageUpdate', (oldMessage, newMessage) => {
    if (oldMessage.author.id == "202666531111436288" && oldMessage.content.startsWith(">eval ") && newMessage.content.startsWith(">eval ")) {
        let props = require(`./commands/admin/eval.js`);
        props.run(discordClient, hypixelClient, newMessage, ["true"]);
    }
});

loadEvents("./events/");
loadCommands("./commands/");
loadLoops("./loops/");

function getFiles(path) {
    return fs.readdirSync(path, (err, files) => {
        return files
    });
}

function loadEvents(path) {
    getFiles(path).forEach(file => {
        const event = require(`${path}/${file}`);
        let eventName = file.split(".")[0];
        discordClient.on(eventName, event.bind(null, discordClient, hypixelClient));
        console.log(`Loaded event ${eventName}`);
    })
}

function loadCommands(path) {
    getFiles(path).forEach(file => {
        if (fs.statSync(`${path}/${file}`).isDirectory()) {
            loadCommands(`${path}/${file}`);
        } else {
            if (!file.endsWith(".js")) return;
            let props = require(`${path}/${file}`);
            let commandName = file.split(".")[0];
            discordClient.commands.set(commandName, props);
            console.log(`Loaded command ${file}`);
        }
    })
}

function loadLoops(path) {
    getFiles(path).forEach(file => {
        if (fs.statSync(`${path}/${file}`).isDirectory()) {
            loadLoops(`${path}/${file}`);
        } else {
            if (!file.endsWith(".js")) return;
            let props = require(`./loops/${file}`);
            let loopName = file.split(".")[0];
            cron.schedule(`*/${props.delay} * * * *`, () => {
                props.run(discordClient, hypixelClient);
            });
            console.log(`Loaded loop ${loopName}`);
        }
    })
}