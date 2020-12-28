module.exports = (discordClient, hypixelClient, message) => {
    if (message.author.bot) return;
    if (!message.content.startsWith(">")) return;
    const args = message.content.slice(1).trim().split(/ +/g);
    const command = args.shift().toLowerCase();
    const cmd = discordClient.commands.get(command);
    if (!cmd) return;
    cmd.run(discordClient, hypixelClient, message, args);
};