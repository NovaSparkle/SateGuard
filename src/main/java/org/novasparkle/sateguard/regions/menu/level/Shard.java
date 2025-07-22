package org.novasparkle.sateguard.regions.menu.level;

import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;

public class Shard extends NonMenuItem {
    public Shard(@NonNull ConfigurationSection section, int amount) {
        super(section);
        this.setAmount(amount);
    }
}
