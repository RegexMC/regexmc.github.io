package me.regexmc.jdaregexbot.commands.admin.imagemanipulation;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.BotMain;
import me.regexmc.jdaregexbot.util.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

public class CompressImageCommand extends Command {

    public CompressImageCommand() {
        this.name = "compressimage";
        this.cooldown = 10;
        this.ownerCommand = true;
        this.arguments = "<attachment>";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nCompresses the attachment image";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            String[] args = Arrays.copyOfRange(event.getMessage().getContentRaw().split(" "), 1, event.getMessage().getContentRaw().split(" ").length);

            int maxFileSizeInKb = 0;

            if (args.length < 1) {
                maxFileSizeInKb = Integer.MAX_VALUE;
            } else if (Utils.isNumeric(args[0])) {
                maxFileSizeInKb = Utils.parseInt(args[0]);
            }
            if (event.getMessage().getAttachments().size() > 0) {
                try {
                    String attachmentsPath = BotMain.config.get("path_attachments").toString();
                    File attachmentFile = Utils.downloadAttachmentToFile(event.getMessage().getAttachments(), event.getMessage());

                    if (attachmentFile.getAbsolutePath().endsWith(".png")) {
                        BufferedImage bufferedImage = ImageIO.read(new File(attachmentFile.getAbsolutePath()));
                        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
                        ImageIO.write(newBufferedImage, "jpg", new File(attachmentFile.getAbsolutePath()));
                    }


                    String originalFileName = event.getMessage().getId() + "." + event.getMessage().getAttachments().get(0).getFileExtension();
                    String newFileName = "";

                    float multiplier = 0.99f;

                    if (args.length == 2) {
                        try {
                            multiplier = Float.parseFloat(args[1]);
                        } catch (NumberFormatException e) {
                            event.reply("Invalid multiplier");
                        }
                    }

                    for (int i = 0; i < 100; i++) {
                        if (newFileName.isEmpty()) {
                            File newFile = Utils.compressImage(attachmentsPath, originalFileName, i, multiplier);
                            newFileName = newFile.getName();

                            int newFileSize = Utils.getFileSize(newFile.getAbsolutePath());
                            if (newFileSize < maxFileSizeInKb) {
                                event.reply(newFile, newFile.getName());
                                break;
                            }

                        } else {
                            File newFile = Utils.compressImage(attachmentsPath, newFileName, i, multiplier);
                            newFileName = newFile.getName();

                            int newFileSize = Utils.getFileSize(newFile.getAbsolutePath());
                            if (newFileSize < maxFileSizeInKb) {
                                event.reply(newFile, newFile.getName());
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    Utils.log(e, Utils.ErrorTypes.ERROR);
                    e.printStackTrace();
                    event.reply("Error downloading attachment.");
                }

                try {
                    File[] files = new File(BotMain.config.get("path_attachments").toString()).listFiles();
                    StringBuilder deletedFiles = new StringBuilder();
                    for (File file : files) {
                        if (file.isFile()) {
                            if (file.getName().startsWith(event.getMessage().getId()) && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
                                try {
                                    if (file.delete()) {
                                        deletedFiles.append(file.getAbsolutePath()).append("\n");
                                    }
                                } catch (SecurityException e) {
                                    Utils.log(e, Utils.ErrorTypes.ERROR);
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    Utils.log("Deleted " + deletedFiles.toString(), Utils.ErrorTypes.INFO);
                } catch (SecurityException e) {
                    Utils.log(e, Utils.ErrorTypes.ERROR);
                    e.printStackTrace();
                }
            } else {
                event.reply("Please attach an image");
            }

        }
    }
}
