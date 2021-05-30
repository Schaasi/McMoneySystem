package de.schaasi.moneySystem.sql;

import com.google.gson.JsonParser;
import de.schaasi.moneySystem.MoneySystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SQLGetter {

    private MoneySystem plugin;

    public SQLGetter(MoneySystem plugin) {
        this.plugin = plugin;
    }

    public void createTable() {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS money "
                    + "(NAME VARCHAR(100),UUID VARCHAR(100),MONEY BIGINT(100),PRIMARY KEY (NAME))");
            ps.executeUpdate();
            PreparedStatement ps2 = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS transaction "
                    + "(ID INT(100),NAMESENDER VARCHAR(100),UUIDSENDER VARCHAR(100),NAMERECEIVER VARCHAR(100),UUIDRECEIVER VARCHAR(100)," +
                    "AMOUNT BIGINT(100),DATE VARCHAR(100),PRIMARY KEY (ID))");
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createPlayer(Player player) {
        try {
            UUID uuid = player.getUniqueId();
            if (!exists(uuid)) {
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO money (NAME,UUID,MONEY) VALUES (?,?,?)");
                ps.setString(1, player.getName());
                ps.setString(2, uuid.toString());
                ps.setLong(3, 1000);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(UUID uuid) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM money WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addTransaction(String nameSender, UUID uuidSender, String nameReceiver, UUID uuidReceiver, long amount) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO transaction (ID,NAMESENDER,UUIDSENDER,NAMERECEIVER," +
                    "UUIDRECEIVER,AMOUNT,DATE) VALUES (?,?,?,?,?,?,?)");
            ps.setInt(1,getMaxTransactionID()+1);
            ps.setString(2, nameSender);
            ps.setString(3, uuidSender.toString());
            ps.setString(4, nameReceiver);
            ps.setString(5, uuidReceiver.toString());
            ps.setLong(6, amount);
            ps.setString(7, getSimpleDateFormat());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxTransactionID() {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT MAX(ID) AS LARGESTID FROM transaction");
            ResultSet rs = ps.executeQuery();
            int maxID = 0;
            if (rs.next()) {
                maxID = rs.getInt("LARGESTID");
                return maxID;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getTransactionNameSender(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT NAMESENDER FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String nameSender = "";
            if (rs.next()) {
                nameSender = rs.getString("NAMESENDER");
                return nameSender;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getTransactionUUIDSender(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT UUIDSENDER FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String uuid = "";
            if (rs.next()) {
                uuid = rs.getString("UUIDSENDER");
                return uuid;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getTransactionNameReceiver(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT NAMERECEIVER FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String nameSender = "";
            if (rs.next()) {
                nameSender = rs.getString("NAMERECEIVER");
                return nameSender;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getTransactionUUIDReceiver(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT UUIDRECEIVER FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String uuid = "";
            if (rs.next()) {
                uuid = rs.getString("UUIDRECEIVER");
                return uuid;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public long getTransactionAmount(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT AMOUNT FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            long amount = 0;
            if (rs.next()) {
                amount = rs.getLong("AMOUNT");
                return amount;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getTransactionDate(int id) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT DATE FROM transaction WHERE ID=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            String uuid = "";
            if (rs.next()) {
                uuid = rs.getString("DATE");
                return uuid;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    public List<Integer> getTransactionsFromPlayerSend(UUID uuid) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM transaction WHERE UUIDSENDER=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            List<Integer> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(rs.getInt("ID"));
            }
            return transactions;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Integer> transactions = new ArrayList<>();
        return transactions;
    }

    public List<Integer> getTransactionsFromPlayerReceive(UUID uuid) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM transaction WHERE UUIDRECEIVER=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            List<Integer> transactions = new ArrayList<>();
            while (rs.next()) {
                transactions.add(rs.getInt("ID"));
            }
            return transactions;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<Integer> transactions = new ArrayList<>();
        return transactions;
    }

    public void addMoney(UUID uuid, long amount) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE money SET MONEY=? WHERE UUID=?");
            ps.setLong(1, getMoney(uuid)+amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMoney(UUID uuid, long amount) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE money SET MONEY=? WHERE UUID=?");
            ps.setLong(1, getMoney(uuid)-amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long getMoney(UUID uuid) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT MONEY FROM money WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            long amount = 0;
            if (rs.next()) {
                amount = rs.getLong("MONEY");
                return amount;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void transactionsUpdatePlayerNames(int id) {
        String senderName = null;
        String uuid = getTransactionUUIDSender(id);
        if (Bukkit.getPlayer(uuid).isOnline()) {
            senderName = Bukkit.getPlayer(uuid).getName();
        } else {
            try {
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                InputStreamReader reader = new InputStreamReader(url.openStream());
                senderName = new JsonParser().parse(reader).getAsJsonObject().get("name").getAsString();
            } catch (Exception ignored) {
            }
        }
        if (senderName != null) {
            try {
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE transaction SET NAMESENDER=? WHERE ID=?");
                ps.setString(1, senderName);
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String receiverName = null;
        uuid = getTransactionUUIDReceiver(id);
        if (Bukkit.getPlayer(uuid).isOnline()) {
            receiverName = Bukkit.getPlayer(uuid).getName();
        } else {
            try {
                URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
                InputStreamReader reader = new InputStreamReader(connection.getInputStream());
                receiverName = new JsonParser().parse(reader).getAsJsonObject().get("name").getAsString();
            } catch (Exception ignored) {
            }
        }
        if (receiverName != null) {
            try {
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE transaction SET NAMERECEIVER=? WHERE ID=?");
                ps.setString(1, receiverName);
                ps.setInt(2, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void emptyTable() {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("TRUNCATE money");
            ps.executeUpdate();
            PreparedStatement ps2 = plugin.SQL.getConnection().prepareStatement("TRUNCATE transaction");
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(UUID uuid) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("DELETE FROM money WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getSimpleDateFormat() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss ");
        Date currentDate = new Date();
        return simpleDateFormat.format(currentDate);
    }

}




















