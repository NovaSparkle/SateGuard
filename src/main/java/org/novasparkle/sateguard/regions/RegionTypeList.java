package org.novasparkle.sateguard.regions;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Objects;

public class RegionTypeList {
    @Getter
    private final static List<RegionType> regionTypes = Lists.newArrayList();
    public static void registerRegionTypes(ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            RegionType regionType = new RegionType(Objects.requireNonNull(section.getConfigurationSection(key)));
            regionTypes.add(regionType);
        }
    }
    public static RegionType getRegionType(String id) {
        return RegionTypeList.getRegionTypes().stream().filter(rg -> rg.getRegionId().equals(id)).findFirst().orElse(null);
    }
    public static RegionType getRegionType(Material material) {
        return RegionTypeList.getRegionTypes().stream().filter(rg -> rg.getMaterial().equals(material)).findFirst().orElse(null);
    }
}
