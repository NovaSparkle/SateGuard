package org.novasparkle.sateguard.regions.menu;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.configuration.IConfig;
import org.novasparkle.lunaspring.API.events.CooldownPrevent;
import org.novasparkle.lunaspring.API.menus.AMenu;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.item.InfoButton;
import org.novasparkle.sateguard.regions.menu.item.LevelButton;
import org.novasparkle.sateguard.regions.menu.item.Shard;
import org.novasparkle.sateguard.regions.menu.item.ShardSlot;
import org.novasparkle.sateguard.regions.menu.level.LevelType;

@Getter
public class LevelMenu extends AMenu {

    private final IConfig config;
    private final SateRegion region;
    private final CooldownPrevent<LevelMenu> cooldown;
    public LevelMenu(@NonNull Player player, SateRegion region) {
        super(player);
        this.cooldown = new CooldownPrevent<>(20);
        this.config = new IConfig(SateGuard.getInstance().getDataFolder(), "LevelMenu");
        this.region = region;
        this.initialize(config.getString("title").replace("[owner]", region.getOwnerName()),
                (byte) config.getInt("size"),
                config.getSection("decoration"), true);
    }

    @Override
    public void onOpen(InventoryOpenEvent inventoryOpenEvent) {
        ConfigurationSection section = this.config.getSection("levels");

        for (String key : section.getKeys(false)) {
            ConfigurationSection levelSection = section.getConfigurationSection(key);
            if (levelSection != null) {
                int level = LunaMath.toInt(levelSection.getName());
                if (level <= this.region.getLevel()) {
                    this.addItems(new LevelButton(levelSection, LevelType.OPENED, this.region));
                } else if (level == (region.getLevel() + 1)) {
                    this.addItems(new LevelButton(levelSection, LevelType.NEXT, this.region));
                } else {
                    this.addItems(new LevelButton(levelSection, LevelType.CLOSED, this.region));
                }
            }
        }
        Item shard;
        if (this.region.getShards() == 0) {
            shard = new ShardSlot(this.config.getSection("items.ShardSlot"));
        } else {
            shard = new Shard(ConfigManager.getSection("items.ShardItem"), region.getShards());
        }

        InfoButton infoButton = new InfoButton(this.config.getSection("items.RegionInfoItem"), this.region);

        this.addItems(new Item(this.config.getSection("items.InfoItem"), false), shard, infoButton);
        this.insertAllItems();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (cooldown.isCancelled(event, this)) return;
        event.setCancelled(event.getRawSlot() == event.getSlot() || event.isShiftClick() || event.getClick().equals(ClickType.DOUBLE_CLICK));
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem != null) {
            Item item = this.findFirstItem(clickedItem);
            if (item != null) {
                item.onClick(event);
            }
        }
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().updateShards(this.region));
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        event.setCancelled(event.getRawSlots().stream().anyMatch(s -> s < this.getInventory().getSize()));
    }
}
