package org.novasparkle.sateguard;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.novasparkle.lunaspring.API.commands.LunaExecutor;
import org.novasparkle.lunaspring.LunaPlugin;
import org.novasparkle.sateguard.db.DataBase;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.event.JudgmentNight;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.RegionTypeList;
import org.novasparkle.sateguard.regions.SateRegion;
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
        this.processListeners();
        db = new DataBase();
        this.loadFile("LevelMenu.yml");
        RegionTypeList.registerRegionTypes(ConfigManager.getSection("regions"));
        LunaExecutor.initialize(this);
        RegionManager.load();

        JudgmentNight event = EventManager.deserialize();
        if (event != null) EventManager.startEvent(event);


        this.createPlaceholder("sg", (player, parameter) -> {
            if (player == null) return "Игрок не найден";
            if (parameter.startsWith("health")) {
                String[] splitedName = parameter.split("!");
                if (splitedName.length < 2) {
                    return "Регион не найден";
                }
                SateRegion region = RegionManager.getRegion(splitedName[1]);
                if (region == null)
                    return "Регион не найден";
                return String.valueOf(region.getHealth());
            }
            return "Неверный идентификатор";
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        EventManager.serialize();
        EventManager.stopEvent(Bukkit.getConsoleSender());

        RegionManager.serializeAll();
        db.getExecutor().shutdown();
    }
}
