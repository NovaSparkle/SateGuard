package org.novasparkle.sateguard.regions.flags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class CustomFlags {
    public static final MaximumStorages maximumStorages = new MaximumStorages();
    public static final StateFlag generateShards = new StateFlag("generateShards", true);

    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(maximumStorages);
        registry.register(generateShards);
    }
}
