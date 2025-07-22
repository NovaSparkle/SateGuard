package org.novasparkle.sateguard.event;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.RgManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.RegionItem;
import org.novasparkle.sateguard.regions.RegionType;
import org.novasparkle.sateguard.regions.RegionTypeList;
import org.novasparkle.sateguard.regions.SateRegion;

import java.util.Set;

public class RegionListener implements Listener {
    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        for (RegionType regionType : RegionTypeList.getRegionTypes()) {
            if (event.getBlockPlaced().getType().equals(regionType.getMaterial())) {
                ConfigurationSection section = ConfigManager.getSection(String.format("regions.%s", regionType.getRegionId()));
                RegionItem regionItem = new RegionItem(section, regionType);
                regionItem.onPlace(event);
            }
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        Set<ProtectedRegion> regions = RgManager.getRegions(location);
        if (!regions.isEmpty()) {
            SateRegion region = new SateRegion(location, regions.stream().findFirst().orElse(null));
            if (region.getRegionBlock().equals(event.getBlock()))
                region.onRemove(event);
        }
    }

    @EventHandler
    private void onBlockClick(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

            Block block = event.getClickedBlock();
            if (block != null) {

                Set<ProtectedRegion> regions = RgManager.getRegions(block.getLocation());
                if (!regions.isEmpty()) {

                    String regionName = regions.iterator().next().getId();
                    String[] stringLocation = regionName.split("_");
                    Location center = new Location(block.getWorld(), LunaMath.toInt(stringLocation[1]), LunaMath.toInt(stringLocation[2]), LunaMath.toInt(stringLocation[3]));
                    if (center.equals(block.getLocation())) {

                        SateRegion region = SateGuard.getDb().getRegion(regionName);
                        if (region.getRegionBlock().equals(block)) region.onClick(event);
                    }
                }
            }
        }
    }
}
