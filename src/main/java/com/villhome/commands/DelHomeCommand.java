package com.villhome.commands;

import com.villhome.VillHomePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DelHomeCommand implements CommandExecutor, TabCompleter {

    private final VillHomePlugin plugin;

    public DelHomeCommand(VillHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда только для игроков.");
            return true;
        }

        if (!player.hasPermission("villhome.player")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getConfigManager().getMessage("usage-delhome"));
            return true;
        }

        String name = args[0];
        boolean removed = plugin.getHomeManager().deleteHome(player.getUniqueId(), name);

        if (removed) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-deleted", Map.of("name", name)));
        } else {
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-found", Map.of("name", name)));
        }

        return true;
    }

    // Coded by Villoni
    // t.me/VilloniQ

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (args.length == 1) {
            return plugin.getHomeManager().getHomeNames(player.getUniqueId())
                    .stream()
                    .filter(h -> h.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
