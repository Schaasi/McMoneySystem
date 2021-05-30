package de.schaasi.moneySystem.command;

import com.google.gson.JsonParser;
import de.schaasi.moneySystem.MoneySystem;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class MoneyLogCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!MoneySystem.getInstance().SQL.isConnected()) {
            sender.sendMessage("§cBitte kontaktiere einen Administrator.");
            return true;
        }

        if (!sender.hasPermission("moneysystem.moneylog")) {
            sender.sendMessage("§cBitte kontaktiere einen Administrator.");
            return true;
        }

        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 2) {
            String playerName = args[0];
            int site;
            try {
                site = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                site = 1;
            }
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
            if (uuid == null) {
                sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                return true;
            }
            List<Integer> sendTransactions = MoneySystem.getInstance().data.getTransactionsFromPlayerSend(uuid);
            List<Integer> receivedTransactions = MoneySystem.getInstance().data.getTransactionsFromPlayerReceive(uuid);
            Integer[] transactionsSend = new Integer[sendTransactions.size()];
            transactionsSend = sendTransactions.toArray(transactionsSend);
            Integer[] transactionsReceived = new Integer[receivedTransactions.size()];
            transactionsReceived = receivedTransactions.toArray(transactionsReceived);
            Integer[] transactions = new Integer[sendTransactions.size() + receivedTransactions.size()];
            transactions = sendTransactions.toArray(transactions);
            for (int i = 0; i < transactionsReceived.length; i++) {
                transactions[i + sendTransactions.size()] = transactionsReceived[i];
            }
            Arrays.sort(transactions);
            Arrays.sort(transactionsSend);
            Arrays.sort(transactionsReceived);
            int sites = transactions.length / 10;
            if (transactions.length % 10 > 0) {
                sites++;
            }
            if (sites == 0) {
                sender.sendMessage("§cDieser Spieler hat keine Transaktionen.");
                return true;
            }
            if (site > sites) {
                sender.sendMessage("§cDieser Spieler hat nur §e" + sites + " Seiten§c.");
                return true;
            }
            sender.sendMessage("§a-----§eMoney Log §c" + playerName + " §bSeite " + site + "/" + sites + "§a-----");
            for (int i = transactions.length - 1 - ((site - 1) * 10); i > transactions.length - 1 - (site * 10) && i >= 0; i--) {
                TextComponent messageInOut, messagePlayerName, messageAmount, messageDate, messageSeparator, message;
                messageSeparator = new TextComponent(" | ");
                messageSeparator.setColor(ChatColor.GRAY);
                messageAmount = new TextComponent(MoneySystem.getInstance().data.getTransactionAmount(transactions[i]) + "$");
                messageAmount.setColor(ChatColor.GREEN);
                messageDate = new TextComponent(MoneySystem.getInstance().data.getTransactionDate(transactions[i]));
                messageDate.setColor(ChatColor.AQUA);
                if (Arrays.binarySearch(transactionsSend, transactions[i]) > -1) {
                    messageInOut = new TextComponent("-> ");
                    messageInOut.setColor(ChatColor.GREEN);
                    if (MoneySystem.getInstance().data.getTransactionNameReceiver(transactions[i]).startsWith("[")) {
                        messagePlayerName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameReceiver(transactions[i]));
                        messagePlayerName.setColor(ChatColor.YELLOW);
                    } else {
                        messagePlayerName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameReceiver(transactions[i]));
                        messagePlayerName.setColor(ChatColor.YELLOW);
                        messagePlayerName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                "https://de.namemc.com/search?q="
                                        + MoneySystem.getInstance().data.getTransactionUUIDReceiver(transactions[i])));
                    }
                    transactionsSend[Arrays.binarySearch(transactionsSend, transactions[i])] = -2;
                    Arrays.sort(transactionsSend);
                } else {
                    messageInOut = new TextComponent("<- ");
                    messageInOut.setColor(ChatColor.RED);
                    if (MoneySystem.getInstance().data.getTransactionNameSender(transactions[i]).startsWith("[")) {
                        messagePlayerName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameSender(transactions[i]));
                        messagePlayerName.setColor(ChatColor.YELLOW);
                    } else {
                        messagePlayerName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameSender(transactions[i]));
                        messagePlayerName.setColor(ChatColor.YELLOW);
                        messagePlayerName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                "https://de.namemc.com/search?q="
                                        + MoneySystem.getInstance().data.getTransactionUUIDSender(transactions[i])));
                    }
                }
                message = new TextComponent();
                message.addExtra(messageInOut);
                message.addExtra(messagePlayerName);
                message.addExtra(messageSeparator);
                message.addExtra(messageAmount);
                message.addExtra(messageSeparator);
                message.addExtra(messageDate);
                player.spigot().sendMessage(message);
            }
            TextComponent message, messagePart, messageLeft, messageRight, messagePlayerName, messageSite;
            messagePart = new TextComponent("Money Log ");
            messagePart.setColor(ChatColor.YELLOW);
            messagePlayerName = new TextComponent(playerName);
            messagePlayerName.setColor(ChatColor.RED);
            messageSite = new TextComponent(" Seite " + site + "/" + sites);
            messageSite.setColor(ChatColor.AQUA);
            if (site > 1) {
                messageLeft = new TextComponent("<----");
                messageLeft.setColor(ChatColor.GREEN);
                messageLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moneylog " + playerName + " " + (site - 1)));
            } else {
                messageLeft = new TextComponent("-----");
                messageLeft.setColor(ChatColor.GREEN);
            }
            if (site < sites) {
                messageRight = new TextComponent("---->");
                messageRight.setColor(ChatColor.GREEN);
                messageRight.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moneylog " + playerName + " " + (site + 1)));
            } else {
                messageRight = new TextComponent("-----");
                messageRight.setColor(ChatColor.GREEN);
            }
            message = new TextComponent();
            message.addExtra(messageLeft);
            message.addExtra(messagePart);
            message.addExtra(messagePlayerName);
            message.addExtra(messageSite);
            message.addExtra(messageRight);
            player.spigot().sendMessage(message);
        } else if (args.length == 1) {
            int site;
            try {
                site = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                site = 1;
            }
            if (MoneySystem.getInstance().data.getMaxTransactionID() <= 0) {
                sender.sendMessage("§cEs sind keine Transaktionen vorhanden.");
                return true;
            }
            int sites = MoneySystem.getInstance().data.getMaxTransactionID() / 10;
            if (MoneySystem.getInstance().data.getMaxTransactionID() % 10 > 0) {
                sites++;
            }
            if (site <= 0) {
                sender.sendMessage("§cDie Seite muss größer als 0 sein.");
                return true;
            }
            if (site > sites) {
                sender.sendMessage("§cEs gibt nur §e" + sites + " Seiten§c.");
                return true;
            }
            sender.sendMessage("§a----------§eMoney Log §bSeite " + site + "/" + sites + "§a----------");
            for (int i = MoneySystem.getInstance().data.getMaxTransactionID() - ((site - 1) * 10); i > MoneySystem.getInstance().data.getMaxTransactionID() - (site * 10) && i > 0; i--) {
                TextComponent messageArrow, messageSenderName, messageReceiverName, messageAmount, messageDate, messageSeparator, message;
                messageSeparator = new TextComponent(" | ");
                messageSeparator.setColor(ChatColor.GRAY);
                messageAmount = new TextComponent(MoneySystem.getInstance().data.getTransactionAmount(i) + "$");
                messageAmount.setColor(ChatColor.GREEN);
                messageDate = new TextComponent(MoneySystem.getInstance().data.getTransactionDate(i));
                messageDate.setColor(ChatColor.AQUA);
                messageArrow = new TextComponent(" -> ");
                messageArrow.setColor(ChatColor.DARK_GRAY);
                messageSenderName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameSender(i));
                messageSenderName.setColor(ChatColor.YELLOW);
                messageSenderName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        "https://de.namemc.com/search?q="
                                + MoneySystem.getInstance().data.getTransactionUUIDSender(i)));
                messageReceiverName = new TextComponent(MoneySystem.getInstance().data.getTransactionNameReceiver(i));
                messageReceiverName.setColor(ChatColor.YELLOW);
                messageReceiverName.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
                        "https://de.namemc.com/search?q="
                                + MoneySystem.getInstance().data.getTransactionUUIDReceiver(i)));
                message = new TextComponent();
                message.addExtra(messageSenderName);
                message.addExtra(messageArrow);
                message.addExtra(messageReceiverName);
                message.addExtra(messageSeparator);
                message.addExtra(messageAmount);
                message.addExtra(messageSeparator);
                message.addExtra(messageDate);
                player.spigot().sendMessage(message);
            }
            TextComponent message, messagePart, messageLeft, messageRight, messageSite;
            messagePart = new TextComponent("Money Log ");
            messagePart.setColor(ChatColor.YELLOW);
            messageSite = new TextComponent(" Seite " + site + "/" + sites);
            messageSite.setColor(ChatColor.AQUA);
            if (site > 1) {
                messageLeft = new TextComponent("<----");
                messageLeft.setColor(ChatColor.GREEN);
                messageLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moneylog " + (site - 1)));
            } else {
                messageLeft = new TextComponent("-----");
                messageLeft.setColor(ChatColor.GREEN);
            }
            if (site < sites) {
                messageRight = new TextComponent("---->");
                messageRight.setColor(ChatColor.GREEN);
                messageRight.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/moneylog " + (site + 1)));
            } else {
                messageRight = new TextComponent("-----");
                messageRight.setColor(ChatColor.GREEN);
            }
            message = new TextComponent();
            message.addExtra(messageLeft);
            message.addExtra(messagePart);
            message.addExtra(messageSite);
            message.addExtra(messageRight);
            player.spigot().sendMessage(message);
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("update")) {
                String playerName = args[1];
                int site;
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
                if (uuid == null) {
                    sender.sendMessage("§cDiesen Spieler gibt es nicht.");
                    return true;
                }
                List<Integer> sendTransactions = MoneySystem.getInstance().data.getTransactionsFromPlayerSend(uuid);
                List<Integer> receivedTransactions = MoneySystem.getInstance().data.getTransactionsFromPlayerReceive(uuid);
                Integer[] transactionsSend = new Integer[sendTransactions.size()];
                transactionsSend = sendTransactions.toArray(transactionsSend);
                Integer[] transactionsReceived = new Integer[receivedTransactions.size()];
                transactionsReceived = receivedTransactions.toArray(transactionsReceived);
                Integer[] transactions = new Integer[sendTransactions.size() + receivedTransactions.size()];
                transactions = sendTransactions.toArray(transactions);
                for (int i = 0; i < transactionsReceived.length; i++) {
                    transactions[i + sendTransactions.size()] = transactionsReceived[i];
                }
                Arrays.sort(transactions);
                Arrays.sort(transactionsSend);
                Arrays.sort(transactionsReceived);
                if (args[2].equalsIgnoreCase("all")) {
                    if (sender.hasPermission("moneysystem.moneylog.update.all")) {
                        sender.sendMessage("§7Update Money Log von " + playerName + "...");
                        sender.sendMessage("§7Bitte warte einen Moment...");
                        for (int i = 0; i < transactions.length; i++) {
                            MoneySystem.getInstance().data.transactionsUpdatePlayerNames(transactions[i]);
                        }
                        sender.sendMessage("§7Update beendet.");
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                } else {
                    if (sender.hasPermission("moneysystem.moneylog.update")) {
                        try {
                            site = Integer.parseInt(args[2]);
                        } catch (NumberFormatException e) {
                            site = 1;
                        }
                        int sites = transactions.length / 10;
                        if (transactions.length % 10 > 0) {
                            sites++;
                        }
                        if (sites != 0) {
                            if (site <= sites) {
                                sender.sendMessage("§7Update Money Log Seite " + site + " von " + playerName + "...");
                                sender.sendMessage("§7Bitte warte kurz...");
                                for (int i = transactions.length - 1 - ((site - 1) * 10); i > transactions.length - 1 - (site * 10) && i >= 0; i--) {
                                    MoneySystem.getInstance().data.transactionsUpdatePlayerNames(transactions[i]);
                                }
                                sender.sendMessage("§7Update beendet.");
                            } else {
                                sender.sendMessage("§cDer Spieler hat nur " + sites + " Seiten.");
                            }
                        } else {
                            sender.sendMessage("§cDie Seite muss größer als 0 sein.");
                        }
                    } else {
                        sender.sendMessage("§cDazu hast du keine Rechte.");
                    }
                }
            }
        } else {
            sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
        }
        return true;
    }
}

