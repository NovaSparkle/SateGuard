package org.novasparkle.sateguard.regions.menu.item;

import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.menus.items.Item;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.regions.SateRegion;

public class InfoButton extends Item {
    public InfoButton(ConfigurationSection section, SateRegion region) {
        super(section, false);
        this.replaceLore(lore -> Utils.applyReplacements(lore,
                "owner-%-" + region.getOwnerName(),
                "type-%-" + region.getRegionType().getName(),
                "radius-%-" + region.getRegionType().getRadius(),
                "health-%-" + region.getHealth(),
                "maxHealth-%-" + region.getRegionType().getStartHealth()));
    }
}
