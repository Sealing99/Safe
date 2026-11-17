package net.sealing99.safe;

import net.sealing99.safe.database.BalanceDatabaseManager;
import net.sealing99.safe.exception.DatabaseException;
import net.sealing99.safe.exception.EconomyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

/**
 * Balance manager used to connect to a database manager.
 * The main methods of a BalanceManager are used with {@link String} and {@link BigDecimal}s.
 * @see BalanceDatabaseManager
 * @see BigDecimal
 * @see BalanceManager#close()
 * @since <em>initial:</em> v0.1.0
 * @author Sealing99
 */
public class BalanceManager {
    private final BalanceDatabaseManager balanceDatabase;

    public BalanceManager(Safe plugin) throws SQLException {
        balanceDatabase = new BalanceDatabaseManager(plugin);

        balanceDatabase.connect();
    }

    /**
     * Closes the connection to the database
     * @throws SQLException Rethrows the exception from SQL
     * @since <em>initial:</em> v0.1.0
     * @author Sealing99
     */
    public void close() throws SQLException {
        if (balanceDatabase != null) {
            balanceDatabase.close();
        }
    }

    /**
     * Used to get the balance of a player
     * @param uuid Player unique id
     * @return Value of money as {@link BigDecimal}
     * @throws DatabaseException {@linkplain  SQLException}s will be rethrown as a {@link DatabaseException}
     * @since Major update: v0.2.0
     * @author Sealing99
     */
    public BigDecimal getBalance(String uuid) {
        try {
            return balanceDatabase.getBalance(uuid).setScale(2, RoundingMode.UNNECESSARY);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in getBalance!");
            throw new DatabaseException("Database exception caught in getBalance!", e);
        }
    }

    /**
     * Used to set the balance of a player
     * @param uuid Player unique id
     * @param bal Amount of money to change balance to as {@link BigDecimal}
     * @throws DatabaseException {@linkplain  SQLException}s will be rethrown as a {@link DatabaseException}
     * @since Major update: v0.2.0
     * @author Sealing99
     */
    public void setBalance(String uuid, BigDecimal bal) {
        validateBalance(bal);

        try {
            BigDecimal result = balanceDatabase.setBalance(uuid, bal);
            if (result.compareTo(bal) != 0) {
                throw new DatabaseException("Database update failed at setBalance.");
            }
            validateBalance(result);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in setBalance!");
            throw new DatabaseException("Database exception caught in setBalance!", e);
        }
    }

    /**
     * Used to add money to a player
     * @param uuid Player unique id
     * @param amount Amount of money to add as {@link BigDecimal}
     * @throws DatabaseException {@linkplain  SQLException}s will be rethrown as a {@link DatabaseException}
     * @since Major update: v0.2.0
     * @author Sealing99
     */
    public BigDecimal addMoney(String uuid, BigDecimal amount) {
        validateTransaction(amount);

        try {
            BigDecimal result = balanceDatabase.addBalance(uuid, amount);
            validateBalance(result);
            return result.setScale(2, RoundingMode.UNNECESSARY);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in addMoney!");
            throw new DatabaseException("Database exception caught in addMoney!", e);
        }
    }

    /**
     * Used to remove money from a player
     * @param uuid Player unique id
     * @param amount Amount of money to remove as {@link BigDecimal}
     * @throws DatabaseException {@linkplain  SQLException}s will be rethrown as a {@link DatabaseException}
     * @since Major update: v0.2.0
     * @author Sealing99
     */
    public BigDecimal removeMoney(String uuid, BigDecimal amount) {
        validateTransaction(amount);

        try {
            if (!(balanceDatabase.subtractBalance(uuid, amount))) {
                throw new DatabaseException("Database failed to remove money");
            }
            BigDecimal value = balanceDatabase.getBalance(uuid);
            validateBalance(value);
            return value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in removeMoney!");
            throw new DatabaseException("Database exception caught in removeMoney!", e);
        }
    }

    /**
     * Used to transfer money between players
     * @param sender Player unique id of the player sending money
     * @param receiver Player unique id of the player receiving money
     * @param amount Amount of money to transfer as {@link BigDecimal}
     * @return The resulting balances of both players in a {@link List}
     * @since v0.5.0
     * @author Sealing99
     */
    public List<BigDecimal> transferMoney(String sender, String receiver, BigDecimal amount) {
        validateTransaction(amount);

        try {
            return balanceDatabase.transferMoney(sender, receiver, amount);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in transferMoney!");
            throw new DatabaseException("Database exception caught in transferMoney!", e);
        }
    }

    /**
     * Checks whether a player with a specific unique id already has a balance registered in the database
     * @param uuid Player unique id of the player to check
     * @return The result of whether the player has a balance
     * @since v0.5.0
     * @author Sealing99
     */
    public boolean hasBalance(String uuid) {
        return balanceDatabase.exists(uuid);
    }

    /**
     * Validates a balance by:
     * <ul>
     *     <li>Checking if the value is {@code null}</li>
     *     <li>Checking if the {@linkplain BigDecimal}'s size is 2 decimal places</li>
     *     <li>Checking if the value is non-negative</li>
     * </ul>
     * @param value The value to validate as {@link BigDecimal}
     * @since v0.2.0
     * @author Sealing99
     */
    private static void validateBalance(BigDecimal value) {
        if (value == null) {
            throw new EconomyException("BigDecimal caught null.");
        }

        if (value.scale() > 2) {
            throw new EconomyException("BigDecimal caught with more than 2 decimal places.");
        }

        if (value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact() < 0) {
            throw new EconomyException("BigDecimal value caught negative.");
        }
    }

    /**
     * Validates a transaction amount by:
     * <ul>
     *     <li>Checking if the amount is {@code null}</li>
     *     <li>Checking if the {@linkplain BigDecimal}'s size is 2 decimal places</li>
     *     <li>Checking if the value is positive</li>
     * </ul>
     * This is different from {@link BalanceManager#validateBalance(BigDecimal)} as this is used for transaction amounts instead of balances.
     * @param value The value to validate as {@link BigDecimal}
     * @since v0.2.0
     * @author Sealing99
     */
    private static void validateTransaction(BigDecimal value) {
        if (value == null) {
            throw new EconomyException("BigDecimal caught null.");
        }

        if (value.scale() > 2) {
            throw new EconomyException("BigDecimal caught with more than 2 decimal places.");
        }

        if (value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact() <= 0) {
            throw new EconomyException("BigDecimal value caught non-positive");
        }
    }
}
