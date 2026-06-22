package com.villhome.commands;

import com.villhome.VillHomePlugin;
import com.villhome.managers.HomeManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class HomeCommand implements CommandExecutor, TabCompleter {

    private final VillHomePlugin plugin;

    public HomeCommand(VillHomePlugin plugin) {
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
            player.sendMessage(plugin.getConfigManager().getMessage("usage-home"));
            return true;
        }

        String name = args[0];
        HomeManager hm = plugin.getHomeManager();
        Location loc = hm.getHome(player.getUniqueId(), name);

        // Coded by Villoni
        // t.me/VilloniQ

        if (loc == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-not-found",
                    Map.of("name", name)));
            return true;
        }

        player.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                player.sendMessage(plugin.getConfigManager().getMessage("home-teleported",
                        Map.of("name", name)));
            }
        });

        return true;
    }

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
