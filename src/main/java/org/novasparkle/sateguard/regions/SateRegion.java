package org.novasparkle.sateguard.regions;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.RgManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.flags.CustomFlags;
import org.novasparkle.sateguard.regions.menu.LevelMenu;

import java.util.List;
import java.util.Objects;
@Getter
public class SateRegion {
    private final ProtectedRegion region;
    private final Location center;
    private final RegionType regionType;
    @Setter
    private int level;
    @Setter
    private int health;
    private int shards;
    private Hologram hologram;

    public SateRegion(BlockPlaceEvent event, RegionType regionType) {
        this.center = event.getBlockPlaced().getLocation();
        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        this.regionType = regionType;
        int radius = this.regionType.getRadius();
        this.region = RgManager.createRegion(name, center.clone().add(radius, radius, radius), center.clone().add(-radius, -radius, -radius));

        this.manageRegion(event.getPlayer());
        Location holoLocation = center.clone().add(ConfigManager.getInt("hologram.location.x"), ConfigManager.getInt("hologram.location.y"), ConfigManager.getInt("hologram.location.z"));
        this.hologram = DHAPI.createHologram(name, holoLocation, this.hologram());
        this.health = regionType.getStartHealth();
        this.level = 1;
        this.loadRegion();
    }

    public SateRegion(Location center, String regionType, int health, int level, int shards) {
        this.regionType = RegionTypeList.getRegionType(regionType);
        this.center = center;

        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        this.region = RgManager.getRegion(name);
        this.level = level;
        this.health = health;
        this.shards = shards;
        Hologram oHologram = DHAPI.getHologram(name);
        Location holoLocation = center.clone().add(ConfigManager.getInt("hologram.location.x"), ConfigManager.getInt("hologram.location.y"), ConfigManager.getInt("hologram.location.z"));
        this.hologram = oHologram == null ? DHAPI.createHologram(name, holoLocation, this.hologram()) : oHologram;
    }

    public SateRegion(Location location, ProtectedRegion region) {
        this.center = location;
        this.regionType = RegionTypeList.getRegionType(this.getRegionBlock().getType());
        this.region = region;
    }

    public Block getRegionBlock() {
        return this.center.getBlock();
    }

    private List<String> hologram() {
        List<String> hologram = ConfigManager.getStringList("hologram.lines");
        OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwners().getPlayers().iterator().next());
        hologram.replaceAll(line -> line.replace("[regionName]", ColorManager.color(this.regionType.getRegionName())).replace("[owner]", Objects.requireNonNull(owner.getName())));
        System.out.println(hologram);
        return hologram;
    }

    private void manageRegion(Player owner) {
        RgManager.addOwner(this.region.getId(), owner);
        this.region.setFlag(CustomFlags.generateShards, StateFlag.State.ALLOW);
        this.region.setFlag(CustomFlags.maximumStorages, ConfigManager.getInt("default.maximumStorages"));
        this.region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        this.region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        this.region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.DENY);
        this.region.setFlag(Flags.TNT, StateFlag.State.DENY);
        this.region.setFlag(Flags.GREET_MESSAGE, ConfigManager.getString("messages.onEnter").replace("[player]", owner.getName()));
        this.region.setFlag(Flags.FAREWELL_MESSAGE, ConfigManager.getString("messages.onExit").replace("[player]", owner.getName()));
    }

    public void loadRegion() {
        SateGuard.getDb().serializeRegion(this);
    }

    public void onRemove(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        location.getWorld().strikeLightningEffect(location);
        SateGuard.getDb().removeRegion(this.region.getId());
        RgManager.removeRegion(region.getId());
        ConfigManager.sendMessage(event.getPlayer(), "regionRemoved", "regionName-%-" + this.regionType.getRegionName());
    }

    public void onClick(PlayerInteractEvent event) {
        LevelMenu levelMenu = new LevelMenu(event.getPlayer(), this.level);
        MenuManager.openInventory(event.getPlayer(), levelMenu);
    }
}
