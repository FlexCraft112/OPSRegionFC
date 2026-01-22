package me.flexcraft.opsregionng;

import me.flexcraft.opsregionng.listener.BlockProtectionListener;
import me.flexcraft.opsregionng.listener.WorldEditCommandListener;
import me.flexcraft.opsregionng.listener.RegionProtectListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class OPSRegionNG extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getPluginManager().registerEvents(
                new BlockProtectionListener(this), this
        );
        Bukkit.getPluginManager().registerEvents(
                new WorldEditCommandListener(this), this
        );
        Bukkit.getPluginManager().registerEvents(
                new RegionProtectListener(this), this
        );

        getLogger().info("OPSRegionNG enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("OPSRegionNG disabled");
    }
}
