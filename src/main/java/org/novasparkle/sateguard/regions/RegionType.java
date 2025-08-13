package org.novasparkle.sateguard.regions;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public class RegionType {
    private final String regionId;
    private final Material material;
    private final String name;
    private final int radius;
    private final int maxLevel;
    private final int startHealth;

    public RegionType(ConfigurationSection section) {
        this.regionId = section.getName();
        this.name = section.getString("displayName");
        this.radius = section.getInt("radius");
        this.material = Material.valueOf(section.getString("material"));
        this.maxLevel = section.getInt("maxLevel");
        this.startHealth = section.getInt("health");
    }
}
