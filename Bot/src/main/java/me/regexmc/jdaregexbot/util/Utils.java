package me.regexmc.jdaregexbot.util;

import com.google.gson.JsonParser;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.BotMain;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Utils {
    //region Internal
    public static File compressImage(String attachmentPath, String imageFileName, int suffix, float quality) throws IOException {
        InputStream is = new FileInputStream(attachmentPath + imageFileName);
        String newName = imageFileName;
        int suffixCharCount = String.valueOf(suffix).length();
        if (imageFileName.endsWith("_compressed_" + (suffix - 1) + ".jpg")) {
            newName = imageFileName.substring(0, imageFileName.length() - (suffixCharCount + 16));
        }
        OutputStream os = new FileOutputStream(attachmentPath + newName + "_compressed_" + suffix + ".jpg");

        BufferedImage image = ImageIO.read(is);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");

        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();

        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        writer.write(null, new IIOImage(image, null, null), param);

        is.close();
        os.close();
        ios.close();
        writer.dispose();

        return new File(attachmentPath + newName + "_compressed_" + suffix + ".jpg");
    }

    public static <K, V> Map<K, V> convertToTreeMap(Map<K, V> hashMap) {

        // Return the TreeMap
        return hashMap
                // Get the entries from the hashMap
                .entrySet()

                // Convert the map into stream
                .stream()

                // Now collect the returned TreeMap
                .collect(
                        Collectors

                                // Using Collectors, collect the entries
                                // and convert it into TreeMap
                                .toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue,
                                         newValue)
                                                -> newValue,
                                        TreeMap::new));
    }

    public static void log(Object in, ErrorTypes type) {
        String timeStamp = new SimpleDateFormat("yyyy_MM_dd").format(new java.util.Date());
        ArrayList<String> logHistory = new ArrayList<>();

        String logFile;

        if (BotMain.logFile == null) {
            File folder = new File(BotMain.config.get("path_run") + "logs\\");
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles != null) {
                if (listOfFiles.length != 0) {
                    for (File file : listOfFiles) {
                        if (file.isFile()) {
                            logHistory.add(file.getName());
                        }
                    }
                }
            }

            int i = 0;
            while (logHistory.contains(timeStamp + "_" + i + ".log")) {
                i++;
            }

            logFile = BotMain.config.get("path_run") + "logs\\" + timeStamp + "_" + i + ".log";
            BotMain.logFile = logFile;
        } else {
            logFile = BotMain.logFile;
        }


        int callerLineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();
        String callerClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        String exactTime = new SimpleDateFormat("yyyy.MM.dd HH.mm.ss.SSS").format(new java.util.Date());

        String toLog;

        if (in instanceof Exception) {
            toLog = type.print + " " + exactTime + " | " + callerClassName + " (" + callerLineNumber + ") ::: " + exceptionStacktraceToString((Exception) in);
        } else if (in instanceof String) {
            toLog = type.print + " " + exactTime + " | " + callerClassName + " (" + callerLineNumber + ") ::: " + in;
        } else {
            toLog = type.print + " " + exactTime + " | " + callerClassName + " (" + callerLineNumber + ") ::: " + in.toString();
        }

        appendToFile(logFile, toLog + "\n");
    }

    public static String exceptionStacktraceToString(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        ps.close();
        return baos.toString();
    }

    public enum ErrorTypes {
        ERROR("[ERROR]"),
        WARNING("[WARNING]"),
        INFO("[INFO]"),
        API("[API]");

        private final String print;

        ErrorTypes(String print) {
            this.print = print;
        }

    }

    public enum Emotes {
        LEFT_ARROW("U+2b05"),
        RIGHT_ARROW("U+27a1");

        private final String unicode;

        Emotes(String unicode) {
            this.unicode = unicode;
        }

        public String getUnicode() {
            return unicode;
        }

    }
    //endregion

    //region Hypixel
    public static TreeMap<Integer, int[]> getSeasons(JSONObject rankedObject) {
        TreeMap<Integer, int[]> seasonsMap = new TreeMap<>();

        Period diff = Period.between(LocalDate.of(2016, Month.JANUARY, 1), LocalDate.now());
        int years = diff.getYears() + 1;

        int start = -2;
        for (int i = 16; i < years + 16; i++) {
            for (int j = 1; j < 13; j++) {
                int season = start;
                start++;
                if (rankedObject.has("season_" + season)) {
                    JSONObject seasonXObj = rankedObject.getJSONObject("season_" + season);
                    seasonsMap.put(season, new int[]{(int) seasonXObj.get("rating"), (int) seasonXObj.get("pos")});
                }
            }
        }

        return seasonsMap;
    }

    public static boolean invalidUsername(String username) {
        return !Pattern.compile("^(\\w{3,16})$").matcher(username).find();
    }
    //endregion

    //region Discord
    public static boolean isAdmin(CommandEvent event) {
        return BotMain.admins.contains(event.getAuthor().getId());
    }

    public static boolean isCommandChannel(CommandEvent event) {
        return BotMain.commandChannels.contains(event.getChannel().getId());
    }

    public static boolean messageReplyChecks(MessageReceivedEvent e, CommandEvent event) {
        return e.getAuthor().equals(event.getAuthor())
                && e.getChannel().equals(event.getChannel())
                && !e.getMessage().equals(event.getMessage());
    }
    //endregion

    //region Embeds
    public static MessageEmbed invalidUsernameEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("**Invalid Username**");
        eb.setColor(RankUtils.Ranks.ADMIN.getColor());
        eb.addField("Invalid username", "Please enter a valid username", false);
        eb.setFooter("If using a UUID, please use a username. Support for UUIDS will come soon");
        return eb.build();
    }

    public static MessageEmbed gettingStatsEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Getting stats");
        eb.setColor(RankUtils.Ranks.ADMIN.getColor());
        eb.addField("...", "...", false);
        return eb.build();
    }
    //endregion

    //region File Utils
    public static Boolean exists(String path) {
        return org.apache.commons.io.FileUtils.getFile(path).exists();
    }

    public static void writeToFile(String path, String content) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            Utils.log("An error occurred writing to file.", Utils.ErrorTypes.ERROR);
            Utils.log(e, Utils.ErrorTypes.ERROR);
            e.printStackTrace();
        }
    }

    public static void appendToFile(String path, String content) {
        try {
            FileWriter writer = new FileWriter(path, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            Utils.log("An error occurred appending to file.", Utils.ErrorTypes.ERROR);
            Utils.log(e, Utils.ErrorTypes.ERROR);
            e.printStackTrace();
        }
    }

    public static String loadResourceAsString(String resource) throws IOException {
        InputStream in = BotMain.class.getResourceAsStream("/" + resource);
        return Utils.readAll(new BufferedReader(new InputStreamReader(in)));
    }

    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Path.of(path)));
    }

    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static File downloadAttachmentToFile(List<Message.Attachment> attachments, Message message) throws IOException {
        String attachmentsPath = "C:\\Users\\regex\\Desktop\\Development\\regexmc.github.io\\Bot\\run\\attachments\\";
        File attachmentFile = new File(attachmentsPath + message.getId() + "." + attachments.get(0).getFileExtension());
        FileUtils.copyURLToFile(
                new URL(attachments.get(0).getUrl()),
                attachmentFile);
        return attachmentFile;
    }

    public static int getFileSize(String path) throws IOException {
        return (int) (Files.size(Path.of(path)) / 1000);
    }

    public static long timeSinceLastEdit(String path) throws IOException {
        if (org.apache.commons.io.FileUtils.getFile(path).exists()) {
            FileTime fileTime = Files.getLastModifiedTime(Path.of(path));
            long currentTime = System.currentTimeMillis();
            long lastEdit = fileTime.toMillis();

            return currentTime - lastEdit;
        } else {
            return 0;
        }
    }
    //endregion

    //region JSON Utils
    public static JSONObject readJsonFromUrl(String url) {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        } catch (IOException | JSONException e) {
            return null;
        }
    }

    public static JSONObject readJsonFromFile(String path) throws IOException {
        return new JSONObject(JsonParser.parseString(Utils.readFile(path)).getAsJsonObject().toString());
    }
    //endregion

    //region Number Utils
    public static boolean isNumeric(String input) {
        return Pattern.compile("^[0-9]+$").matcher(input).find();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static long minutesToMs(int minutes) {
        return minutes * 60 * 1000;
    }

    public static int parseInt(Object o) {
        return (int) Double.parseDouble(o.toString());
    }

    public static double parseDouble(Object o) {
        return Double.parseDouble(o.toString());
    }

    public static long parseLong(Object o) {
        return Long.parseLong(o.toString());
    }
    //endregion

    public static String timeConvert(long time, String format) {
        String days = time / 24 / 60 + "";
        String hours = time / 60 % 24 + "";
        String minutes = time % 60 + "";
        if (format.equals("long")) {
            return days + " days " + hours + " hours " + minutes + " minutes";
        } else if (format.equals("default")) {
            if (days.length() == 1) days = "0" + days;
            if (hours.length() == 1) hours = "0" + hours;
            if (minutes.length() == 1) minutes = "0" + minutes;

            return days + ":" + hours + ":" + minutes;
        } else {
            return hours;
        }
    }

}
