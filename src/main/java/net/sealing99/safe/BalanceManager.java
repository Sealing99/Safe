package net.sealing99.safe;

import net.sealing99.safe.database.BalanceDatabaseManager;

import java.sql.SQLException;

public class BalanceManager {
    private final BalanceDatabaseManager balanceDatabase;

    public BalanceManager(Safe plugin) {
        balanceDatabase = new BalanceDatabaseManager(plugin);

        try {
            balanceDatabase.connect();
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Safe database failed to load");
            e.printStackTrace();
        }
    }

    public void close() {
        if (balanceDatabase != null) {
            try {
                balanceDatabase.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public float getBalance(String uuid) {
        return balanceDatabase.getBalance(uuid);
    }

    public void setBalance(String uuid, float bal) {
        try {
            balanceDatabase.setBalance(uuid, bal);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Exception caught at balanceDatabase.setBalance()!");
            e.printStackTrace();
        }
    }

    public float addMoney(String uuid, float amount) {
        try {
            balanceDatabase.addBalance(uuid, amount);
            return balanceDatabase.getBalance(uuid);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Exception caught at balanceDatabase.setBalance()!");
            e.printStackTrace();
        }
        return -1;
    }

    public float removeMoney(String uuid, float amount) {
        try {
            balanceDatabase.subtractBalance(uuid, amount);
            return balanceDatabase.getBalance(uuid);
        } catch (SQLException e) {
            Safe.getInstance().getLogger().severe("Exception caught at balanceDatabase.setBalance()!");
            e.printStackTrace();
        }
        return -1;
    }
}
