package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.annotations.Check;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.processor.ZeroArgCommand;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = "drop")
@Check(permissions = "#.admin", flags = ZeroArgCommand.AccessFlag.PLAYER_ONLY)
public class DropRegionCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String regionName = GuardManager.getRegionsIds(player.getLocation()).stream().findFirst().orElse(null);
        if (regionName != null) {
            SateRegion sateRegion = RegionManager.getRegion(regionName);
            sateRegion.onRemove(player);
        } else {
            ConfigManager.sendMessage(player, "outOfRegion");
        }
    }
}
