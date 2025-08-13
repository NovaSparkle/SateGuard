package org.novasparkle.sateguard.listener;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.novasparkle.lunaspring.API.commands.annotations.LunaHandler;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.*;

import java.util.Set;

@LunaHandler
public class RegionListener implements Listener {
    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        for (RegionType regionType : RegionTypeList.getRegionTypes()) {
            if (event.getBlockPlaced().getType().equals(regionType.getMaterial())) {
                if (GuardManager.hasRegionsInside(event.getBlockPlaced().getLocation(), regionType.getRadius())) {
                    event.setCancelled(true);
                    ConfigManager.sendMessage(event.getPlayer(), "intersected");
                    return;
                }
                ConfigurationSection section = ConfigManager.getSection(String.format("regions.%s", regionType.getRegionId()));
                RegionItem regionItem = new RegionItem(section, regionType);
                regionItem.onPlace(event);
            }
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        Set<ProtectedRegion> regions = GuardManager.getRegions(location);
        if (!regions.isEmpty()) {
            String regionName = regions.iterator().next().getId();
            String[] stringLocation = regionName.split("_");
            Location center = new Location(location.getWorld(), LunaMath.toInt(stringLocation[0]), LunaMath.toInt(stringLocation[1]), LunaMath.toInt(stringLocation[2]));
            if (center.equals(location)) {
                ProtectedRegion rg = regions.stream().findFirst().orElse(null);
                assert rg != null;
                SateRegion region = RegionManager.getRegion(rg.getId());
                if (region.getRegion().getMembers().contains(event.getPlayer().getUniqueId()))
                    region.onRemove(event);
                else
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onBlockClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Set<ProtectedRegion> regions = GuardManager.getRegions(block.getLocation());
                if (!regions.isEmpty()) {
                    String regionName = regions.iterator().next().getId();
                    String[] stringLocation = regionName.split("_");
                    Location center = new Location(block.getWorld(), LunaMath.toInt(stringLocation[0]), LunaMath.toInt(stringLocation[1]), LunaMath.toInt(stringLocation[2]));
                    if (center.equals(block.getLocation())) {
                        SateRegion region = RegionManager.getRegion(regionName);
                        if (region.getRegion().getMembers().contains(event.getPlayer().getUniqueId()))
                            region.onClick(event);
                        else
                            event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    private void onExplode(EntityExplodeEvent event) {
        Location explosionCenter = event.getLocation();
        Set<ProtectedRegion> regions = GuardManager.getRegions(explosionCenter);
        if (!regions.isEmpty()) {
            SateRegion region = RegionManager.getRegion(regions.iterator().next().getId());
            region.onExplode(event);
        }
    }
}
