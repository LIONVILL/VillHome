package com.villhome.managers;

import com.villhome.VillHomePlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigManager {

    private final VillHomePlugin plugin;
    private Map<String, Integer> groupLimits;
    private String prefix;
    private String accentColor;

    private static final Pattern LAST_COLOR_PATTERN = Pattern.compile(
            "(§[0-9a-fk-orA-FK-OR]|§x(§[0-9a-fA-F]){6})(?!.*(?:§[0-9a-fk-orA-FK-OR]|§x(?:§[0-9a-fA-F]){6}))"
    );

    public ConfigManager(VillHomePlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration cfg = plugin.getConfig();
        groupLimits = new LinkedHashMap<>();

        prefix = colorize(cfg.getString("prefix", "&8[&6VillHome&8] &r"));
        accentColor = colorize(cfg.getString("accent-color", "&e"));

        if (cfg.isConfigurationSection("group-limits")) {
            for (String key : cfg.getConfigurationSection("group-limits").getKeys(false)) {
                groupLimits.put(key.toLowerCase(), cfg.getInt("group-limits." + key, 2));
            }
        }
        groupLimits.putIfAbsent("default", 2);
    }

    public void reload() {
        load();
    }

    public int getGroupLimit(Player player) {
        String primaryGroup = getPrimaryGroup(player);

        if (primaryGroup != null && groupLimits.containsKey(primaryGroup.toLowerCase())) {
            return groupLimits.get(primaryGroup.toLowerCase());
        }

        if (primaryGroup == null) {
            for (Map.Entry<String, Integer> entry : groupLimits.entrySet()) {
                String group = entry.getKey();
                if (group.equals("default")) continue;
                if (player.hasPermission("group." + group) && !player.hasPermission("-group." + group)) {
                    return entry.getValue();
                }
            }
        }

        return groupLimits.getOrDefault("default", 2);
    }

    private String getPrimaryGroup(Player player) {
        try {
            var provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
            if (provider == null) return null;
            LuckPerms luckPerms = provider.getProvider();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return null;
            return user.getPrimaryGroup();
        } catch (Exception e) {
            return null;
        }
    }


    public String getMessage(String key) {
        String msg = plugin.getConfig().getString("messages." + key, "&cMessage not found: " + key);
        msg = msg.replace("{prefix}", prefix);
        msg = colorize(msg);
        return msg;
    }

    public String getMessage(String key, Map<String, String> placeholders) {
        String msg = getMessage(key);
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            String token = "{" + e.getKey() + "}";
            String value = e.getValue();
            String restore = getColorBefore(msg, token);
            msg = msg.replace(token, accentColor + value + restore);
        }
        return msg;
    }

    // Coded by Villoni
    // t.me/VilloniQ


    private String getColorBefore(String msg, String token) {
        int idx = msg.indexOf(token);
        if (idx <= 0) return "§f";
        String before = msg.substring(0, idx);
        Matcher m = LAST_COLOR_PATTERN.matcher(before);
        String last = "§f";
        while (m.find()) {
            last = m.group();
        }
        return last;
    }

    private String colorize(String s) {
        return s.replace("&", "§");
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public Map<String, Integer> getGroupLimits() {
        return groupLimits;
    }
}
