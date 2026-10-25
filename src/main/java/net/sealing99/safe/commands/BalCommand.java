package net.sealing99.safe.commands;

import net.sealing99.safe.Safe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalCommand implements CommandExecutor {
    private final Safe plugin;

    public BalCommand(Safe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (sender instanceof Player player) {
            float bal;
            bal = plugin.getBalanceDatabase().getBalance(String.valueOf(player.getUniqueId()));
            sender.sendMessage("You have $" + bal);
        }

        return true;
    }
}
