package de.schaasi.moneySystem.command;

import de.schaasi.moneySystem.MoneySystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TestDataCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        for (int i = 0; i < 11000; i++) {
            MoneySystem.getInstance().data.addTransaction(player.getName(), player.getUniqueId(), player.getName(), player.getUniqueId(), 0);
            player.sendMessage("§e" + i);
        }
        return true;
    }
}
