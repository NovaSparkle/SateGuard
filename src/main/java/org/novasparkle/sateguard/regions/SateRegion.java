package org.novasparkle.sateguard.regions;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.regions.flags.CustomFlags;
import org.novasparkle.sateguard.regions.menu.LevelMenu;
import org.novasparkle.sateguard.regions.menu.level.Fear;
import org.novasparkle.sateguard.regions.menu.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class SateRegion {
    private final ProtectedRegion region;
    private final Location center;
    private final RegionType regionType;
    private final String ownerName;
    private int level;
    private final AtomicInteger health;
    private final AtomicInteger shards;
    @Nullable
    private Hologram hologram;
    @Nullable
    private Fear fear;

    public SateRegion(BlockPlaceEvent event, RegionType regionType) {
        this.center = event.getBlockPlaced().getLocation();
        this.health = new AtomicInteger(regionType.getStartHealth());
        this.shards = new AtomicInteger();
        this.level = 1;
        this.regionType = regionType;
        this.ownerName = event.getPlayer().getName();

        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        int radius = this.regionType.getRadius();
        this.region = GuardManager.createRegion(name, center.clone().add(radius, radius, radius), center.clone().add(-radius, -radius, -radius));

        this.manageRegion(event.getPlayer(), EventManager.getEvent() != null);

        this.applyHologram(name);

    }

    public SateRegion(Location center, String ownerName, String regionType, int health, int level, int shards, @Nullable Fear fear) {
        this.regionType = RegionTypeList.getRegionType(regionType);
        this.center = center;

        String name = String.format("%d_%d_%d_%s", center.getBlockX(), center.getBlockY(), center.getBlockZ(), center.getWorld().getName());
        this.region = GuardManager.getRegion(name);
        this.ownerName = ownerName;
        this.level = level;
        this.health = new AtomicInteger(health);
        this.shards = new AtomicInteger(shards);
        this.fear = fear;
        this.applyHologram(name);
    }


    public void applyHologram(String name) {
        if (!checkHologram()) {
            Location holoLocation = center.clone().add(ConfigManager.getDouble("hologram.location.x"), ConfigManager.getDouble("hologram.location.y"), ConfigManager.getDouble("hologram.location.z"));
            this.hologram = this.hologram(name, holoLocation);
        }
    }

    private Hologram hologram(String name, Location location) {
        List<String> hologram = ConfigManager.getStringList("hologram.lines");

        hologram.replaceAll(line -> line
                .replace("[regionName]", ColorManager.color(this.regionType.getName()))
                .replace("[owner]", Objects.requireNonNull(this.ownerName))
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

    public void onRemove(Player remover) {
        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().removeRegion(this.region.getId()));
        GuardManager.removeRegion(region.getId());
        if (!checkHologram() && this.hologram != null) DHAPI.removeHologram(this.hologram.getName());
        RegionManager.removeRegion(this);
        ConfigManager.sendMessage(remover, "regionRemoved", "regionName-%-" + this.regionType.getName());
    }

    public void onRemove(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        location.getWorld().strikeLightningEffect(location);

        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().removeRegion(this.region.getId()));
        GuardManager.removeRegion(region.getId());
        if (!checkHologram() && this.hologram != null) DHAPI.removeHologram(this.hologram.getName());
        MenuManager.getActiveMenus(LevelMenu.class, true).filter(m -> m.getRegion().equals(this)).findFirst().ifPresent(firstMenu -> firstMenu.getInventory().getViewers().forEach(HumanEntity::closeInventory));

        event.setDropItems(false);
        event.setExpToDrop(0);
        ConfigurationSection section = ConfigManager.getSection(String.format("regions.%s", regionType.getRegionId()));
        RegionItem regionItem = new RegionItem(section, regionType);
        int newLevel = level <= 1 ? level : level - 1;
        regionItem.replaceLore(lore -> Utils.applyReplacements(lore, "level-%-" + newLevel, "health-%-" + this.getHealth()));
        NBTManager.setInt(regionItem.getItemStack(), "regionLevel", newLevel);
        NBTManager.setInt(regionItem.getItemStack(), "regionHealth", this.getHealth());
        regionItem.dropNaturally(location);

        if (this.getShards() > 0) {
            NonMenuItem shard = new NonMenuItem(ConfigManager.getSection("items.ShardItem"));
            shard.setAmount(this.getShards());
            shard.dropNaturally(location);
        }

        RegionManager.removeRegion(this);
        ConfigManager.sendMessage(event.getPlayer(), "regionRemoved", "regionName-%-" + this.regionType.getName());
    }

    public void onClick(PlayerInteractEvent event) {
        LevelMenu firstMenu = MenuManager.getActiveMenus(LevelMenu.class, true).filter(menu -> menu.getRegion().equals(this)).findFirst().orElse(null);
        LevelMenu levelMenu;
        if (firstMenu != null)
            levelMenu = (LevelMenu) firstMenu.copy(event.getPlayer());
        else
            levelMenu = new LevelMenu(event.getPlayer(), this);
        MenuManager.openInventory(levelMenu);
    }

    public void onExplode(EntityExplodeEvent event) {
        if (event.blockList().contains(this.center.getBlock())) {
            event.blockList().remove(this.center.getBlock());
            if (this.getHealth() > 0
                    && this.fear != null
                    && this.fear.ordinal() < Fear.BLOCK_INVINCIBILITY.ordinal()) {
                this.health.decrementAndGet();
                SateGuard.getDb().updateHealth(this);
                return;
            }
        }
        Location explosionLoc = event.getLocation();
        float power = event.getYield();

        if (this.getHealth() > 0 && this.getCenter().distance(explosionLoc) <= power * 3
                && this.fear != null && this.fear.ordinal() < Fear.BLOCK_INVINCIBILITY.ordinal()) {
            this.health.decrementAndGet();

            if (this.health.get() <= 0) {
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
        this.setHealth(this.getHealth() + level.addHealth());
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
        this.setHealth(this.regionType.getStartHealth());
        this.getCenter().getBlock().setType(regionType.getMaterial());
        this.applyHologram(this.getRegion().getId());
        SateGuard.getDb().updateHealth(this);
    }

    public void onStopEvent() {
        this.applyDefaultFlags();
        double healthPercent = ((double) this.getHealth() / regionType.getStartHealth()) * 100;
        if (healthPercent <= 0) {
            this.heal();
        } else if (Objects.equals(this.getRegion().getFlag(CustomFlags.generateShards), StateFlag.State.ALLOW)){
            int addShards;
            if (healthPercent < 51) {
                addShards = LunaMath.getRandomInt(ConfigManager.getString("settings.night.shards.1-50"));
            } else {
                addShards = LunaMath.getRandomInt(ConfigManager.getString("settings.night.shards.51-100"));
            }
            this.setShards(Math.min(64, this.shards.get() + addShards));
            SateGuard.getDb().updateShards(this);
        }
        this.region.setFlag(CustomFlags.generateShards, StateFlag.State.ALLOW);
    }

    public int getHealth() {
        return health.get();
    }

    public int getShards() {
        return shards.get();
    }

    public void setShards(int shards) {
        this.shards.set(shards);
    }
    public void setHealth(int health) {
        this.health.set(health);
    }
}
