package org.novasparkle.sateguard;

import lombok.Getter;
import org.novasparkle.lunaspring.LunaPlugin;
import org.novasparkle.sateguard.db.DataBase;
import org.novasparkle.sateguard.event.RegionListener;
import org.novasparkle.sateguard.regions.RegionTypeList;
import org.novasparkle.sateguard.regions.flags.CustomFlags;

public final class SateGuard extends LunaPlugin {

    @Getter
    private static SateGuard instance;
    @Getter
    private static DataBase db;
    @Getter
    private final static String nbtTag = "sateguard";

    @Override
    public void onLoad() {
        CustomFlags.register();
    }
    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        this.saveDefaultConfig();
        this.registerListeners(new RegionListener());
        db = new DataBase();
        RegionTypeList.registerRegionTypes(ConfigManager.getSection("regions"));

    }
}
