package me.regexmc.jdaregexbot.commands.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.regexmc.jdaregexbot.util.Utils;
import org.json.JSONObject;

import java.util.Arrays;

public class ConvertCommand extends Command {

    public ConvertCommand() {
        this.name = "convert";
        this.cooldown = 5;
        this.ownerCommand = true;
        this.arguments = "too many. check website when i add it";
        this.help = "Cooldown: " + this.cooldown +
                " | Syntax: `" + this.arguments +
                "`\nConverts the input parameters (refer to help)";
        this.helpBiConsumer = (commandEvent, command) -> {
            if (Utils.isAdmin(commandEvent)) {
                commandEvent.reply(this.help);
            }
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        if (Utils.isCommandChannel(event)) {
            final String[] args = event.getArgs().split(" ");
            final var inches2 = args[2].equalsIgnoreCase("inches") || args[2].equalsIgnoreCase("\"");
            final var metres = args[1].equalsIgnoreCase("m") || args[1].equalsIgnoreCase("metres") || args[1].equalsIgnoreCase("meters");
            final var centimetres = args[1].equalsIgnoreCase("cm") || args[1].equalsIgnoreCase("centimetres") || args[1].equalsIgnoreCase("centimeters");
            final var metres2 = args[2].equalsIgnoreCase("m") || args[2].equalsIgnoreCase("metres") || args[2].equalsIgnoreCase("meters");
            final var inches = args[1].equalsIgnoreCase("inches") || args[1].equalsIgnoreCase("\"");
            final var feet2 = args[2].equalsIgnoreCase("feet") || args[2].equalsIgnoreCase("'");
            final var feet = args[1].equalsIgnoreCase("feet") || args[1].equalsIgnoreCase("'");
            final var kg = args[1].equalsIgnoreCase("kg");

            if (kg && args[2].equalsIgnoreCase("pounds")) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s kg is %s pounds", args[0], Utils.round(input * 2.2046226218, 3)));
                    return;

                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (kg && args[2].equalsIgnoreCase("grams")) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s kg is %s grams", args[0], Utils.round(input * 1000, 3)));
                    return;

                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (args[1].equalsIgnoreCase("pounds") && args[2].equalsIgnoreCase("kg")) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s pounds is %s kg", args[0], Utils.round(input / 2.2046226218, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (metres && inches2) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s metres is %s inches", args[0], Utils.round(input * 39.37, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (centimetres && inches2) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s centimetres is %s inches", args[0], Utils.round(input * 0.3937007874, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }

            if (metres2 && inches) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s inches is %s metres", args[0], Utils.round(input / 39.37, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (metres && feet2) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s metres is %s feet", args[0], Utils.round(input * 3.28, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (metres2 && feet) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s feet is %s metres", args[0], Utils.round(input / 3.28, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (centimetres && feet2) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s centimetres is %s feet", args[0], Utils.round(input / 30.48, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (inches && feet2) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s inches is %s feet", args[0], Utils.round(input / 12, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }
            if (inches2 && feet) {
                try {
                    double input = Double.parseDouble(args[0]);
                    event.reply(String.format("%s feet is %s inches", args[0], Utils.round(input * 12, 3)));
                    return;
                } catch (Exception e) {
                    event.reply("something went wrong");
                }
                return;
            }

            if (args[0].equalsIgnoreCase("currency")) {
                if (args.length > 3) {
                    String baseAmount = args[1];
                    String baseCurrency = args[2];
                    String resultCurrency = args[3];

                    JSONObject exchangeRatesJSON = Utils.readJsonFromUrl("https://api.exchangeratesapi.io/latest?base=" + baseCurrency.toUpperCase());
                    if (exchangeRatesJSON != null) {
                        try {
                            double result = Utils.round(Double.parseDouble(baseAmount) * exchangeRatesJSON.getJSONObject("rates").getDouble(resultCurrency.toUpperCase()), 2);
                            event.reply(baseAmount + " " + baseCurrency + " is " + result + " " + resultCurrency);
                            return;
                        } catch (Exception e) {
                            event.reply("Something went wrong");
                            return;
                        }
                    }
                } else {
                    event.reply("Format is: `!!convert currency 1.00 AUD USD`");
                    return;
                }
            }

            event.reply("cant convert that");
        }

    }
}
