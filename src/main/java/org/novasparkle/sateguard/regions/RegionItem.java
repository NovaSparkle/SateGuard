package org.novasparkle.sateguard.regions;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;

@Getter
public class RegionItem extends NonMenuItem {
    private final RegionType regionType;
    public RegionItem(@NonNull ConfigurationSection section, RegionType regionType) {
        super(section);
        this.regionType = regionType;
    }

    public void onPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        location.getWorld().strikeLightningEffect(location);
        SateRegion region = RegionManager.newRegion(event, this.regionType);
        GuardManager.getRegionManager(location.getWorld()).addRegion(region.getRegion());
        ConfigManager.sendMessage(event.getPlayer(), "regionCreated", "regionName-%-" + this.regionType.getName());
    }
}
