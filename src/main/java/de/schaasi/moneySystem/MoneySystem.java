package de.schaasi.moneySystem;

import de.schaasi.moneySystem.command.*;
import de.schaasi.moneySystem.listener.ConnectListener;
import de.schaasi.moneySystem.sql.MySQL;
import de.schaasi.moneySystem.sql.SQLGetter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class MoneySystem extends JavaPlugin {

    private static MoneySystem instance;

    public MySQL SQL;
    public SQLGetter data;

    private static File file;
    private static YamlConfiguration yamlConfiguration;

    @Override
    public void onEnable() {
        instance = this;
        this.SQL = new MySQL();
        this.data = new SQLGetter(this);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.options().copyDefaults(true);
        yamlConfiguration.addDefault("database.host", "");
        yamlConfiguration.addDefault("database.port", "");
        yamlConfiguration.addDefault("database.database", "");
        yamlConfiguration.addDefault("database.username", "");
        yamlConfiguration.addDefault("database.password", "");

        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.exists()) {
            SQL.loadFromConfig();
            try {
                SQL.connect();
            } catch (SQLException | ClassNotFoundException e) {
                Bukkit.getLogger().info("Database not connected");
            }
            if (SQL.isConnected()) {
                Bukkit.getLogger().info("Database is connected");
                data.createTable();
            }
        } else {
            Bukkit.getLogger().info("Config file not exist");
        }
        getServer().getPluginManager().registerEvents(new ConnectListener(), this);
        getCommand("moneylog").setExecutor(new MoneyLogCommand());
        getCommand("money").setExecutor(new MoneyCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("moneysystem").setExecutor(new MoneySystemCommand());
        getCommand("testdata").setExecutor(new TestDataCommand());

    }

    @Override
    public void onDisable() {
        SQL.disconnect();
    }

    public static MoneySystem getInstance() {
        return instance;
    }

    public SQLGetter getData() {
        return data;
    }

    public static YamlConfiguration getYamlConfiguration() {
        return yamlConfiguration;
    }
}
