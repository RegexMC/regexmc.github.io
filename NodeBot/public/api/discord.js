const express = require('express');
const btoa = require('btoa');
const config = require('../../config.json');
const router = express.Router();
const axios = require('axios');
const DiscordOauth2 = require("discord-oauth2");

const catchAsync = fn => (
    (req, res, next) => {
        const routePromise = fn(req, res, next);
        if (routePromise.catch) {
            routePromise.catch(err => next(err));
        }
    }
);

router.get('/login', (req, res) => {
    res.redirect("https://discord.com/api/oauth2/authorize?client_id=778936290090942514&redirect_uri=http%3A%2F%2Fuuwuu.xyz%2Fpublic%2Fapi%2Fdiscord%2Fcallback&response_type=code&scope=identify");
//    res.redirect("https://discord.com/api/oauth2/authorize?client_id=778936290090942514&redirect_uri=http%3A%2F%2Flocalhost%2Fpublic%2Fapi%2Fdiscord%2Fcallback&response_type=code&scope=identify");
});

/**
 * Credit to MYNAMERYAN - wouldn't have got the requests working w/o him
 */
router.get('/callback', catchAsync(async (req, res) => {
    if (!req.query.code) throw new Error('NoCodeProvided');
    const oauth = new DiscordOauth2();

    // Get access token from code using client secret

    const exchangeParams = new URLSearchParams();
    exchangeParams.append("grant_type", "authorization_code");
    exchangeParams.append("code", req.query.code);
    exchangeParams.append("redirect_uri", "http://uuwuu.xyz/public/api/discord/callback");
   //exchangeParams.append("redirect_uri", "http://localhost/public/api/discord/callback");
    const exchangeOptions = {
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Authorization": `Basic ${btoa(`778936290090942514:${config.discord_secret}`)}`
        }
    };

    axios
        .post("https://discord.com/api/oauth2/token", exchangeParams, exchangeOptions)
        .then(({
            data
        }) => {
            // Get user ID and username using the token just received
            var token = data.access_token;

            res.cookie("discord_token", token, {
                httpOnly: true
            });

            oauth.getUser(token).then(response => {
                res.cookie("discord_username", response.username + "#" + response.discriminator, {
                    httpOnly: true
                });
                res.cookie("discord_id", response.id, {
                    httpOnly: true
                });

                res.redirect(`/`);
            }).catch((err) => {
                if (err.response) {
                    console.log("API Error", err.response.data);
                } else {
                    console.log("Request Error", err.message);
                }
                res.sendFile(path.join(__dirname, '../error.html'));
                return;
            });
        })
        .catch((err) => {
            if (err.response) {
                console.log("API Error", err.response.data);
            } else {
                console.log("Request Error", err.message);
            }
            res.sendFile(path.join(__dirname, '../error.html'));
            return;
        });
}));

module.exports = router;