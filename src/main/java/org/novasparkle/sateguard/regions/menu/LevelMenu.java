package org.novasparkle.sateguard.regions.menu;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.novasparkle.lunaspring.API.menus.AMenu;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.item.InfoButton;
import org.novasparkle.sateguard.regions.menu.item.LevelButton;
import org.novasparkle.sateguard.regions.menu.level.LevelType;
import org.novasparkle.sateguard.regions.menu.item.Shard;
import org.novasparkle.sateguard.regions.menu.item.ShardSlot;

@Getter
public class LevelMenu extends AMenu {

    private final Configuration configuration;
    private final SateRegion region;
    public LevelMenu(@NonNull Player player, SateRegion region) {
        super(player);
        this.configuration = new Configuration(SateGuard.getInstance().getDataFolder(), "levelMenu");
        this.region = region;
        this.initialize(configuration.self(), true);
    }

    @Override
    public void onOpen(InventoryOpenEvent inventoryOpenEvent) {
        ConfigurationSection section = this.configuration.getSection("levels");

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
            shard = new ShardSlot(this.configuration.getSection("items.shardSlot"));
        } else {
            shard = new Shard(this.configuration.getSection("items.shard"), region.getShards());
        }

        InfoButton infoButton = new InfoButton(this.configuration.getSection("items.regionInfo"), this.region);

        this.addItems(new Item(this.configuration.getSection("items.info"), false), shard, infoButton);
        this.insertAllItems();
    }

    @Override
    public void onClick(InventoryClickEvent event) {
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
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {
        Bukkit.getScheduler().runTaskAsynchronously(SateGuard.getInstance(), () -> SateGuard.getDb().updateShards(this.region));
    }

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }
}
