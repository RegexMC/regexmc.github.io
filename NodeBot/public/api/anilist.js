const express = require('express');
const config = require('../../config.json');
const router = express.Router();
const axios = require('axios');

const catchAsync = fn => (
    (req, res, next) => {
        const routePromise = fn(req, res, next);
        if (routePromise.catch) {
            routePromise.catch(err => next(err));
        }
    }
);

router.get('/login', (req, res) => {
    res.redirect("https://anilist.co/api/v2/oauth/authorize?client_id=4221&redirect_uri=http%3A%2F%2Fuuwuu.xyz%2Fpublic%2Fapi%2Fanilist%2Fcallback&response_type=code")
});

/**
 * Credit to MYNAMERYAN - wouldn't have got the requests working w/o him
 */
router.get('/callback', catchAsync(async (req, res) => {
    if (!req.query.code) throw new Error('NoCodeProvided');

    const params = new URLSearchParams();
    params.append("grant_type", "authorization_code");
    params.append("client_id", 4221);
    params.append("client_secret", config.anilist_secret);
    params.append("code", req.query.code);
    params.append("redirect_uri", "http://uuwuu.xyz/public/api/anilist/callback");

    const options = {
        headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            'Accept': 'application/json'
        }
    };

    axios
        .post("https://anilist.co/api/v2/oauth/token", params, options)
        .then(({
            data
        }) => {
            var token = data.access_token;

            const userOptions = {
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': 'Bearer ' + token
                }
            }

            res.cookie("anilist_token", token, {
                httpOnly: true
            });

            axios.post("https://graphql.anilist.co", {
                query: `mutation {UpdateUser {id name}}`
            }, userOptions).then((response) => {
                var user = response.data.data.UpdateUser;

                res.cookie("anilist_username", user.name, {
                    httpOnly: true
                });

                res.cookie("anilist_id", user.id, {
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

module.exports = router;