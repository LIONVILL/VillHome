package com.villhome;

import com.villhome.commands.DelHomeCommand;
import com.villhome.commands.HomeCommand;
import com.villhome.commands.SetHomeCommand;
import com.villhome.commands.VhCommand;
import com.villhome.managers.ConfigManager;
import com.villhome.managers.HomeManager;
import com.villhome.placeholders.VillHomePlaceholder;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class VillHomePlugin extends JavaPlugin {

    private static VillHomePlugin instance;
    private HomeManager homeManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getDataFolder().mkdirs();
            saveResource("config.yml", false);
        }
        getConfig().options().copyDefaults(false);


        configManager = new ConfigManager(this);
        homeManager = new HomeManager(this);

        registerCommands();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VillHomePlaceholder(this).register();
            getLogger().info("PlaceholderAPI найден, плейсхолдеры зарегистрированы.");
        } else {
            getLogger().warning("PlaceholderAPI не найден. Плейсхолдеры не будут работать.");
        }

        getLogger().info("VillHome успешно загружен!");
    }

    @Override
    public void onDisable() {
        if (homeManager != null) {
            homeManager.saveAll();
        }
        getLogger().info("VillHome выгружен.");
    }

    private void registerCommands() {
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("home").setTabCompleter(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("sethome").setTabCompleter(new SetHomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("delhome").setTabCompleter(new DelHomeCommand(this));
        getCommand("vh").setExecutor(new VhCommand(this));
        getCommand("vh").setTabCompleter(new VhCommand(this));
    }

    public static VillHomePlugin getInstance() {
        return instance;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        homeManager.reload();
    }
}