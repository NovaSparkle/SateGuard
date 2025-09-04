package org.novasparkle.sateguard.regions.menu.item;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.novasparkle.lunaspring.API.menus.MenuManager;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.service.managers.VaultManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.LevelMenu;
import org.novasparkle.sateguard.regions.menu.level.Level;
import org.novasparkle.sateguard.regions.menu.level.LevelType;

import java.util.List;

public class LevelButton extends Item {
    private final Level level;
    private final LevelType levelType;
    private final SateRegion region;

    public LevelButton(@NonNull ConfigurationSection section, LevelType levelType, SateRegion region) {
        super(levelType.getMaterial(), section.getInt("amount"));
        this.setDisplayName(section.getString("displayName"));
        this.setSlot((byte) LunaMath.getIndex(section.getInt("slot.row"), section.getInt("slot.column")));
        this.setGlowing(levelType.isEnchanted());

        this.level = new Level(section);

        this.region = region;
        ConfigurationSection levelsSection = section.getParent();
        assert levelsSection != null;
        int currentHealth = this.region.getRegionType().getStartHealth();
        for (String key : levelsSection.getKeys(false)) {
            if (Integer.parseInt(key) < this.level.level()) {
                currentHealth += levelsSection.getInt(key + ".addHealth");
            } else break;
        }
        List<String> lore = section.getStringList("lore");
        int finalCurrentHealth = currentHealth;
        lore.replaceAll(l -> l.replace("[levelStatus]", levelType.getLocalName())
                .replace("[cost]", String.valueOf(this.level.cost()))
                .replace("[shards]", String.valueOf(this.level.shards()))
                .replace("[hp]", String.valueOf(this.level.addHealth() + finalCurrentHealth)));

        this.setLore(lore);
        this.levelType = levelType;
    }

    @Override
    public Item onClick(InventoryClickEvent event) {
        event.setCancelled(event.getRawSlot() == event.getSlot());
        Player player = (Player) event.getWhoClicked();
        LevelMenu levelMenu = ((LevelMenu) this.getMenu());
        switch (this.levelType) {
            case OPENED -> ConfigManager.sendMessage(player,"level.openedLevel");
            case NEXT -> {
                if (EventManager.getEvent() != null) {
                    ConfigManager.sendMessage(player, "event.forbiddenAtNight");

                } else if (this.level.level() > region.getRegionType().getMaxLevel()) {
                    ConfigManager.sendMessage(player, "level.maxLevel");

                } else if (!VaultManager.hasEnoughMoney(player, this.level.cost())) {
                    ConfigManager.sendMessage(player, "level.lowBalance");

                } else if (this.region.getShards() < this.level.shards()) {
                    ConfigManager.sendMessage(player, "level.lowShards");

                } else {
                    VaultManager.withdraw(player, this.level.cost());
                    this.region.setLevel(this.level);
                    this.region.setShards(region.getShards() - level.shards());
                    LevelMenu newLevelMenu = new LevelMenu(player, region);
                    List<LevelMenu> levelMenus = MenuManager.getActiveMenus(LevelMenu.class, true).filter(menu -> menu.getRegion().equals(this.region) && !menu.getPlayer().equals(player)).toList();
                    levelMenus.forEach(menu -> MenuManager.openInventory(newLevelMenu.copy(menu.getPlayer())));
                    MenuManager.openInventory(newLevelMenu);
                }
            }
        }
        return this;
    }
}
