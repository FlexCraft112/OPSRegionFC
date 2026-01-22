package me.flexcraft.opsregionng.listener;

import me.flexcraft.opsregionng.OPSRegionNG;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.List;

public class WorldEditCommandListener implements Listener {

    private final OPSRegionNG plugin;

    public WorldEditCommandListener(OPSRegionNG plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onWorldEdit(PlayerCommandPreprocessEvent e) {

        Player player = e.getPlayer();
        String msg = e.getMessage().toLowerCase();

        // 🔒 ЛОВИМ ВСЕ WORLDEDIT / FAWE КОМАНДЫ
        if (!msg.startsWith("//") && !msg.startsWith("/we")) {
            return;
        }

        // 1️⃣ ЕСЛИ ИГРОК СТОИТ В ЗАЩИЩЁННОМ РЕГИОНЕ → БЛОК
        if (isInProtectedRegion(player.getLocation())) {
            block(e, player);
            return;
        }

        // 2️⃣ ЕСЛИ ВЫДЕЛЕНИЕ ЗАДЕВАЕТ ЗАЩИЩЁННЫЙ РЕГИОН → БЛОК
        if (selectionTouchesProtected(player)) {
            block(e, player);
        }
    }

    // =========================
    // ПРОВЕРКА: ИГРОК В РЕГИОНЕ
    // =========================
    private boolean isInProtectedRegion(Location loc) {

        ApplicableRegionSet regions = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery()
                .getApplicableRegions(BukkitAdapter.adapt(loc));

        List<String> protectedRegions =
                plugin.getConfig().getStringList("worldedit.protected-regions");

        for (ProtectedRegion r : regions) {
            if (protectedRegions.contains(r.getId().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // =========================
    // ПРОВЕРКА: ВЫДЕЛЕНИЕ ЗАДЕВАЕТ РЕГИОН
    // =========================
    private boolean selectionTouchesProtected(Player player) {
        try {
            WorldEditPlugin we = (WorldEditPlugin)
                    plugin.getServer().getPluginManager().getPlugin("WorldEdit");

            if (we == null) return false;

            World world = player.getWorld();

            Region selection = we.getSession(player)
                    .getSelection(BukkitAdapter.adapt(world));

            if (selection == null) return false;

            // Проверяем минимум и максимум выделения
            Location min = BukkitAdapter.adapt(world, selection.getMinimumPoint());
            Location max = BukkitAdapter.adapt(world, selection.getMaximumPoint());

            return isInProtectedRegion(min) || isInProtectedRegion(max);

        } catch (Exception ignored) {
            return false;
        }
    }

    // =========================
    // БЛОКИРОВКА
    // =========================
    private void block(PlayerCommandPreprocessEvent e, Player player) {
        e.setCancelled(true);

        String msg = plugin.getConfig().getString("messages.worldedit-blocked");
        if (msg != null && !msg.isEmpty()) {
            player.sendMessage(msg.replace("&", "§"));
        }
    }
}
