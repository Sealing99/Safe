package net.sealing99.safe.commands;

import net.sealing99.safe.Safe;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bukkit command for retrieving balance
 * @since Major update: v0.2.0
 * @author Sealing99
 */
public class BalCommand implements CommandExecutor {
    private final Safe plugin;

    public BalCommand(Safe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            BigDecimal bal;
            bal = plugin.getBalanceManager().getBalance(String.valueOf(player.getUniqueId()));
            sender.sendMessage("You have " + ChatColor.BLUE + "$" + bal.setScale(2, RoundingMode.UNNECESSARY));
        }

        return true;
    }
}
