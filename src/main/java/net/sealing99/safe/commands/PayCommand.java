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
import java.util.Collection;
import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {
    private final Safe plugin;

    public PayCommand(Safe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!(args.length >= 2 && args.length <= 3)) {
            sender.sendMessage("Incorrect usage: /pay <playername> <amount>");
            return true;
        }

        String receiverName = args[0];

        if (receiverName.equals(sender.getName())) {
            sender.sendMessage("Cannot send money to yourself");
            return true;
        }

        @SuppressWarnings("deprication")
        OfflinePlayer receiver = Bukkit.getOfflinePlayer(receiverName);
        String receiverId = receiver.getUniqueId().toString();
        String senderId = player.getUniqueId().toString();

        BigDecimal amount;
        try {
            amount = new BigDecimal(args[1]).setScale(2, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException e) {
            sender.sendMessage("Incorrect value");
            return true;
        }

        BigDecimal balance = plugin.getBalanceDatabase().getBalance(senderId);
        if (balance.compareTo(amount) < 0) {
            sender.sendMessage("Insufficient balance");
            return true;
        }

        if (!plugin.getBalanceDatabase().hasBalance(receiverId)) {
            if (args.length != 3) {
                sender.sendMessage("Receiver does not have balance (yet). Type " + ChatColor.GOLD + "/pay " + receiverName + " " + args[1] + " confirm" + ChatColor.RESET + " to confirm");
                return true;
            }
            if (!args[2].equals("confirm")) {
                sender.sendMessage("Receiver does not have balance (yet). Type " + ChatColor.GOLD + "/pay " + args[0] + " " + args[1] + " confirm" + ChatColor.RESET + " to confirm");
                return true;
            }
        }

        List<BigDecimal> results = plugin.getBalanceDatabase().transferMoney(senderId, receiverId, amount);

        sender.sendMessage("Paid " + receiverName + " " + ChatColor.BLUE +"$" + amount);
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        for (Player onlineReceiver : players) {
            if (onlineReceiver.getUniqueId().toString().equals(receiverId)) {
                onlineReceiver.sendMessage(sender.getName() + " paid you " + ChatColor.BLUE +"$" + amount);
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<? extends Player> players = Bukkit.getOnlinePlayers().stream().toList();
            List<String> names = new ArrayList<>();
            for (Player player : players) {
                names.add(player.getName());
            }
            return names;
        }
        return List.of();
    }
}
