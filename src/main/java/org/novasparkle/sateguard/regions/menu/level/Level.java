package org.novasparkle.sateguard.regions.menu.level;

import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.configuration.IConfig;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.SateGuard;

public record Level(int level, int cost, int shards, int addHealth, Fear fear) {
    public Level(ConfigurationSection section) {
        this(LunaMath.toInt(section.getName()), section.getInt("cost"), section.getInt("shards"), section.getInt("addHealth"), Fear.getFear(section.getString("new")));
    }
    public static Level of(int level) {
        return new Level(new IConfig(SateGuard.getInstance().getDataFolder(), "LevelMenu").getSection("levels." + level));
    }
}
