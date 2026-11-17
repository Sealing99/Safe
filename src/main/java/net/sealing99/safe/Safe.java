package net.sealing99.safe;

import net.sealing99.safe.commands.BalCommand;
import net.sealing99.safe.commands.PayCommand;
import net.sealing99.safe.commands.SafeCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Safe
 * <hr>
 * Main Safe class
 * @see <a href="README.md">README.md</a>
 * @since <em>initial:</em> v0.1.0
 * @version Safe v0.5.0
 * @author Sealing99
 */
public final class Safe extends JavaPlugin {
    private static Safe plugin;
    private BalanceManager balanceManager;
    public static final String ID = "Safe";

    /**
     * Enables the plugin
     * <hr>
     * <ul>
     *     <li>Initializes {@link BalanceManager}</li>
     *     <li>Registers commands</li>
     * </ul>
     * @since <em>initial:</em> v0.1.0
     * @author Sealing99
     */
    @Override
    public void onEnable() {
        plugin = this;
        try {
            balanceManager = new BalanceManager(this);
        } catch (SQLException e) {
            getLogger().severe("Safe Database failed to load!");
            e.printStackTrace();
            onDisable();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Using database for " + ID);

        getLogger().info("Registering commands for " + ID);
        Objects.requireNonNull(getCommand("bal")).setExecutor(new BalCommand(this));
        Objects.requireNonNull(getCommand("safe")).setExecutor(new SafeCommand(this));
        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand(this));
    }

    /**
     * Disables the plugin
     * <hr>
     * Closes database connection
     * @since <em>initial:</em> v0.1.0
     * @author Sealing99
     */
    @Override
    public void onDisable() {
        try {
            balanceManager.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to get the plugin instance
     * @return {@link Safe} instance of the running plugin
     * @since <em>initial:</em> v0.1.0
     * @author Sealing99
     */
    public static Safe getInstance() {
        return plugin;
    }

    /**
     * Used to get the balance manager
     * @return {@link BalanceManager} object
     * @since <em>initial:</em> v0.1.0
     * @author Sealing99
     */
    public BalanceManager getBalanceManager() {
        return this.balanceManager;
    }
}
