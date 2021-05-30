package de.schaasi.moneySystem.listener;

import de.schaasi.moneySystem.MoneySystem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class ConnectListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (MoneySystem.getInstance().SQL.isConnected()) {
            MoneySystem.getInstance().data.createPlayer(player);
        }
        if (player.hasPermission("moneysystem.notify")) {
            if (!MoneySystem.getInstance().SQL.isConnected()) {
                player.sendMessage("§f[§2MoneySystem§f] §cDatabase not connected.");
            }
        }
    }
}