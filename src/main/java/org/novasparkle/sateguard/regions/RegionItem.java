package org.novasparkle.sateguard.regions;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.RgManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionType;
import org.novasparkle.sateguard.regions.SateRegion;

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
        SateRegion region = new SateRegion(event, this.regionType);
        RgManager.getRegionManager(location.getWorld()).addRegion(region.getRegion());
        ConfigManager.sendMessage(event.getPlayer(), "regionCreated", "regionName-%-" + this.regionType.getRegionName());
    }
}
