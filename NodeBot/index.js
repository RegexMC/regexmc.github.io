const cacheManager = require('cache-manager');
const {
    Client
} = require('@zikeji/hypixel');
const Discord = require('discord.js');
const fs = require('fs');
const Enmap = require('enmap');
const config = require('./config.json');
const cron = require('node-cron');
const express = require('express');
const path = require('path');

const cache = cacheManager.caching({
    store: 'memory',
    ttl: 5 * 60,
    max: 500
});

var mongoUtil = require('./mongoUtil');

mongoUtil.connectToServer(function (err, client) {
    if (err) console.log(err);
    console.log("Connected to database");
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

discordClient.on('message', message => {
    if (message.content === ">clearcache" && message.author.id == "202666531111436288") {
        cache.reset();
        message.reply("Cleared Cache");
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

const app = express();
var cookieParser = require('cookie-parser');
app.set('view engine', 'ejs');
app.use(cookieParser());
app.use('/static', express.static(path.join(__dirname, 'public/static')));

// TODO: Add page not found error. (Using * causes a few not to work for some reason)

app.get('/', (req, res) => {
    res.status(200).render(path.join(__dirname, './public/index.ejs'), {
        discord_username: req.cookies.discord_username,
        discord_id: req.cookies.discord_id,
        discord_token: req.cookies.discord_token,
        anilist_username: req.cookies.anilist_username,
        anilist_id: req.cookies.anilist_id,
        anilist_token: req.cookies.anilist_token,
        linked: req.cookies.linked
    });
});

app.get('/about', (req, res) => {
    res.sendFile(path.join(__dirname, './public/about.html'));
});

app.get('/commands', (req, res) => {
    res.sendFile(path.join(__dirname, './public/commands.html'));
});

app.listen(80, () => {
    console.info('Running on port 80');
});

app.use('/public/api/discord/', require('./public/api/discord'));
app.use('/public/api/anilist/', require('./public/api/anilist'));
app.use('/public/api/link/', require('./public/api/link'));
app.use('/public/clearcookies/', require('./public/clearcookies'));

app.use((err, req, res, next) => {
    switch (err.message) {
        case 'NoCodeProvided':
            return res.status(400).send({
                status: 'ERROR',
                error: err.message,
            });
        default:
            return res.status(500).send({
                status: 'ERROR',
                error: err.message,
            });
    }
});