const Discord = require('discord.js');
const {
  Client
} = require('@zikeji/hypixel');
const utils = require('../../utils');
const request = require('request');

/**
 * @param {Discord.Client} discordClient
 * @param {Client} hypixelClient
 * @param {Discord.Message} message
 * @param {String[]} args
 */
exports.run = async (discordClient, hypixelClient, message, args) => {
  let username;
  let id;

  if (args.length == 0) {

    var mongoUtil = require("../../mongoUtil");
    var db = mongoUtil.getDb();
    var userCollection = db.collection("users");

    const query = {
      discord_id: Number(message.author.id)
    }

    var user = await userCollection.findOne(query);

    if (user) {
      id = user.anilist_id;
    } else {
      message.reply("Please enter a username or link your anilist profile using `>linkprofile`")
    }
  }

  if (args.length > 0) username = args[0];

  var query = `
        {
            User(${id ? "id" : "name"}: ${id ? id : '"' + username + '"'}) {
              name
              about
              avatar {
                large
                medium
              }
              bannerImage
              siteUrl
              statistics {
                anime {
                  minutesWatched
                  count
                  episodesWatched
                  genres {
                    count
                    genre
                  }
                }
                manga {
                  count
                  chaptersRead
                  volumesRead
                  genres {
                    count
                    genre
                  }
                }
              }
              options {
                profileColor
              }
            }
            anime: MediaListCollection(${id ? "userId" : "userName"}: ${id ? id : '"' +  username+ '"'}, type: ANIME) {
              lists {
                status
                entries {
                  media {
                    genres
                  }
                }
              }
            }
            manga: MediaListCollection(${id ? "userId" : "userName"}: ${id ? id : '"' +  username+ '"'}, type: MANGA) {
              lists {
                status
                entries {
                  media {
                    genres
                  }
                }
              }
            }
          }
        `;

  var options = {
    uri: 'https://graphql.anilist.co',
    method: 'POST',
    json: {
      query
    }
  };

  request(options, function (error, response, body) {
    if (!error && response.statusCode == 200) {
      //reuse for list cmd
      var animeListPlanning;
      var animeListCompleted;
      var animeListCurrent;
      var animeListDropped;
      var animeListPaused;

      for (i = 0; i < body.data.anime.lists.length; i++) {
        var list = body.data.anime.lists[i];
        var status = list.status;

        if (status == "PLANNING") {
          animeListPlanning = list;
        } else if (status == "COMPLETED") {
          animeListCompleted = list;
        } else if (status == "CURRENT") {
          animeListCurrent = list;
        } else if (status == "DROPPED") {
          animeListDropped = list;
        } else if (status == "PAUSED") {
          animeListPaused = list;
        }
      }

      var animeListCompletedGenresMap = new Map();
      animeListCompleted.entries.forEach(entry => {
        entry.media.genres.forEach(genre => {
          if (animeListCompletedGenresMap.has(genre)) {
            animeListCompletedGenresMap.set(genre, (animeListCompletedGenresMap.get(genre) + 1));
          } else {
            animeListCompletedGenresMap.set(genre, 1);
          }
        })
      });
      animeListCompletedGenresMap = utils.sortMap(animeListCompletedGenresMap, "values", "desc");
      var user = body.data.User;

      var name = user.name;
      var about = user.about == null ? "" : user.about;
      var avatar = user.avatar.large;
      var bannerImage = user.bannerImage;
      var siteUrl = user.siteUrl;
      var statistics = user.statistics;

      var animeCount = statistics.anime.count;
      var timeWatched = statistics.anime.minutesWatched;
      var episodesWatched = statistics.anime.episodesWatched;

      var topThreeAnimeGenres = "";
      var i = 0;

      animeListCompletedGenresMap.forEach((value, key) => {
        if (i < 3) {
          topThreeAnimeGenres += `${key}: ${value}\n`;
          i++;
        }
      });
      topThreeAnimeGenres = topThreeAnimeGenres.trim();

      // TODO: change this to be completed manga only as well
      var mangaCount = statistics.manga.count;
      var chaptersRead = statistics.manga.chaptersRead;
      var volumesRead = statistics.manga.volumesRead;
      var mangaGenres = statistics.manga.genres;

      var sortedMangaGenres = mangaGenres.sort((a, b) => (a.count > b.count) ? -1 : 1);
      var topThreeMangaGenres = "";
      for (i = 0; i < 3; i++) {
        topThreeMangaGenres += `${sortedMangaGenres[i].genre}: ${sortedMangaGenres[i].count}\n`;
      }
      topThreeMangaGenres = topThreeMangaGenres.trim();

      var profileColor = user.options.profileColor;

      var embed = new Discord.MessageEmbed();
      embed = utils.getAuthor(message, embed);
      embed.setColor(getColorFromString(profileColor));
      embed.setTitle(name);
      embed.setURL(siteUrl);
      embed.setDescription(about);
      embed.setThumbnail(avatar);
      embed.setImage(bannerImage);

      embed.addField("List Entries", animeCount + mangaCount, false);
      embed.addField("Anime Watched", animeListCompleted.entries.length, true);
      embed.addField("Eps. Watched", episodesWatched, true);
      embed.addField("Time Watched", utils.formatTime(timeWatched), true);

      embed.addField("Manga Read", mangaCount, true);
      embed.addField("Chapters Read", chaptersRead, true);
      embed.addField("Volumes Read", volumesRead, true);

      embed.addField("Most Watched Genres", topThreeAnimeGenres, true);
      embed.addField("Most Read Genres", topThreeMangaGenres, true);

      message.channel.send({
        embed
      });
    } else {
      message.reply("Something went wrong! Invalud username?");
    }
  });
}

/**
 * @param {String} color 
 */
function getColorFromString(color) {
  switch (color) {
    case "blue":
      return 4044018;
    case "purple":
      return 12608511;
    case "green":
      return 5032529;
    case "orange":
      return 15697946;
    case "red":
      return 14758707;
    case "pink":
      return 16555478;
    default:
      return 6781844; //gray
  };
}