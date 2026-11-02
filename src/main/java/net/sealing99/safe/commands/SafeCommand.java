package net.sealing99.safe.commands;

import net.sealing99.safe.Safe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SafeCommand implements CommandExecutor, TabCompleter {
    private final Safe plugin;

    public SafeCommand(Safe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!sender.hasPermission("safe.admin")) {
            sender.sendMessage("You do not have permissions to execute this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("Incorrect usage: /safe <subcommand> [params]");
            return true;
        }

        if (args[0].equals("admin")) {
            if (args.length == 1) {
                sender.sendMessage("Incorrect usage: /safe admin <subcommand> [params]");
                return true;
            }

            switch (args[1]) {
                case "bal" -> {
                    return bal(sender, args);
                }
                case "addmoney" -> {
                    return addMoney(sender, args);
                }
                case "removemoney" -> {
                    return removeMoney(sender, args);
                }
                case "setbalance" -> {
                    return setBalance(sender, args);
                }
            }

        }

        return true;
    }

    public boolean bal(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 3) {
            sender.sendMessage("Incorrect usage");
            return true;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        String uuid = String.valueOf(offlinePlayer.getUniqueId());
        if (uuid == null || uuid.isEmpty()) {
            sender.sendMessage("Player not found");
            return true;
        }
        BigDecimal bal = plugin.getBalanceDatabase().getBalance(uuid);
        sender.sendMessage(args[2] + " has " + ChatColor.BLUE + "$" + bal.setScale(2, RoundingMode.UNNECESSARY));

        return true;
    }

    public boolean addMoney(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 4) {
            sender.sendMessage("Incorrect usage");
            return true;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Incorrect value");
            return true;
        }
        if (amount.scale() > 2) {
            sender.sendMessage("Value cannot have more than 2 decimal places.");
            return true;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        String uuid = String.valueOf(offlinePlayer.getUniqueId());
        if (uuid == null || uuid.isEmpty()) {
            sender.sendMessage("Player not found");
            return true;
        }
        BigDecimal result = plugin.getBalanceDatabase().addMoney(uuid, amount);
        sender.sendMessage("Sent $" + amount + " to " + args[2] + ". They now have " + ChatColor.BLUE + "$" + result.setScale(2, RoundingMode.UNNECESSARY));

        return true;
    }

    public boolean removeMoney(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 4) {
            sender.sendMessage("Incorrect usage");
            return true;
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Incorrect value");
            return true;
        }
        if (amount.scale() > 2) {
            sender.sendMessage("Value cannot have more than 2 decimal places.");
            return true;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        String uuid = String.valueOf(offlinePlayer.getUniqueId());
        if (uuid == null || uuid.isEmpty()) {
            sender.sendMessage("Player not found");
            return true;
        }
        BigDecimal result = plugin.getBalanceDatabase().removeMoney(uuid, amount);
        sender.sendMessage("Removed $" + amount + " from " + args[2] + ". They now have " + ChatColor.BLUE + "$" + result.setScale(2, RoundingMode.UNNECESSARY));

        return true;
    }

    public boolean setBalance(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        if (args.length != 4) {
            sender.sendMessage("Incorrect usage");
            return true;
        }
        BigDecimal bal;
        try {
            bal = new BigDecimal(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Incorrect value");
            return true;
        }
        if (bal.scale() > 2) {
            sender.sendMessage("Value cannot have more than 2 decimal places.");
            return true;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
        String uuid = String.valueOf(offlinePlayer.getUniqueId());
        if (uuid == null || uuid.isEmpty()) {
            sender.sendMessage("Player not found");
            return true;
        }
        plugin.getBalanceDatabase().setBalance(uuid, bal);
        sender.sendMessage("Set " + args[2] + "'s balance to $" + bal.setScale(2, RoundingMode.UNNECESSARY));

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> recc = new ArrayList<>(List.of());

        try {
            if (sender.hasPermission("safe.admin")) {
                if (args.length <= 1) {
                    recc.add("admin");
                }
                if (args[0].equals("admin")) {
                    if (args.length <= 2) {
                        recc.add("bal");
                        recc.add("addmoney");
                        recc.add("removemoney");
                        recc.add("setbalance");
                    }
                    if (args[1].equals("addmoney") || args[1].equals("removemoney") || args[1].equals("bal") || args[1].equals("setbalance")) {
                        if (args.length <= 3) {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                recc.add(player.getName());
                            }
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException _) {

        }

        return recc;
    }
}
