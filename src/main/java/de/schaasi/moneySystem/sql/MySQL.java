package de.schaasi.moneySystem.sql;

import de.schaasi.moneySystem.MoneySystem;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    private Connection connection;

    public void loadFromConfig() {
        try {
            host = MoneySystem.getInstance().getYamlConfiguration().getString("database.host");
            port = MoneySystem.getInstance().getYamlConfiguration().getString("database.port");
            database = MoneySystem.getInstance().getYamlConfiguration().getString("database.database");
            username = MoneySystem.getInstance().getYamlConfiguration().getString("database.username");
            password = MoneySystem.getInstance().getYamlConfiguration().getString("database.password");
        } catch (NullPointerException e) {
            System.out.println("Error while loading from Config");
        }
    }

    public boolean isConnected() {
        boolean connected;
        if (connection == null) {
            connected = false;
        } else {
            connected = true;
        }
        return (connected);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!isConnected()) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
            Bukkit.getOnlinePlayers().forEach(player -> {
                if (!MoneySystem.getInstance().data.exists(player.getUniqueId())) {
                    MoneySystem.getInstance().data.createPlayer(player);
                }
            });
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
