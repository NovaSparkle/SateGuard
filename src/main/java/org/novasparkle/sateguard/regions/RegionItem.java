package org.novasparkle.sateguard.regions;

import com.sk89q.worldguard.protection.flags.StateFlag;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.regions.flags.CustomFlags;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        if (EventManager.getEvent() != null && ChronoUnit.SECONDS.between(LocalDateTime.now(), EventManager.getEvent().getEndTime()) < ConfigManager.getInt("settings.offShardsTime")) {
            region.getRegion().setFlag(CustomFlags.generateShards, StateFlag.State.DENY);
            ConfigManager.sendMessage(event.getPlayer(), "disabledShards");
        }
        GuardManager.getRegionManager(location.getWorld()).addRegion(region.getRegion());
        ConfigManager.sendMessage(event.getPlayer(), "regionCreated", "regionName-%-" + this.regionType.getName());
    }
}
