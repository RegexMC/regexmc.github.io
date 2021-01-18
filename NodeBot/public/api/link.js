const express = require('express');
const config = require('../../config.json');
const router = express.Router();
const axios = require('axios');
const path = require('path');


router.get('/', async function (req, res) {
    const discord_id = req.cookies.discord_id;
    const discord_username = req.cookies.discord_username;
    const discord_token = req.cookies.discord_token;

    const anilist_id = req.cookies.anilist_id;
    const anilist_username = req.cookies.anilist_username;
    const anilist_token = req.cookies.anilist_token;

    const DiscordOauth2 = require("discord-oauth2");
    const oauth = new DiscordOauth2();

    const discordUser = await oauth.getUser(discord_token)
    const discord_id_new = discordUser.id;
    const discord_username_new = discordUser.username + "#" + discordUser.discriminator;

    if (discord_id == discord_id_new && discord_username == discord_username_new) {
        const userOptions = {
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + anilist_token
            }
        }

        var anilistUser = await axios.post("https://graphql.anilist.co", {
            query: `mutation {UpdateUser {id name}}`
        }, userOptions);
        anilistUser = anilistUser.data.data.UpdateUser;

        const anilist_id_new = anilistUser.id;
        const anilist_username_new = anilistUser.name;

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

            var dataBaseUser = await userCollection.findOne(query);

            var newUser = {
                discord_id: Number(discord_id),
                anilist_id: Number(anilist_id),
                minecraft_uuid: ""
            }

            console.log(dataBaseUser);
            console.log(newUser);

            if (!dataBaseUser) {
                userCollection.insertOne(newUser, function (error, response_) {
                    if (error) throw error;
                    console.log("Inserted user");
                });
            } else {
                newUser = {
                    $set: {
                        discord_id: Number(discord_id),
                        anilist_id: Number(anilist_id),
                        minecraft_uuid: ""
                    }
                }
                userCollection.updateOne(query, newUser, function (error, response_) {
                    if (error) throw error;
                    console.log("Updated user");
                });
            }

            res.redirect(`/public/api/link/success`);
        }
    }
});
router.get("/success", async function (req, res) {
    res.sendFile(path.join(__dirname, '../success.html'));
});

module.exports = router;
//todo: have index.js pass the discord obj here. when this is run use the disc object to send msg saying linked. temp store channel and guild id somewhere?