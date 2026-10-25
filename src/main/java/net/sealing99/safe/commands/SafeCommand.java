package net.sealing99.safe.commands;

import net.sealing99.safe.Safe;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SafeCommand implements CommandExecutor, TabCompleter {
    private final Safe plugin;

    public SafeCommand(Safe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args[0].equals("admin")) {
            if (args[1].equals("bal")) {
                if (args.length != 3) {
                    sender.sendMessage("Incorrect usage");
                    return true;
                }
                String uuid = String.valueOf(Bukkit.getPlayerUniqueId(args[2]));
                float bal = plugin.getBalanceDatabase().getBalance(uuid);
                sender.sendMessage(args[2] + " has $" + bal);
            }

            if (args[1].equals("addmoney")) {
                if (args.length != 4) {
                    sender.sendMessage("Incorrect usage");
                    return true;
                }
                float amount;
                try {
                    amount = Float.parseFloat(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Incorrect value");
                    return true;
                }
                String uuid = String.valueOf(Bukkit.getPlayerUniqueId(args[2]));
                float result = plugin.getBalanceDatabase().addMoney(uuid, amount);
                sender.sendMessage("Sent $" + amount + " to " + args[2] + ". They now have $" + result);

                return true;
            }

            if (args[1].equals("removemoney")) {
                if (args.length != 4) {
                    sender.sendMessage("Incorrect usage");
                    return true;
                }
                float amount;
                try {
                    amount = Float.parseFloat(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Incorrect value");
                    return true;
                }
                String uuid = String.valueOf(Bukkit.getPlayerUniqueId(args[2]));
                float result = plugin.getBalanceDatabase().removeMoney(uuid, amount);
                sender.sendMessage("Removed $" + amount + " from " + args[2] + ". They now have $" + result);

                return true;
            }

            if (args[1].equals("setbalance")) {
                if (args.length != 4) {
                    sender.sendMessage("Incorrect usage");
                    return true;
                }
                float bal;
                try {
                    bal = Float.parseFloat(args[3]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Incorrect value");
                    return true;
                }
                String uuid = String.valueOf(Bukkit.getPlayerUniqueId(args[2]));
                plugin.getBalanceDatabase().setBalance(uuid, bal);
                sender.sendMessage("Set " + args[2] + "'s balance to $" + bal);

                return true;
            }
        }

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
