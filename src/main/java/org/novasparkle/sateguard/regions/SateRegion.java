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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.regions.flags.CustomFlags;
import org.novasparkle.sateguard.regions.menu.LevelMenu;
import org.novasparkle.sateguard.regions.menu.level.Fear;
import org.novasparkle.sateguard.regions.menu.level.Level;

import java.util.List;
import java.util.Objects;
@Getter
public class SateRegion {
    private final ProtectedRegion region;
    private final Location center;
    private final RegionType regionType;
    private final String ownerName;
    private int level;
    @Setter
    private int health;
    @Setter
    private int shards;
    @Nullable
    private Hologram hologram;
    @Nullable
    private Fear fear;

    public SateRegion(BlockPlaceEvent event, RegionType regionType) {
        this.center = event.getBlockPlaced().getLocation();
        this.health = regionType.getStartHealth();
        this.level = 1;
        this.regionType = regionType;
        this.ownerName = event.getPlayer().getName();

        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        int radius = this.regionType.getRadius();
        this.region = GuardManager.createRegion(name, center.clone().add(radius, radius, radius), center.clone().add(-radius, -radius, -radius));

        this.manageRegion(event.getPlayer(), EventManager.getEvent() != null);

        Location holoLocation = center.clone().add(ConfigManager.getDouble("hologram.location.x"), ConfigManager.getDouble("hologram.location.y"), ConfigManager.getDouble("hologram.location.z"));
        this.hologram = this.hologram(name, holoLocation);

    }

    public SateRegion(Location center, String regionType, int health, int level, int shards, @Nullable Fear fear) {
        this.regionType = RegionTypeList.getRegionType(regionType);
        this.center = center;

        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        this.region = GuardManager.getRegion(name);
        this.ownerName = region.getOwners().getPlayers().stream().findFirst().orElse(null);
        this.level = level;
        this.health = health;
        this.shards = shards;
        this.fear = fear;
        this.applyHologram(name);
    }


    public void applyHologram(String name) {
        if (!checkHologram()) {
            Hologram oHologram = DHAPI.getHologram(name);
            Location holoLocation = center.clone().add(ConfigManager.getDouble("hologram.location.x"), ConfigManager.getDouble("hologram.location.y"), ConfigManager.getDouble("hologram.location.z"));
            this.hologram = oHologram == null ? this.hologram(name, holoLocation) : oHologram;
        }
    }

    @SuppressWarnings("deprecation")
    private Hologram hologram(String name, Location location) {
        List<String> hologram = ConfigManager.getStringList("hologram.lines");

        OfflinePlayer owner = Bukkit.getOfflinePlayer(region.getOwners().getPlayers().iterator().next());
        hologram.replaceAll(line -> line
                .replace("[regionName]", ColorManager.color(this.regionType.getName()))
                .replace("[owner]", Objects.requireNonNull(owner.getName()))
                .replace("[rgName]", this.region.getId()));

        return DHAPI.createHologram(name, location, hologram);
    }

    @SuppressWarnings("deprecation")
    private void manageRegion(Player owner, boolean judgementNight) {
        GuardManager.addOwner(this.region.getId(), owner);
        this.region.setFlag(CustomFlags.generateShards, StateFlag.State.ALLOW);
        this.region.setFlag(CustomFlags.maximumStorages, ConfigManager.getInt("default.maximumStorages"));
        this.region.setFlag(Flags.GREET_MESSAGE, ConfigManager.getString("messages.onEnter").replace("[player]", owner.getName()));
        this.region.setFlag(Flags.FAREWELL_MESSAGE, ConfigManager.getString("messages.onExit").replace("[player]", owner.getName()));
        if (judgementNight)
            onNightStarted();
        else
            this.applyDefaultFlags();
    }

