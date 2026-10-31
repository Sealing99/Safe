package net.sealing99.safe;

import net.sealing99.safe.commands.BalCommand;
import net.sealing99.safe.commands.SafeCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

public final class Safe extends JavaPlugin {
    private static Safe plugin;
    private BalanceManager balanceManager;
    public static final String ID = "Safe";

    @Override
    public void onEnable() {
        plugin = this;
        try {
            balanceManager = new BalanceManager(this);
        } catch (SQLException e) {
            getLogger().severe("Safe Database failed to load!");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Using database for " + ID);

        getLogger().info("Registering commands for " + ID);
        Objects.requireNonNull(getCommand("bal")).setExecutor(new BalCommand(this));
        Objects.requireNonNull(getCommand("safe")).setExecutor(new SafeCommand(this));
    }

    @Override
    public void onDisable() {
        try {
            balanceManager.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Safe getInstance() {
        return plugin;
    }

    public BalanceManager getBalanceDatabase() {
        return this.balanceManager;
    }
}
