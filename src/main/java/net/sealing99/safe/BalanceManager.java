package net.sealing99.safe;

import net.sealing99.safe.database.BalanceDatabaseManager;
import net.sealing99.safe.exception.DatabaseException;
import net.sealing99.safe.exception.EconomyException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class BalanceManager {
    private final BalanceDatabaseManager balanceDatabase;

    public BalanceManager(Safe plugin) throws SQLException {
        balanceDatabase = new BalanceDatabaseManager(plugin);

        balanceDatabase.connect();
    }

    public void close() throws SQLException {
        if (balanceDatabase != null) {
            balanceDatabase.close();
        }
    }

    public BigDecimal getBalance(String uuid) {
        try {
            return balanceDatabase.getBalance(uuid);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in getBalance!");
            throw new DatabaseException("Database exception caught in getBalance!", e);
        }
    }

    public void setBalance(String uuid, BigDecimal bal) {
        validateBigDecimal(bal);

        try {
            BigDecimal result = balanceDatabase.setBalance(uuid, bal);
            if (!result.equals(bal)) {
                throw new DatabaseException("Database update failed at setBalance.");
            }
            validateBigDecimal(result);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in setBalance!");
            throw new DatabaseException("Database exception caught in setBalance!", e);
        }
    }

    public BigDecimal addMoney(String uuid, BigDecimal amount) {
        validateBigDecimal(amount);

        try {
            BigDecimal result = balanceDatabase.addBalance(uuid, amount);
            validateBigDecimal(result);
            return result;
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in addMoney!");
            throw new DatabaseException("Database exception caught in addMoney!", e);
        }
    }

    public BigDecimal removeMoney(String uuid, BigDecimal amount) {
        validateBigDecimal(amount);

        try {
            BigDecimal result = balanceDatabase.subtractBalance(uuid, amount);
            validateBigDecimal(result);
            return result;
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Database exception caught in removeMoney!");
            throw new DatabaseException("Database exception caught in removeMoney!", e);
        }
    }

    private static void validateBigDecimal(BigDecimal value) {
        if (value == null) {
            throw new EconomyException("BigDecimal caught null.");
        }

        if (value.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact() < 0) {
            throw new EconomyException("BigDecimal value caught negative.");
        }
    }
}
