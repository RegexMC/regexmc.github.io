const express = require('express');
const router = express.Router();

const catchAsync = fn => (
    (req, res, next) => {
        const routePromise = fn(req, res, next);
        if (routePromise.catch) {
            routePromise.catch(err => next(err));
        }
    }
);

router.get('/', catchAsync(async (req, res) => {
    res.cookie('discord_token', '', {
        maxAge: 0
    });
    res.cookie('anilist_token', '', {
        maxAge: 0
    });
    res.cookie('discord_id', '', {
        maxAge: 0
    });
    res.cookie('anilist_id', '', {
        maxAge: 0
    });
    res.cookie('discord_username', '', {
        maxAge: 0
    });
    res.cookie('anilist_username', '', {
        maxAge: 0
    });
    res.cookie('linked', '', {
        maxAge: 0
    });
    res.redirect('http://uuwuu.xyz/');
}));

module.exports = router;