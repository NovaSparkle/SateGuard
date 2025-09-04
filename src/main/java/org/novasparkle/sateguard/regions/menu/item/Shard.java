package org.novasparkle.sateguard.regions.menu.item;

import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.NBTManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.menu.LevelMenu;

public class Shard extends Item {
    public static final String nbt = "shard";
    public Shard(@NonNull ConfigurationSection section, int amount) {
        super(section, false);
        this.setAmount(amount);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Item onClick(InventoryClickEvent event) {
        if (event.getSlot() == event.getRawSlot()) {
            LevelMenu levelMenu = (LevelMenu) this.getMenu();
            ItemStack cursor = event.getCursor();
            ItemStack clickedItem = event.getCurrentItem();

            if (cursor != null && !cursor.getType().equals(Material.AIR) && NBTManager.hasTag(cursor, Shard.nbt)) {
                event.setCancelled(true);

                if (this.getAmount() >= 64 || (this.getAmount() + cursor.getAmount()) > 64) {
                    ConfigManager.sendMessage(event.getWhoClicked(), "maxShards", "amount-%-" + 64);
                    return this;
                }
                this.setAmount(cursor.getAmount() + this.getAmount());
                levelMenu.getRegion().setShards(this.getAmount());
                this.insert();

                event.setCursor(null);

            } else if (cursor != null && cursor.getType().equals(Material.AIR)
                    && clickedItem != null
                    && !clickedItem.getType().equals(Material.AIR)
                    && NBTManager.hasTag(clickedItem, Shard.nbt)) {
                event.setCancelled(true);
                event.setCursor(this.getItemStack());

                levelMenu.getRegion().setShards(0);

                levelMenu.findItems(Shard.class).forEach(i -> i.remove(levelMenu));
                ShardSlot shardSlot = new ShardSlot(levelMenu.getConfig().getSection("items.ShardSlot"));
                levelMenu.addItems(shardSlot);
                shardSlot.insert(levelMenu);
            }
        }
        return this;
    }
}
