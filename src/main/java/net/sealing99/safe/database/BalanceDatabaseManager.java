package net.sealing99.safe.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
                    balance TEXT NOT NULL DEFAULT '0'
                )
            """);
        }
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public BigDecimal setBalance(String uuid, BigDecimal bal) throws SQLException {
        long cents = toCents(bal);

        if (cents < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }

        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setFloat(2, cents);

            statement.executeUpdate();
        }

        return getBalance(uuid);
    }

    public BigDecimal addBalance(String uuid, BigDecimal amount) throws SQLException {
        long cents = toCents(amount);

        if (cents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = balance + excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setFloat(2, cents);

            statement.executeUpdate();
        }

        return getBalance(uuid);
    }

    public BigDecimal subtractBalance(String uuid, BigDecimal amount) throws SQLException {
        long cents = toCents(amount);

        if (cents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String sql = """
                UPDATE player_balance
                SET balance = balance - ?
                WHERE uuid = ?
                AND balance >= ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cents);
            statement.setString(2, uuid);
            statement.setLong(3, cents);
        }

        return getBalance(uuid);
    }

    public BigDecimal getBalance(String uuid) throws SQLException {
        String sql = "SELECT balance FROM player_balance WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return BigDecimal.ZERO;
                }

                return fromCents(result.getLong("balance"));
            }
        }
    }

    private static long toCents(BigDecimal val) {
        return val.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }

    private static BigDecimal fromCents(long val) {
        return BigDecimal.valueOf(val, 2);
    }
}
