const Discord = require("discord.js");
const {
    Client
} = require("@zikeji/hypixel");

module.exports = {
    //probs doesnt work lol, havent tested 
    /**
     * Gets the message by id
     * @param {Discord.Client} discordClient Discord client
     * @param {Discord.Channel|Number|String} channel Channel to retrieve message ID from (ID or object)
     * @param {Number|String} messageId Message ID
     * @returns {Discord.Message} Message with the id
     */
    getMessageById: function (discordClient, channel, messageId) {
        if (channel instanceof Discord.Channel) {
            return channel.messages.fetch(messageId).then(msg => {
                return msg;
            });
        } else {
            return this.getChannelById(discordClient, messageId).messages.fetch(messageId).then(msg => {
                return msg;
            });
        }
    },

    /**
     * Gets the channel by id
     * @param {Discord.Client} discordClient Discord client
     * @param {Number|String} id Channel ID
     * @returns {Discord.Channel} Channel with the id
     */
    getChannelById: function (discordClient, id) {
        return discordClient.channels.cache.get(id);
    },

    //todo- redo this lol, the format desc is a mess
    /**
     * Formats `time` to `DD:HH:MM`|`MM:SS`
     * @param {String|Number} time Time in minutes|seconds
     * @param {"m"|"s"} [format=m] Whether to parse from minutes or seconds
     * @returns {String} `time` formatted as `DD:HH:MM`|`MM:SS`
     */
    formatTime: function (time, format) {
        if (!format || format == "m") {
            var days = Math.floor(time / 24 / 60) + "";
            var hours = Math.floor(time / 60 % 24) + "";
            var minutes = Math.floor(time % 60) + "";

            if (days.length == 1) days = "0" + days;
            if (hours.length == 1) hours = "0" + hours;
            if (minutes.length == 1) minutes = "0" + minutes;

            return days + ":" + hours + ":" + minutes;
        } else if (format == "s") {
            return ((time - (time %= 60)) / 60).toFixed(0) + (9 < time.toFixed(0) ? ":" : ":0") + time.toFixed(0);
        }
    },

    /**
     * Sets the author for the embed based on the message's author.
     * @param {Discord.Message} message 
     * @param {Discord.MessageEmbed} embed 
     * @returns {Discord.MessageEmbed} The embed with the author set to the message author
     */
    getAuthor: function (message, embed) {
        return embed.setAuthor(message.author.username, message.author.avatarURL());
    },

    /**
     * Sorts a map ascending or descending by its keys or values
     * @param {Map} map
     * @param {"keys"|"values"} item
     * @param {"asc"|"desc"} method
     * @returns {Map} Sorted map
     */
    sortMap: function (map, item, method) {
        if (item == "keys") {
            if (method == "asc") {
                return new Map([...map.entries()].sort());
            } else if (method == "desc") {
                return new Map([...map.entries()].sort().reverse());
            }
        } else if (item == "values") {
            if (method == "asc") {
                return new Map([...map.entries()].sort((a, b) => a[1] - b[1]));
            } else if (method == "desc") {
                return new Map([...map.entries()].sort((a, b) => b[1] - a[1]));
            }
        }
        return null;
    }
}