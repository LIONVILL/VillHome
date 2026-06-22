package com.villhome.managers;

import com.villhome.VillHomePlugin;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {

    private final VillHomePlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    private final Map<UUID, HomeData> playerData = new HashMap<>();

    public HomeManager(VillHomePlugin plugin) {
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), "homes.yml");
        load();
    }


    public void load() {
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось создать homes.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        playerData.clear();

        if (dataConfig.isConfigurationSection("players")) {
            for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    int extra = dataConfig.getInt("players." + uuidStr + ".extra-limit", 0);
                    Map<String, Location> homes = new HashMap<>();

                    if (dataConfig.isConfigurationSection("players." + uuidStr + ".homes")) {
                        for (String homeName : dataConfig.getConfigurationSection("players." + uuidStr + ".homes").getKeys(false)) {
                            String locStr = dataConfig.getString("players." + uuidStr + ".homes." + homeName);
                            if (locStr != null) {
                                Location loc = HomeData.deserializeLocation(locStr);
                                if (loc != null) homes.put(homeName, loc);
                            }
                        }
                    }

                    playerData.put(uuid, new HomeData(extra, homes));
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveAll() {
        dataConfig = new YamlConfiguration();
        for (Map.Entry<UUID, HomeData> entry : playerData.entrySet()) {
            String path = "players." + entry.getKey();
            HomeData data = entry.getValue();
            dataConfig.set(path + ".extra-limit", data.getExtraLimit());
            for (Map.Entry<String, Location> home : data.getHomes().entrySet()) {
                dataConfig.set(path + ".homes." + home.getKey(), HomeData.serializeLocation(home.getValue()));
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить homes.yml: " + e.getMessage());
        }
    }

    public void reload() {
        saveAll();
        load();
    }


    private HomeData getData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new HomeData());
    }

    private void save(UUID uuid) {
        saveAll();
    }



    public int getTotalLimit(Player player) {
        int groupLimit = plugin.getConfigManager().getGroupLimit(player);
        int extra = getData(player.getUniqueId()).getExtraLimit();
        return groupLimit + extra;
    }


    public int getExtraLimit(UUID uuid) {
        return getData(uuid).getExtraLimit();
    }

    public void addExtraLimit(UUID uuid, int amount) {
        getData(uuid).addExtraLimit(amount);
        save(uuid);
    }

    public void removeExtraLimit(UUID uuid, int amount) {
        HomeData data = getData(uuid);
        data.setExtraLimit(data.getExtraLimit() - amount);
        save(uuid);
    }


    public boolean canAddHome(Player player) {
        return getData(player.getUniqueId()).getHomesCount() < getTotalLimit(player);
    }

    public boolean homeExists(UUID uuid, String name) {
        return getData(uuid).hasHome(name);
    }

    public void setHome(UUID uuid, String name, Location location) {
        getData(uuid).setHome(name, location);
        save(uuid);
    }

    public boolean deleteHome(UUID uuid, String name) {
        boolean removed = getData(uuid).removeHome(name);
        if (removed) save(uuid);
        return removed;
    }

    public Location getHome(UUID uuid, String name) {
        return getData(uuid).getHome(name);
    }

    public Set<String> getHomeNames(UUID uuid) {
        return getData(uuid).getHomeNames();
    }

    public int getHomesCount(UUID uuid) {
        return getData(uuid).getHomesCount();
    }

    public HomeData getHomeData(UUID uuid) {
        return getData(uuid);
    }
}
