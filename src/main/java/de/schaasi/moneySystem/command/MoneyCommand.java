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
import java.util.UUID;

public class MoneyCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (MoneySystem.getInstance().SQL.isConnected()) {
            if (args.length == 0) {
                if (sender.hasPermission("moneysystem.money")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        UUID uuid = player.getUniqueId();
                        player.sendMessage("§2Kontostand: §a" + MoneySystem.getInstance().data.getMoney(uuid) + "$");
                    } else {
                        sender.sendMessage("§cKonsolen dürfen das nicht.");
                    }
                } else {
                    sender.sendMessage("§cDazu hast du keine Rechte.");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("balance")) {
                    if (sender.hasPermission("moneysystem.money.balance")) {
                        String playerName = args[1];
                        UUID uuid = null;
                        if (Bukkit.getPlayer(playerName) != null) {
                            uuid = Bukkit.getPlayer(playerName).getUniqueId();
                        } else {
                            try {
                                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                                InputStreamReader reader = new InputStreamReader(url.openStream());
                                String uuidString = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
                                uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" +
                                        uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                                uuid = UUID.fromString(uuidString);
                            } catch (Exception ignored) {
                            }
                        }
                        if (uuid != null) {
                            if (MoneySystem.getInstance().data.exists(uuid)) {
                                sender.sendMessage("§e" + playerName + "`s §2Kontostand: " + MoneySystem.getInstance().data.getMoney(uuid));
                            } else {
                                sender.sendMessage("§cDieser Spieler war noch nie auf dem Server.");
                            }
                        } else {
                            sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                        }
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                }
            } else if (args.length == 3) {
                if (args[0].equalsIgnoreCase("add")) {
                    if (sender.hasPermission("moneysystem.money.add")) {
                        String playerName = args[1];
                        UUID uuid = null;
                        if (Bukkit.getPlayer(playerName) != null) {
                            uuid = Bukkit.getPlayer(playerName).getUniqueId();
                        } else {
                            try {
                                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                                InputStreamReader reader = new InputStreamReader(url.openStream());
                                String uuidString = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
                                uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" +
                                        uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                                uuid = UUID.fromString(uuidString);
                            } catch (Exception ignored) {
                            }
                        }
                        if (uuid != null) {
                            if (MoneySystem.getInstance().data.exists(uuid)) {
                                long amount;
                                try {
                                    amount = Integer.parseInt(args[2]);
                                } catch (NumberFormatException e) {
                                    amount = 0;
                                    sender.sendMessage("§cEs gab einen Fehler. Füge kein Geld hinzu.");
                                }
                                if (amount > 0) {
                                    MoneySystem.getInstance().data.addMoney(uuid, amount);
                                    sender.sendMessage("§e" + playerName + " §awurden §2" + amount + "$ §ahinzugefügt.");
                                    UUID uuid2 = UUID.fromString("ea805d40-e828-4d8d-8e64-e9fc8ac301cb");
                                    MoneySystem.getInstance().data.addTransaction("[ADD]", uuid2, playerName, uuid, amount);
                                }
                            } else {
                                sender.sendMessage("§cDieser Spieler war noch nie auf dem Server.");
                            }
                        } else {
                            sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                        }
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                } else if (args[0].equalsIgnoreCase("remove")) {
                    if (sender.hasPermission("moneysystem.money.remove")) {
                        String playerName = args[1];
                        UUID uuid = null;
                        if (Bukkit.getPlayer(playerName) != null) {
                            uuid = Bukkit.getPlayer(playerName).getUniqueId();
                        } else {
                            try {
                                URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
                                InputStreamReader reader = new InputStreamReader(url.openStream());
                                String uuidString = new JsonParser().parse(reader).getAsJsonObject().get("id").getAsString();
                                uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16) + "-" +
                                        uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                                uuid = UUID.fromString(uuidString);
                            } catch (Exception ignored) {
                            }
                        }
                        if (uuid != null) {
                            if (MoneySystem.getInstance().data.exists(uuid)) {
                                long amount;
                                try {
                                    amount = Integer.parseInt(args[2]);
                                } catch (NumberFormatException e) {
                                    sender.sendMessage("§cEs gab einen Fehler. Entferne kein Geld.");
                                    amount = 0;
                                }
                                if (amount > 0) {
                                    MoneySystem.getInstance().data.removeMoney(uuid, amount);
                                    sender.sendMessage("§e" + playerName + " §awurden §c" + amount + "$ §aentfernt.");
                                    UUID uuid2 = UUID.fromString("ea805d40-e828-4d8d-8e64-e9fc8ac301cb");
                                    MoneySystem.getInstance().data.addTransaction(playerName, uuid, "[REMOVE]", uuid2, amount);
                                }
                            } else {
                                sender.sendMessage("§cDieser Spieler war noch nie auf dem Server.");
                            }
                        } else {
                            sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                        }
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                } else {
                    sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
                }
            } else {
                sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
            }
        } else {
            sender.sendMessage("§cBitte kontaktiere einen Administrator.");
        }
        return true;
    }
}
