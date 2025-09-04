package org.novasparkle.sateguard.regions.flags;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

public class CustomFlags {
    public static final MaximumStorages maximumStorages = new MaximumStorages("maximum-storages");
    public static final CurrentStorages currentStorages = new CurrentStorages("current-storages");
    public static final StateFlag generateShards = new StateFlag("generate-shards", true);
    public static final StateFlag hideHologram = new StateFlag("hide-hologram", false);
    public static final StateFlag blockInvincibility = new StateFlag("block-invincibility", false);
    public static final StateFlag invincibility = new StateFlag("region-invincibility", false);

    public static void register() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        registry.register(maximumStorages);
        registry.register(currentStorages);
        registry.register(generateShards);
        registry.register(hideHologram);
        registry.register(blockInvincibility);
        registry.register(invincibility);
    }
}
