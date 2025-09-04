package org.novasparkle.sateguard.regions;


import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.menu.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<SateRegion> getRegions(OfflinePlayer offlinePlayer) {
        return regionList.stream().filter(rg -> rg.getRegion().getMembers().contains(offlinePlayer.getUniqueId()) || rg.getOwnerName().equals(offlinePlayer.getName())).collect(Collectors.toList());
    }
    public List<SateRegion> getOwningRegions(OfflinePlayer offlinePlayer) {
        return regionList.stream().filter(rg -> rg.getOwnerName().equals(offlinePlayer.getName())).collect(Collectors.toList());
    }

    public SateRegion newRegion(BlockPlaceEvent event, RegionType regionType) {
        SateRegion region = new SateRegion(event, regionType);
        ItemStack clickedItem = event.getItemInHand();
        if (NBTManager.hasTag(clickedItem, "regionLevel")) {
            region.setLevel(Level.of(NBTManager.getInt(clickedItem, "regionLevel")));
        }
        if (NBTManager.hasTag(clickedItem, "regionHealth")) {
            region.setHealth(NBTManager.getInt(clickedItem, "regionHealth"));
        }
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

    public boolean isAnyCenterBlock(Block block) {
        return regionList.stream().anyMatch(rg -> rg.getCenter().getBlock().equals(block));
    }
    public boolean isAnyCenterBlock(List<Block> blockList) {
        for (Block block : blockList) {
            if (isAnyCenterBlock(block)) return true;
        }
        return false;
    }
}
