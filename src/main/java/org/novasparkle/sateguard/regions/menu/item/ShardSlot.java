package org.novasparkle.sateguard.regions.menu.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.novasparkle.sateguard.regions.menu.LevelMenu;

public class ShardSlot extends Item {
    public ShardSlot(@NonNull ConfigurationSection section) {
        super(section, false);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Item onClick(InventoryClickEvent event) {
        event.setCancelled(true);
        ItemStack shardItem = event.getCursor();
        if (shardItem != null && !shardItem.getType().equals(Material.AIR) && NBTManager.hasTag(shardItem, Shard.nbt)) {
            event.setCursor(null);
            LevelMenu menu = (LevelMenu) this.getMenu();
            Configuration configuration = menu.getConfiguration();
            menu.getRegion().setShards(menu.getRegion().getShards() + shardItem.getAmount());

            menu.findItems(ShardSlot.class).forEach(i -> i.remove(menu));
            Shard shard = new Shard(configuration.getSection("items.shard"), shardItem.getAmount());
            shard.setSlot(this.getSlot());
            menu.addItems(shard);
            shard.insert(menu);
        }
        return this;
    }
}
