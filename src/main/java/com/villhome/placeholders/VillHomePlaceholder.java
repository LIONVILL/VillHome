package com.villhome.placeholders;

import com.villhome.VillHomePlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VillHomePlaceholder extends PlaceholderExpansion {

    private final VillHomePlugin plugin;

    public VillHomePlaceholder(VillHomePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "villhome";
    }

    @Override
    public @NotNull String getAuthor() {
        return "VillHome";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    // Coded by Villoni
    // t.me/VilloniQ

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";

        return switch (params.toLowerCase()) {
            case "limit" -> String.valueOf(plugin.getHomeManager().getTotalLimit(player));
            case "used" -> String.valueOf(plugin.getHomeManager().getHomesCount(player.getUniqueId()));
            default -> null;
        };
    }
}
