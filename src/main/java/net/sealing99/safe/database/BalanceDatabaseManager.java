package net.sealing99.safe.database;

import net.sealing99.safe.Safe;
import net.sealing99.safe.exception.DatabaseException;
import net.sealing99.safe.exception.EconomyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.List;

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
                    balance INTEGER NOT NULL DEFAULT 0
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
            statement.setLong(2, cents);

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
            statement.setLong(2, cents);

            statement.executeUpdate();
        }

        return getBalance(uuid);
    }

    public boolean subtractBalance(String uuid, BigDecimal amount) throws SQLException {
        long cents = toCents(amount);

        BigDecimal previous = getBalance(uuid);
        if (previous.compareTo(amount) < 0) {
            throw new EconomyException("Cannot get to negative balance");
        }

        if (cents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Safe.getInstance().getLogger().info("removing " + amount + " from " + uuid);

        String sql = """
                INSERT INTO player_balance (uuid, balance) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET balance = balance - excluded.balance
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);
            statement.setLong(2, cents);

            return statement.executeUpdate() > 0;
        }
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

    public boolean exists(String uuid) {
        String sql = "SELECT 1 FROM player_balance WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<BigDecimal> transferMoney(String sender, String receiver, BigDecimal amount) throws SQLException {
        long cents = toCents(amount);

        if (cents <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BigDecimal previousSender = getBalance(sender);
        if (previousSender.compareTo(amount) < 0) {
            throw new EconomyException("Cannot get to negative balance");
        }

        BigDecimal subtractResult;

        try {
            if (!subtractBalance(sender, amount)) {
                throw new DatabaseException("Database failed to remove money");
            }
            subtractResult = getBalance(sender);
            subtractResult.setScale(2, RoundingMode.UNNECESSARY);
            if (previousSender.setScale(2, RoundingMode.UNNECESSARY).subtract(amount).compareTo(subtractResult.setScale(2, RoundingMode.UNNECESSARY)) != 0) {
                throw new RuntimeException("Failed to subtract balance while transferring money");
            }
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in removeMoney!");
            throw new DatabaseException("Database exception caught in removeMoney!", e);
        }

        BigDecimal addResult = addBalance(receiver, amount);

        return List.of(subtractResult, addResult);
    }

    private static long toCents(BigDecimal val) {
        return val.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }

    private static BigDecimal fromCents(long val) {
        return BigDecimal.valueOf(val, 2);
    }
}
