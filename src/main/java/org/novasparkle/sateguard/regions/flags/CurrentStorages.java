package org.novasparkle.sateguard.regions.flags;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import org.jetbrains.annotations.Nullable;

public class CurrentStorages extends Flag<Integer> {
    protected CurrentStorages(String name) {
        super(name);
    }

    @Override
    public Integer parseInput(FlagContext flagContext)  {
        return null;
    }

    @Override
    public Integer unmarshal(@Nullable Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Object marshal(Integer integer) {
        return integer;
    }
}
