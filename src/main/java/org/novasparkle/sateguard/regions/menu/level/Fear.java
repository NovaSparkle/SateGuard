package org.novasparkle.sateguard.regions.menu.level;

import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.novasparkle.sateguard.regions.flags.CustomFlags;

import java.util.function.Consumer;

public enum Fear implements Consumer<ProtectedRegion> {
    CREEPER_EXPLOSION(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY),
    HIDE_HOLOGRAM(CustomFlags.hideHologram, StateFlag.State.ALLOW),
    TNT_EXPLOSION(Flags.TNT, StateFlag.State.DENY),
    BLOCK_INVINCIBILITY(CustomFlags.blockInvincibility, StateFlag.State.ALLOW),
    INVINCIBILITY(CustomFlags.invincibility, StateFlag.State.ALLOW);
    private final StateFlag flag;
    private final StateFlag.State state;
    Fear(StateFlag flag, StateFlag.State state) {
        this.flag = flag;
        this.state = state;
    }
    public static Fear getFear(String name) {
        if (name == null) return null;
        return valueOf(name);
    }

    @Override
    public void accept(ProtectedRegion protectedRegion) {
        protectedRegion.setFlag(flag, state);
    }
}
