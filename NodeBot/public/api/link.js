const express = require('express');
const config = require('../../config.json');
const router = express.Router();
const axios = require('axios');
const path = require('path');

const catchAsync = fn => (
    (req, res, next) => {
        const routePromise = fn(req, res, next);
        if (routePromise.catch) {
            routePromise.catch(err => next(err));
        }
    }
);

router.get('/', catchAsync(async (req, res) => {
    const discord_id = req.cookies.discord_id;
    const discord_username = req.cookies.discord_username;
    const discord_token = req.cookies.discord_token;

    const anilist_id = req.cookies.anilist_id;
    const anilist_username = req.cookies.anilist_username;
    const anilist_token = req.cookies.anilist_token;

    const DiscordOauth2 = require("discord-oauth2");
    const oauth = new DiscordOauth2();
    oauth.getUser(discord_token).then(response => {
        const discord_id_new = response.id;
        const discord_username_new = response.username + "#" + response.discriminator;

        if (discord_id == discord_id_new && discord_username == discord_username_new) {
            const userOptions = {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': 'Bearer ' + anilist_token
                }
            }

            axios.post("https://graphql.anilist.co", {
                query: `mutation {UpdateUser {id name}}`
            }, userOptions).then((response) => {
                var user = response.data.data.UpdateUser;

                const anilist_id_new = user.id;
                const anilist_username_new = user.name;

                if (anilist_id == anilist_id_new && anilist_username == anilist_username_new) {
                    res.cookie('discord_token', '', {
                        maxAge: 0
                    });
                    res.cookie('anilist_token', '', {
                        maxAge: 0
                    });
                    res.cookie("linked", "1", {
                        httpOnly: true
                    });

                    var mongoUtil = require("../../mongoUtil");
                    var db = mongoUtil.getDb();
                    var userCollection = db.collection("users");

                    const query = {
                        discord_id: Number(discord_id)
                    }

                    var user = userCollection.findOne(query);

                    var newUser = {
                        $set: {
                            discord_id: Number(discord_id),
                            anilist_id: Number(anilist_id),
                            minecraft_uuid: ""
                        }
                    }

                    if (!user) {
                        userCollection.insertOne(newUser, function (error, response_) {
                            if (error) throw errror;
                            console.log("Inserted user");
                            //discordClient.channels.cache.get("761205404251586591").sendMessage("Linked");
                        });
                    } else {
                        userCollection.updateOne(query, newUser, function (error, response_) {
                            if (error) throw errror;
                            console.log("Updated user");
                            //discordClient.channels.cache.get("761205404251586591").sendMessage("Linked");
                        });
                    }

                    res.redirect(`/public/api/link/success`);
                }
            }).catch((err) => {
                if (err.response) {
                    console.log("API Error", err.response.data);
                } else {
                    console.log("Request Error", err.message);
                }
                res.sendFile(path.join(__dirname, '../error.html'));
                return;
            });
        }
    }).catch((err) => {
        if (err.response) {
            console.log("API Error", err.response.data);
        } else {
            console.log("Request Error", err.message);
        }
        res.sendFile(path.join(__dirname, '../error.html'));
        return;
    });
}));

router.get("/success", catchAsync(async (req, res) => {
    console.log(this.test);
    res.sendFile(path.join(__dirname, '../success.html'));
}));

module.exports = router;
//todo: have index.js pass the discord obj here. when this is run use the disc object to send msg saying linked. temp store channel and guild id somewhere?