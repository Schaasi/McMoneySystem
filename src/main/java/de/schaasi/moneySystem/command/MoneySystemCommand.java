package de.schaasi.moneySystem.command;

import com.google.gson.JsonParser;
import de.schaasi.moneySystem.MoneySystem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.UUID;

public class MoneySystemCommand implements CommandExecutor {

    private HashMap<String, String> resetMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("database")) {
                if (args[1].equalsIgnoreCase("status")) {
                    if (sender.hasPermission("moneysystem.moneysystem.database.status")) {
                        if (MoneySystem.getInstance().SQL.isConnected()) {
                            sender.sendMessage("§aDatabase connected.");
                        } else {
                            sender.sendMessage("§cDatabase not connected.");
                        }
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                } else if (args[1].equalsIgnoreCase("connect")) {
                    if (sender.hasPermission("moneysystem.moneysystem.database.connect")) {
                        if (!MoneySystem.getInstance().SQL.isConnected()) {
                            try {
                                MoneySystem.getInstance().SQL.connect();
                                MoneySystem.getInstance().data.createTable();
                                Bukkit.getOnlinePlayers().forEach(player -> {
                                    MoneySystem.getInstance().data.createPlayer(player);
                                });
                            } catch (ClassNotFoundException | SQLException e) {
                                e.printStackTrace();
                            }
                            if (MoneySystem.getInstance().SQL.isConnected()) {
                                sender.sendMessage("§aDatabase is now connected.");
                            } else {
                                sender.sendMessage("§cDatabase not connected.");
                            }
                        } else {
                            sender.sendMessage("§aDatabase already connected.");
                        }
                    }
                } else if (args[1].equalsIgnoreCase("disconnect")) {
                    if (sender.hasPermission("moneysystem.moneysystem.database.disconnect")) {
                        if (MoneySystem.getInstance().SQL.isConnected()) {
                            MoneySystem.getInstance().SQL.disconnect();
                            sender.sendMessage("§cDatabase disconnected.");
                        } else {
                            sender.sendMessage("§cDatabase already disconnected.");
                        }
                    }
                }
            } else if (args[0].equalsIgnoreCase("reset")) {
                if (sender.hasPermission("moneysystem.moneysystem.reset")) {
                    if (MoneySystem.getInstance().SQL.isConnected()) {
                        if (sender instanceof Player) {
                            if (args[1].equalsIgnoreCase("all")) {
                                if (args.length == 2) {
                                    if (!resetMap.containsKey(((Player) sender).getPlayerListName())) {
                                        sender.sendMessage("§cWenn du dir sicher bist gebe §e/moneysystem reset all confirm §c ein, §e/moneysystem reset all cancel §c um abzubrechen");
                                        resetMap.put(((Player) sender).getPlayerListName(), "all");
                                    } else {
                                        sender.sendMessage("§cBeende zuerst deine aktuelle Bestätigung.");
                                    }
                                } else if (args.length == 3) {
                                    if (args[2].equalsIgnoreCase("confirm")) {
                                        if (resetMap.containsKey(((Player) sender).getPlayerListName())) {
                                            if (resetMap.get(((Player) sender).getPlayerListName()).equalsIgnoreCase("all")) {
                                                MoneySystem.getInstance().data.emptyTable();
                                                resetMap.remove(((Player) sender).getPlayerListName());
                                                MoneySystem.getInstance().SQL.disconnect();
                                                sender.sendMessage("§aPlugin zurückgesetzt.");
                                            }
                                        }
                                    } else if (args[2].equalsIgnoreCase("cancel")) {
                                        resetMap.remove(((Player) sender).getPlayerListName());
                                        sender.sendMessage("§aVorgang abgebrochen.");
                                    }
                                }
                            } else if (args[1].equalsIgnoreCase("player")) {
                                String playerName = args[2];
                                if (Bukkit.getPlayer(playerName) == null) {
                                    UUID uuid = null;
                                    try {
                                        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                                        InputStreamReader reader = new InputStreamReader(url.openStream());
                                        String uuidString = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
                                        uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" +
                                                uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                                        uuid = UUID.fromString(uuidString);
                                    } catch (Exception ignored) {
                                    }
                                    if (uuid != null) {
                                        if (args.length == 3) {
                                            if (!resetMap.containsKey(((Player) sender).getPlayerListName())) {
                                                if (MoneySystem.getInstance().data.exists(uuid)) {
                                                    sender.sendMessage("§cNutze §e/moneysystem reset player " + playerName + " confirm §cum den Vorgang zu bestätigen" +
                                                            " oder §e/moneysystem reset player " + playerName + " cancel §cum abzubrechen.");
                                                    resetMap.put(((Player) sender).getPlayerListName(), "player_" + playerName.toLowerCase());
                                                }
                                            } else {
                                                sender.sendMessage("§cBestätige zuerst deinen aktuellen Vorgang.");
                                            }
                                        } else if (args.length == 4) {
                                            if (resetMap.containsKey(((Player) sender).getPlayerListName())) {
                                                if (resetMap.get(((Player) sender).getPlayerListName()).equalsIgnoreCase("player_" + args[2].toLowerCase())) {
                                                    if (args[3].equalsIgnoreCase("confirm")) {
                                                        MoneySystem.getInstance().data.remove(uuid);
                                                        resetMap.remove(((Player) sender).getPlayerListName());
                                                        sender.sendMessage("§aDatenbankeinträge von " + playerName + " wurden gelöscht.");
                                                    } else if (args[3].equalsIgnoreCase("cancel")) {
                                                        resetMap.remove(((Player) sender).getPlayerListName());
                                                        sender.sendMessage("§cVorgang abgebrochen.");
                                                    } else {
                                                        sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
                                                    }
                                                } else {
                                                    sender.sendMessage("§cBestätige zuerst deinen aktuellen Vorgang.");
                                                }
                                            } else {
                                                sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
                                            }
                                        } else {
                                            sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
                                        }
                                    } else {
                                        sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                                    }
                                } else {
                                    sender.sendMessage("§cDer Spieler muss offline sein.");
                                }
                            } else {
                                sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
                            }
                        }
                    } else {
                        sender.sendMessage("§cBitte kontaktiere einen Administrator.");
                    }
                } else {
                    sender.sendMessage("§cDazu hast du keine Rechte.");
                }
            } else if (args[0].equalsIgnoreCase("help")) {
                if (sender.hasPermission("moneysystem.moneysystem.help")) {
                    sender.sendMessage("§cMoneySystem Help");
                    sender.sendMessage("§e/moneysystem help");
                    sender.sendMessage("§e/money");
                    sender.sendMessage("§e/money balance <Spieler>");
                    sender.sendMessage("§e/money add <Spieler> <Betrag>");
                    sender.sendMessage("§e/money remove <Spielr> <Betrag>");
                    sender.sendMessage("§e/moneylog <Seite>");
                    sender.sendMessage("§e/moneylog <Spieler> <Seite>");
                    sender.sendMessage("§e/moneylog update <Spieler> <Seite>/all");
                    sender.sendMessage("§e/moneysystem database status/connect/disconnect");
                    sender.sendMessage("§e/moneysystem reset all");
                    sender.sendMessage("§e/moneysystem reset player <Spieler>");
                }
            }
        } else {
            sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
        }
        return true;
    }
}
