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

public class PayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!MoneySystem.getInstance().SQL.isConnected()) {
            sender.sendMessage("§cBitte kontaktiere einen Administrator.");
            return true;
        }
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (args.length != 2) {
            sender.sendMessage("§cNutze §e/moneysystem help §cfür hilfe.");
            return true;
        }
        if (!sender.hasPermission("moneysystem.pay")) {
            sender.sendMessage("§cDazu hast du keine Rechte.");
            return true;
        }
        String playerName = args[0];
        long amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            amount = -1;
        }
        if (amount < 0) {
            sender.sendMessage("§cDer Betrag muss im positiven Bereich sein und eine ganze Zahl sein.");
            return true;
        }
        if (playerName.equalsIgnoreCase("*")) {
            if (sender.hasPermission("moneysystem.pay.*")) {
                sender.sendMessage("§cDazu hast du keine Rechte.");
                return true;
            }
            if ((Bukkit.getOnlinePlayers().size() - 1) * amount > MoneySystem.getInstance().data.getMoney(player.getUniqueId())) {
                sender.sendMessage("§cDu hast nicht genug Geld.");
                return true;
            }
            long finalAmount = amount;
            Bukkit.getOnlinePlayers().forEach(player1 -> {
                if (!player.getName().equalsIgnoreCase(player1.getName())) {
                    MoneySystem.getInstance().data.removeMoney(player.getUniqueId(), finalAmount);
                    MoneySystem.getInstance().data.addMoney(player1.getUniqueId(), finalAmount);
                    player.sendMessage("§aDu hast §e" + player1.getName() + " §2" + finalAmount + "$ §agegeben.");
                    player1.sendMessage("§e" + player.getName() + " §ahat dir §2" + finalAmount + "$ §agegeben.");
                    MoneySystem.getInstance().data.addTransaction(player.getName(), player.getUniqueId(), player1.getName(), player1.getUniqueId(), finalAmount);
                }
            });
        } else {
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
            if (!MoneySystem.getInstance().data.exists(uuid)) {
                sender.sendMessage("§cDieser Spieler war noch nie auf dem Server.");
                return true;
            }
            if (amount > MoneySystem.getInstance().data.getMoney(player.getUniqueId())) {
                sender.sendMessage("§cDu hast nicht genug Geld.");
                return true;
            }
            MoneySystem.getInstance().data.removeMoney(player.getUniqueId(), amount);
            MoneySystem.getInstance().data.addMoney(uuid, amount);
            MoneySystem.getInstance().data.addTransaction(player.getName(), player.getUniqueId(), playerName, uuid, amount);
            player.sendMessage("§aDu hast §e" + playerName + " §2" + amount + "$ §agegeben.");
            if (Bukkit.getPlayer(playerName) != null) {
                Bukkit.getPlayer(playerName).sendMessage("§e" + player.getName() + " §ahat dir §2" + amount + "$ §agegeben.");
            }
        }
        return true;
    }
}
