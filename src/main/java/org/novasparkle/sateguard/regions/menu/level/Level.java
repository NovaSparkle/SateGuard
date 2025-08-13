package org.novasparkle.sateguard.regions.menu.level;

import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;

public record Level(int level, int cost, int shards, int addHealth, Fear fear) {
    public Level(ConfigurationSection section) {
        this(LunaMath.toInt(section.getName()), section.getInt("cost"), section.getInt("shards"), section.getInt("addHealth"), Fear.getFear(section.getString("new")));
    }

    @Override
    public String toString() {
        return "Level{" +
                "level=" + level +
                ", cost=" + cost +
                ", shards=" + shards +
                ", addHealth=" + addHealth +
                ", fear=" + fear +
                '}';
    }
}
