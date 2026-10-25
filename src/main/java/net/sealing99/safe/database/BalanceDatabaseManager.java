package net.sealing99.safe.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

public class BalanceDatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public BalanceDatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        File file = new File(plugin.getDataFolder(), "players.db");

        plugin.getDataFolder().mkdirs();

        connection = DriverManager.getConnection(
                "jdbc:sqlite:" + file.getAbsolutePath()
        );

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_balance (
                    uuid TEXT PRIMARY KEY,
                    balance FLOAT NOT NULL DEFAULT 0
                )
            """);
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public void setBalance(String uuid, float bal) throws SQLException {
        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setFloat(2, bal);

            statement.executeUpdate();
        }
    }

    public void addBalance(String uuid, float amount) throws SQLException {
        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = balance + excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setFloat(2, amount);

            statement.executeUpdate();
        }
    }

    public void subtractBalance(String uuid, float amount) throws SQLException {
        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = balance - excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setFloat(2, amount);

            statement.executeUpdate();
        }
    }

    public float getBalance(String uuid) {
        String sql = "SELECT balance FROM player_balance WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getFloat("balance") : 0;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Exception caught in getBalance()!");
            return 0;
        }
    }
}
