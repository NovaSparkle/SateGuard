package org.novasparkle.sateguard.regions.menu.level;

import lombok.Getter;
import org.bukkit.Material;
import org.novasparkle.sateguard.ConfigManager;
@Getter
public enum LevelType {
    OPENED,
    NEXT,
    CLOSED;

    private final Material material;
    private final boolean enchanted;
    private final String localName;

    LevelType() {
        this.material = ConfigManager.getMaterial(String.format("levelTypes.%s.material", this.name()));
        this.enchanted = ConfigManager.getBoolean(String.format("levelTypes.%s.enchanted", this.name()));
        this.localName = ConfigManager.getString(String.format("levelTypes.%s.localName", this.name()));
    }

}
