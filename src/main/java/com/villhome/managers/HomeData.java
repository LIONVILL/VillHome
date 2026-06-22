package com.villhome.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HomeData {

    private int extraLimit;
    private final Map<String, Location> homes;

    public HomeData() {
        this.extraLimit = 0;
        this.homes = new HashMap<>();
    }

    public HomeData(int extraLimit, Map<String, Location> homes) {
        this.extraLimit = extraLimit;
        this.homes = homes;
    }

    public int getExtraLimit() {
        return extraLimit;
    }

    public void setExtraLimit(int extraLimit) {
        this.extraLimit = Math.max(0, extraLimit);
    }

    public void addExtraLimit(int amount) {
        this.extraLimit = Math.max(0, this.extraLimit + amount);
    }

    public Map<String, Location> getHomes() {
        return homes;
    }

    public Set<String> getHomeNames() {
        return homes.keySet();
    }

    public Location getHome(String name) {
        return homes.get(name.toLowerCase());
    }

    public boolean hasHome(String name) {
        return homes.containsKey(name.toLowerCase());
    }

    public void setHome(String name, Location location) {
        homes.put(name.toLowerCase(), location);
    }

    public boolean removeHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }

    public int getHomesCount() {
        return homes.size();
    }

    public static String serializeLocation(Location loc) {
        return loc.getWorld().getName() + "," +
                loc.getX() + "," +
                loc.getY() + "," +
                loc.getZ() + "," +
                loc.getYaw() + "," +
                loc.getPitch();
    }

    public static Location deserializeLocation(String s) {
        try {
            String[] parts = s.split(",");
            World world = Bukkit.getWorld(parts[0]);
            if (world == null) return null;
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(world, x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }
}
