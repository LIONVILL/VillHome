package com.villhome.commands;

import com.villhome.VillHomePlugin;
import com.villhome.managers.HomeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class SetHomeCommand implements CommandExecutor, TabCompleter {

    private final VillHomePlugin plugin;

    public SetHomeCommand(VillHomePlugin plugin) {
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
            player.sendMessage(plugin.getConfigManager().getMessage("usage-sethome"));
            return true;
        }

        String name = args[0];
        HomeManager hm = plugin.getHomeManager();

        if (hm.homeExists(player.getUniqueId(), name)) {
            player.sendMessage(plugin.getConfigManager().getMessage("home-already-exists", Map.of("name", name)));
            return true;
        }

        if (!hm.canAddHome(player)) {
            int used = hm.getHomesCount(player.getUniqueId());
            int limit = hm.getTotalLimit(player);
            player.sendMessage(plugin.getConfigManager().getMessage("home-limit-reached",
                    Map.of("used", String.valueOf(used), "limit", String.valueOf(limit))));
            return true;
        }

        hm.setHome(player.getUniqueId(), name, player.getLocation());
        player.sendMessage(plugin.getConfigManager().getMessage("home-set", Map.of("name", name)));
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