package com.villhome.commands;

import com.villhome.VillHomePlugin;
import com.villhome.managers.HomeData;
import com.villhome.managers.HomeManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class VhCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
            "addhomelimit", "removehomelimit", "gethome", "tphome", "removehome", "reload", "info", "migration"
    );

    private final VillHomePlugin plugin;

    public VhCommand(VillHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villhome.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("usage-vh"));
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "addhomelimit"    -> handleAddLimit(sender, args);
            case "removehomelimit" -> handleRemoveLimit(sender, args);
            case "gethome"         -> handleGetHome(sender, args);
            case "tphome"          -> handleTpHome(sender, args);
            case "removehome"      -> handleRemoveHome(sender, args);
            case "reload"          -> handleReload(sender);
            case "info"            -> handleInfo(sender);
            case "migration"       -> handleMigration(sender);
            default                -> sender.sendMessage(plugin.getConfigManager().getMessage("usage-vh"));
        }

        return true;
    }

    // /vh addhomelimit <nick> <amount>
    private void handleAddLimit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /vh addhomelimit <nick> <amount>");
            return;
        }
        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", Map.of("nick", args[1])));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cНекорректное число: " + args[2]);
            return;
        }
        HomeManager hm = plugin.getHomeManager();
        hm.addExtraLimit(target.getUniqueId(), amount);
        int total = hm.getExtraLimit(target.getUniqueId());
        sender.sendMessage(plugin.getConfigManager().getMessage("limit-added",
                Map.of("nick", target.getName() != null ? target.getName() : args[1],
                        "amount", String.valueOf(amount),
                        "total", String.valueOf(total))));
    }

    // /vh removehomelimit <nick> <amount>
    private void handleRemoveLimit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /vh removehomelimit <nick> <amount>");
            return;
        }
        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", Map.of("nick", args[1])));
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cНекорректное число: " + args[2]);
            return;
        }
        HomeManager hm = plugin.getHomeManager();
        hm.removeExtraLimit(target.getUniqueId(), amount);
        int total = hm.getExtraLimit(target.getUniqueId());
        sender.sendMessage(plugin.getConfigManager().getMessage("limit-removed",
                Map.of("nick", target.getName() != null ? target.getName() : args[1],
                        "amount", String.valueOf(amount),
                        "total", String.valueOf(total))));
    }

    // /vh gethome <nick>
    private void handleGetHome(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cИспользование: /vh gethome <nick>");
            return;
        }
        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", Map.of("nick", args[1])));
            return;
        }

        HomeManager hm = plugin.getHomeManager();
        HomeData data = hm.getHomeData(target.getUniqueId());
        int extra = data.getExtraLimit();
        int used = data.getHomesCount();
        Set<String> names = data.getHomeNames();

        String limitStr;
        Player online = target.getPlayer();
        if (online != null) {
            limitStr = String.valueOf(hm.getTotalLimit(online));
        } else {
            limitStr = "группа+" + extra + " (игрок офлайн)";
        }

        String homesStr = names.isEmpty() ? "нет" : String.join(", ", names);
        String nick = target.getName() != null ? target.getName() : args[1];

        sender.sendMessage(plugin.getConfigManager().getMessage("player-info",
                Map.of("nick", nick, "limit", limitStr,
                        "used", String.valueOf(used), "homes", homesStr)));
    }

    // /vh tphome <nick> <name>
    private void handleTpHome(CommandSender sender, String[] args) {
        if (!(sender instanceof Player admin)) {
            sender.sendMessage("§cЭта команда только для игроков.");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /vh tphome <nick> <name>");
            return;
        }
        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", Map.of("nick", args[1])));
            return;
        }
        String homeName = args[2];
        Location loc = plugin.getHomeManager().getHome(target.getUniqueId(), homeName);
        if (loc == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin-home-not-found",
                    Map.of("nick", target.getName() != null ? target.getName() : args[1], "name", homeName)));
            return;
        }
        String nick = target.getName() != null ? target.getName() : args[1];
        admin.teleportAsync(loc).thenAccept(success -> {
            if (success) {
                admin.sendMessage(plugin.getConfigManager().getMessage("admin-teleported",
                        Map.of("nick", nick, "name", homeName)));
            }
        });
    }

    // /vh removehome <nick> <name>
    private void handleRemoveHome(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cИспользование: /vh removehome <nick> <name>");
            return;
        }
        OfflinePlayer target = getOfflinePlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found", Map.of("nick", args[1])));
            return;
        }
        String homeName = args[2];
        boolean removed = plugin.getHomeManager().deleteHome(target.getUniqueId(), homeName);
        String nick = target.getName() != null ? target.getName() : args[1];
        if (removed) {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin-home-deleted",
                    Map.of("nick", nick, "name", homeName)));
        } else {
            sender.sendMessage(plugin.getConfigManager().getMessage("admin-home-not-found",
                    Map.of("nick", nick, "name", homeName)));
        }
    }

    // /vh reload
    private void handleReload(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.getConfigManager().getMessage("configs-reloaded"));
    }

    // /vh info
    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("  §x§f§d§4§1§5§3● §fVill§x§f§d§4§1§5§3Home §8» §fинформация о плагине");
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("  §7Версия:  §x§f§d§4§1§5§31.0.0");
        sender.sendMessage("  §7Автор:   §x§f§d§4§1§5§3Villoni");
        sender.sendMessage("  §7Сервер:  §x§f§d§4§1§5§3villoni.ru");
        sender.sendMessage("  §7Discord: §x§f§d§4§1§5§3discord.gg/villoni");
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    // /vh migration
    private void handleMigration(CommandSender sender) {
        String prefix = plugin.getConfigManager().getPrefix();

        File essentialsUserdata = new File(Bukkit.getPluginsFolder(), "Essentials/userdata");
        if (!essentialsUserdata.exists() || !essentialsUserdata.isDirectory()) {
            sender.sendMessage(prefix + "§cПапка Essentials/userdata не найдена. Убедитесь что Essentials установлен.");
            return;
        }

        File[] files = essentialsUserdata.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            sender.sendMessage(prefix + "§cФайлы пользователей Essentials не найдены.");
            return;
        }

        sender.sendMessage(prefix + "§eНачинаю миграцию домов из Essentials... Файлов: §f" + files.length);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int playersProcessed = 0;
            int homesImported = 0;
            int homesSkipped = 0;
            int errors = 0;

            HomeManager hm = plugin.getHomeManager();

            for (File file : files) {
                String fileName = file.getName().replace(".yml", "");
                UUID uuid;
                try {
                    uuid = UUID.fromString(fileName);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                try {
                    YamlConfiguration ess = YamlConfiguration.loadConfiguration(file);

                    if (!ess.isConfigurationSection("homes")) continue;

                    playersProcessed++;
                    Set<String> homeKeys = ess.getConfigurationSection("homes").getKeys(false);

                    for (String homeName : homeKeys) {
                        String path = "homes." + homeName;

                        if (hm.homeExists(uuid, homeName)) {
                            homesSkipped++;
                            continue;
                        }

                        World world = null;
                        String worldUuidStr = ess.getString(path + ".world");
                        if (worldUuidStr != null) {
                            try {
                                world = Bukkit.getWorld(UUID.fromString(worldUuidStr));
                            } catch (IllegalArgumentException ignored) {}
                        }
                        if (world == null) {
                            String worldName = ess.getString(path + ".world-name");
                            if (worldName != null) world = Bukkit.getWorld(worldName);
                        }

                        if (world == null) {
                            homesSkipped++;
                            continue;
                        }

                        double x   = ess.getDouble(path + ".x");
                        double y   = ess.getDouble(path + ".y");
                        double z   = ess.getDouble(path + ".z");
                        float yaw   = (float) ess.getDouble(path + ".yaw");
                        float pitch = (float) ess.getDouble(path + ".pitch");

                        Location loc = new Location(world, x, y, z, yaw, pitch);

                        final World finalWorld = world;
                        final String finalName = homeName;
                        final UUID finalUuid = uuid;

                        hm.getHomeData(finalUuid).setHome(finalName, loc);
                        homesImported++;
                    }

                } catch (Exception e) {
                    errors++;
                    plugin.getLogger().warning("Ошибка при миграции файла " + file.getName() + ": " + e.getMessage());
                }
            }

            final int fProcessed = playersProcessed;
            final int fImported  = homesImported;
            final int fSkipped   = homesSkipped;
            final int fErrors    = errors;

            Bukkit.getScheduler().runTask(plugin, () -> {
                hm.saveAll();
                sender.sendMessage(prefix + "§a§lМиграция завершена!");
                sender.sendMessage(prefix + "§7Игроков с домами: §f" + fProcessed);
                sender.sendMessage(prefix + "§7Домов импортировано: §a" + fImported);
                sender.sendMessage(prefix + "§7Пропущено (уже есть / мир не загружен): §e" + fSkipped);
                if (fErrors > 0) {
                    sender.sendMessage(prefix + "§7Ошибок (см. консоль): §c" + fErrors);
                }
            });
        });
    }


    @SuppressWarnings("deprecation")
    private OfflinePlayer getOfflinePlayer(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;
        OfflinePlayer off = Bukkit.getOfflinePlayerIfCached(name);
        if (off != null && off.hasPlayedBefore()) return off;
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("villhome.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        String sub = args[0].toLowerCase();

        if (args.length == 2 && List.of("addhomelimit", "removehomelimit", "gethome", "tphome", "removehome").contains(sub)) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && List.of("tphome", "removehome").contains(sub)) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target != null) {
                return plugin.getHomeManager().getHomeNames(target.getUniqueId())
                        .stream()
                        .filter(h -> h.startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
