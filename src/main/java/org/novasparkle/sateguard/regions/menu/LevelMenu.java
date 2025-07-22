package org.novasparkle.sateguard.regions.menu;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.novasparkle.lunaspring.API.configuration.Configuration;
import org.novasparkle.lunaspring.API.menus.AMenu;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.level.LevelButton;
import org.novasparkle.sateguard.regions.menu.level.LevelType;

public class LevelMenu extends AMenu {
    private final Configuration configuration;
    private final SateRegion region;
    public LevelMenu(@NonNull Player player, SateRegion region) {
        super(player);
        this.configuration = new Configuration(SateGuard.getInstance().getDataFolder(), "levelMenu.yml");
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
        this.addItems(new Item(this.configuration.getSection("items.info"), false));
    }

    @Override
    public void onClick(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public void onClose(InventoryCloseEvent inventoryCloseEvent) {}

    @Override
    public void onDrag(InventoryDragEvent inventoryDragEvent) {
        inventoryDragEvent.setCancelled(true);
    }
}
