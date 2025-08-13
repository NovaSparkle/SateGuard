package org.novasparkle.sateguard.regions;


import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.event.block.BlockPlaceEvent;
import org.novasparkle.sateguard.SateGuard;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class RegionManager {
    @Getter
    private final List<SateRegion> regionList = new ArrayList<>();
    public void load() {
        regionList.addAll(SateGuard.getDb().getAllRegion());
    }

    public SateRegion getRegion(String name) {
        return regionList.stream().filter(rg -> rg.getRegion().getId().equals(name)).findFirst().orElse(null);
    }

    public SateRegion newRegion(BlockPlaceEvent event, RegionType regionType) {
        SateRegion region = new SateRegion(event, regionType);
        regionList.add(region);
        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().serializeRegion(region));
        return region;
    }
    public void removeRegion(SateRegion region) {
        regionList.remove(region);
    }

    public void serializeAll() {
        regionList.forEach(rg -> SateGuard.getDb().update(rg));
    }
}
