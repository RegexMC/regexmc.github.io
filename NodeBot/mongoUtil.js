const MongoClient = require('mongodb').MongoClient;
const config = require('./config.json');
const url = `mongodb+srv://regex:${config.mongo_password}@cluster0.sswc3.mongodb.net/bot?retryWrites=true&w=majority`;

var _db;

module.exports = {
    connectToServer: function (callback) {
        MongoClient.connect(url, {
            useNewUrlParser: true
        }, function (err, client) {
            if (err) {
                console.log(err);
            } else {
                _db = client.db('bot');
            }
            return callback(err);
        });
    },

    /**
     * @returns {Db} db
     */
    getDb: function () {
        return _db;
    }
};