    public void applyDefaultFlags() {
        this.region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
        this.region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.DENY);
        this.region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.DENY);
        this.region.setFlag(Flags.TNT, StateFlag.State.DENY);
    }

    public void onNightStarted() {
        if (this.fear != null && this.fear == Fear.INVINCIBILITY) return;

        this.region.setFlag(Flags.OTHER_EXPLOSION, StateFlag.State.ALLOW);
        this.region.setFlag(Flags.GHAST_FIREBALL, StateFlag.State.ALLOW);
        if (this.fear == null) {
            this.region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.ALLOW);
            this.region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
        } else if (this.fear.ordinal() < Fear.TNT_EXPLOSION.ordinal()) {
            this.region.setFlag(Flags.TNT, StateFlag.State.ALLOW);
        }

    }

    public void onRemove(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        location.getWorld().strikeLightningEffect(location);

        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().removeRegion(this.region.getId()));
        GuardManager.removeRegion(region.getId());
        if (!checkHologram() && this.hologram != null) DHAPI.removeHologram(this.hologram.getName());
        RegionManager.removeRegion(this);
        ConfigManager.sendMessage(event.getPlayer(), "regionRemoved", "regionName-%-" + this.regionType.getName());

    }

    public void onClick(PlayerInteractEvent event) {
        LevelMenu levelMenu = new LevelMenu(event.getPlayer(), this);
        MenuManager.openInventory(levelMenu);
    }

    public void onExplode(EntityExplodeEvent event) {
        EntityType explodedEntity = event.getEntityType();
        if ((explodedEntity.equals(EntityType.MINECART_TNT) || explodedEntity.equals(EntityType.PRIMED_TNT))
                && Objects.equals(this.region.getFlag(Flags.TNT), StateFlag.State.DENY)) {
            event.setCancelled(true);
            return;
        }
        if (event.blockList().contains(this.center.getBlock())) {
            event.blockList().remove(this.center.getBlock());
            if (this.health > 0
                    && this.fear != null
                    && this.fear.ordinal() < Fear.BLOCK_INVINCIBILITY.ordinal()) {
                this.setHealth(this.getHealth() - 1);
                SateGuard.getDb().updateHealth(this);
                return;
            }
        }
        Location explosionLoc = event.getLocation();
        float power = event.getYield();

        if (this.health > 0
                && this.getCenter().distance(explosionLoc) <= power * 3
                || (this.fear != null
                && this.fear.ordinal() < Fear.BLOCK_INVINCIBILITY.ordinal())) {
            this.setHealth(this.getHealth() - 1);

            if (this.health <= 0) {
                this.region.setFlag(Flags.BUILD, StateFlag.State.ALLOW);
                this.region.setFlag(Flags.USE, StateFlag.State.ALLOW);
                this.getCenter().getBlock().setType(Material.AIR);
                this.hideHologram();
            }
            SateGuard.getDb().updateHealth(this);
        }
    }

    private boolean checkHologram() {
        return Objects.equals(this.region.getFlag(CustomFlags.hideHologram), StateFlag.State.ALLOW);
    }

    private void hideHologram() {
        if (this.hologram != null)
            DHAPI.removeHologram(hologram.getName());
    }

    public void setLevel(Level level) {
        this.level = level.level();
        this.health += level.addHealth();
        this.fear = level.fear();
        if (this.fear != null)
            this.fear.accept(region);
        if (this.checkHologram()) {
            this.hideHologram();
        }
        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().updateLevel(this));
    }

    public void heal() {
        this.region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        this.region.setFlag(Flags.USE, StateFlag.State.DENY);
        this.health = this.regionType.getStartHealth();
        this.getCenter().getBlock().setType(regionType.getMaterial());
        this.applyHologram(this.getRegion().getId());
        SateGuard.getDb().updateHealth(this);
    }

    public void onStopEvent() {
        this.applyDefaultFlags();
        double healthPercent = ((double) this.health / regionType.getStartHealth()) * 100;
        if (healthPercent <= 0) {
            this.heal();
        } else {
            int addShards;
            if (healthPercent < 51) {
                addShards = LunaMath.getRandomInt(ConfigManager.getString("settings.night.shards.1-50"));
            } else {
                addShards = LunaMath.getRandomInt(ConfigManager.getString("settings.night.shards.51-100"));
            }
            this.setShards(Math.min(64, this.shards + addShards));
            SateGuard.getDb().updateShards(this);
        }
    }
}
