package org.novasparkle.sateguard.regions.menu.level;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.AMenu;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.VaultManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.LevelMenu;

public class LevelButton extends Item {
    private final Level level;
    private final LevelType levelType;
    private final SateRegion region;

    public LevelButton(@NonNull ConfigurationSection section, LevelType levelType, SateRegion region) {
        super(section, true);
        this.levelType = levelType;
        this.level = new Level(section);
        this.region = region;
    }

    @Override
    public Item onClick(InventoryClickEvent event) {
        event.setCancelled(event.getRawSlot() == event.getSlot());
        Player player = (Player) event.getWhoClicked();
        switch (this.levelType) {
            case OPENED -> ConfigManager.sendMessage(player,"level.openedLevel");
            case NEXT -> {
                if (!VaultManager.hasEnoughMoney(player, this.level.cost())) {
                    ConfigManager.sendMessage(player, "level.lowBalance");
                } else if (((AMenu) this.getMenu()).findFirstItem(Shard.class) == null) {
                    ConfigManager.sendMessage(player, "level.lowShards");
                } else {
                    region.setLevel(region.getLevel() + 1);
                    VaultManager.withdraw(player, this.level.cost());
                    MenuManager.openInventory(player, new LevelMenu(player, region));
                }
            }
        }
        return this;
    }
}
