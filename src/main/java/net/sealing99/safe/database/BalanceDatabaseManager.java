package net.sealing99.safe.database;

import com.google.common.collect.ImmutableList;
import net.sealing99.safe.Safe;
import net.sealing99.safe.exception.DatabaseException;
import net.sealing99.safe.exception.EconomyException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.List;

/**
 * A {@code BalanceDatabaseManager} is used as a low-level class to directly manipulate the database.
 * Various methods are supplied to control and retrieve data from players' balances.
 * @since <em>Initial:</em> v0.1.0
 * @author Sealing99
 */
public class BalanceDatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;

    public BalanceDatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Connects to the database via jdbc.
     * Creates the database when it does not exist already.
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @see BalanceDatabaseManager#close()
     * @since <em>Initial:</em> v0.1.0
     * @author Sealing99
     */
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

    /**
     * Closes the jdbc database connection.
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @see BalanceDatabaseManager#connect()
     * @since <em>Initial:</em> v0.1.0
     * @author Sealing99
     */
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Sets the balance of a player to a specific amount
     * @param uuid Player unique id as {@link String}
     * @param bal Balance to set the new balance to as {@link BigDecimal}
     * @return The resulting balance, which should be the same as parameter {@code bal}
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @throws IllegalArgumentException Whenever {@code amount} parameter is negative
     * @see BalanceDatabaseManager#addBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#subtractBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#transferMoney(String, String, BigDecimal)
     * @since Major update: v0.2.0
     * @author Sealing99
     */
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

    /**
     * Adds a specific amount of money to a player
     * @param uuid Player unique id as {@link String}
     * @param amount Amount of money as {@link BigDecimal}
     * @return The resulting balance, as {@link BigDecimal}
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @throws IllegalArgumentException Whenever {@code amount} parameter is non-positive
     * @see BalanceDatabaseManager#setBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#subtractBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#transferMoney(String, String, BigDecimal)
     * @since Major update: v0.2.0
     * @author Sealing99
     */
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

    /**
     * Subtracts a specific amount of money from a player
     * @param uuid Player unique id as {@link String}
     * @param amount Amount of money as {@link BigDecimal}
     * @return The resulting balance, as {@link BigDecimal}
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @throws IllegalArgumentException Whenever {@code amount} parameter is non-positive
     * @throws EconomyException Whenever a player will result in a negative balance
     * @see BalanceDatabaseManager#setBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#addBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#transferMoney(String, String, BigDecimal)
     * @since Major update: v0.5.0
     * @author Sealing99
     */
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

    /**
     * Retrieves the balance of a player by their unique id
     * @param uuid Player unique id
     * @return The balance as {@link BigDecimal}
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @see BalanceDatabaseManager#exists(String)
     * @since Major update: v0.2.0
     * @author Sealing99
     */
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

    /**
     * Checks whether the database contains a balance with a specific uuid
     * @param uuid Player unique id
     * @return The boolean value of if the balance exists
     * @see BalanceDatabaseManager#getBalance(String)
     * @since v0.5.0
     * @author Sealing99
     */
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

    /**
     * Transfers money from {@code sender} to {@code receiver}.
     * Checks if sender will result in negative balance, or if transaction amount is non-negative.
     * @param sender Player unique id of sender as {@link String}
     * @param receiver Player unique id of receiver as {@link String}
     * @param amount Amount of money as {@link BigDecimal}
     * @return The resulting balance, as {@link BigDecimal}
     * @throws SQLException Any exceptions from jdbc are not caught and will be rethrown
     * @throws IllegalArgumentException Whenever {@code amount} parameter is non-positive
     * @throws EconomyException Whenever a player will result in a negative balance
     * @throws RuntimeException Whenever any unknown errors are recognized
     * @throws DatabaseException Whenever the database failed to respond
     * @see BalanceDatabaseManager#setBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#addBalance(String, BigDecimal)
     * @see BalanceDatabaseManager#subtractBalance(String, BigDecimal)
     * @since Major update: v0.5.0
     * @author Sealing99
     */
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

        return ImmutableList.of(subtractResult, addResult);
    }

    /**
     * Private helper method to convert a {@link BigDecimal} to a {@code long} as cents
     * @param val The {@link BigDecimal} value to convert
     * @return The {@code long} value of cents
     * @see BalanceDatabaseManager#fromCents(long)
     * @since v0.2.0
     * @author Sealing99
     */
    private static long toCents(BigDecimal val) {
        return val.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    }

    /**
     * Private helper method to convert a {@code long} as cents to a {@link BigDecimal}
     * @param val The {@code long} value to convert
     * @return The {@link BigDecimal} value
     * @see BalanceDatabaseManager#toCents(BigDecimal)
     * @since v0.2.0
     * @author Sealing99
     */
    private static BigDecimal fromCents(long val) {
        return BigDecimal.valueOf(val, 2);
    }
}
