package org.novasparkle.sateguard.listener;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.novasparkle.lunaspring.API.events.CooldownPrevent;
import org.novasparkle.lunaspring.API.events.LunaHandler;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.regions.*;
import org.novasparkle.sateguard.regions.flags.CustomFlags;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@LunaHandler
public class RegionListener implements Listener {
    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            for (RegionType regionType : RegionTypeList.getRegionTypes()) {
                if (event.getBlockPlaced().getType().equals(regionType.getMaterial())) {

                    int limit = this.getLimit(player, ConfigManager.getInt("settings.defaultRegions"));
                    int current = RegionManager.getOwningRegions(player).size();
                    if (current + 1 > limit) {
                        ConfigManager.sendMessage(player, "regionLimit", "current-%-" + current, "limit-%-" + limit);
                        return;
                    }

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
//        Block placedBlock = event.getBlockPlaced();
//        if (placedBlock.getState() instanceof Container) {
//            Location location = event.getBlock().getLocation();
//            Set<ProtectedRegion> regions = GuardManager.getRegions(location);
//            if (!regions.isEmpty()) {
//                ProtectedRegion wgRegion = regions.iterator().next();
//                Integer maximumStorages = wgRegion.getFlag(CustomFlags.maximumStorages);
//                Integer currentStorages = wgRegion.getFlag(CustomFlags.currentStorages);
//                if (maximumStorages == null || currentStorages == null) return;
//
//                if (currentStorages < maximumStorages) {
//                    wgRegion.setFlag(CustomFlags.currentStorages, currentStorages + 1);
//                } else {
//                    event.setCancelled(true);
//                    ConfigManager.sendMessage(event.getPlayer(), "tooManyContainers");
//                }
//            }
//        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        CooldownPrevent<Block> cooldownPrevent = new CooldownPrevent<>(100);
        Set<ProtectedRegion> regions = GuardManager.getRegions(location);
        if (!regions.isEmpty() && !cooldownPrevent.isCancelled(event, event.getBlock())) {
            ProtectedRegion wgRegion = regions.iterator().next();
            if (wgRegion.getFlag(CustomFlags.maximumStorages) == null) return;
            String[] stringLocation = wgRegion.getId().split("_");
            Location center = new Location(location.getWorld(), LunaMath.toInt(stringLocation[0]), LunaMath.toInt(stringLocation[1]), LunaMath.toInt(stringLocation[2]));
            if (center.equals(location)) {
                SateRegion region = RegionManager.getRegion(wgRegion.getId());
                Set<UUID> allMembers = wgRegion.getMembers().getUniqueIds();
                Player breaker = event.getPlayer();
                if (allMembers.contains(breaker.getUniqueId()) || breaker.getName().equals(region.getOwnerName())) {
                    if (EventManager.getEvent() != null) {
                        ConfigManager.sendMessage(breaker, "event.forbiddenAtNight");
                        event.setCancelled(true);
                        return;
                    }
                    region.onRemove(event);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onPiston(BlockPistonExtendEvent event) {
        if (!event.getBlocks().isEmpty())
            event.setCancelled(RegionManager.isAnyCenterBlock(event.getBlocks()));
    }
    @EventHandler(priority = EventPriority.MONITOR)
    private void onStickyPiston(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        if (event.isSticky() && !blocks.isEmpty()) {
            event.setCancelled(RegionManager.isAnyCenterBlock(blocks));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWitherChangedBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType().equals(EntityType.WITHER) && RegionManager.isAnyCenterBlock(event.getBlock())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onBlockClick(PlayerInteractEvent event) {
        CooldownPrevent<Block> cooldownPrevent = new CooldownPrevent<>(100);
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !event.getPlayer().isSneaking() && !cooldownPrevent.isCancelled(event, event.getClickedBlock())) {
            Block block = event.getClickedBlock();
            if (block != null) {
                Set<ProtectedRegion> regions = GuardManager.getRegions(block.getLocation());
                if (!regions.isEmpty()) {
                    ProtectedRegion wgRegion = regions.iterator().next();
                    if (wgRegion.getFlag(CustomFlags.maximumStorages) == null) return;
                    String[] stringLocation = wgRegion.getId().split("_");
                    Location center = new Location(block.getWorld(), LunaMath.toInt(stringLocation[0]), LunaMath.toInt(stringLocation[1]), LunaMath.toInt(stringLocation[2]));
                    if (center.equals(block.getLocation())) {
                        SateRegion region = RegionManager.getRegion(wgRegion.getId());
                        Set<UUID> allMembers = wgRegion.getMembers().getUniqueIds();
                        Player clicker = event.getPlayer();
                        if (allMembers.contains(clicker.getUniqueId()) || clicker.getName().equals(region.getOwnerName())) {
                            region.onClick(event);
                        } else {
                            event.setCancelled(true);
                        }
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
            ProtectedRegion wgRegion = regions.iterator().next();
            EntityType explodedEntity = event.getEntityType();
            if ((explodedEntity.equals(EntityType.MINECART_TNT) || explodedEntity.equals(EntityType.PRIMED_TNT))
                    && Objects.equals(wgRegion.getFlag(Flags.TNT), StateFlag.State.DENY)) {
                event.setCancelled(true);
                return;
            }
            if (wgRegion.getFlag(CustomFlags.maximumStorages) == null || EventManager.getEvent() == null) return;
            SateRegion region = RegionManager.getRegion(wgRegion.getId());
            region.onExplode(event);
        }
    }

    public int getLimit(Player player, int basic) {
        if (player.hasPermission("sateguard.limit.bypass")) return Integer.MAX_VALUE;

        String pattern = "sateguard.limit.";
        int finded = player.getEffectivePermissions()
                .stream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(p -> p.startsWith(pattern))
                .mapToInt(p -> LunaMath.toInt(p.replace(pattern, "")))
                .max()
                .orElse(basic);
        return Math.max(finded, basic);
    }
}
