package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.ZeroArgCommand;
import org.novasparkle.lunaspring.API.commands.annotations.Flags;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.annotations.TabCompleteIgnore;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

import java.util.Set;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"info", "i"})
@Flags(flagList = ZeroArgCommand.AccessFlag.PLAYER_ONLY)
@TabCompleteIgnore(ignoreList = "i")
public class RegionInfoCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String regionName = GuardManager.getRegionsIds(player.getLocation()).stream().findFirst().orElse(null);

        if (regionName != null) {
            SateRegion sateRegion = RegionManager.getRegion(regionName);
            Set<String> memberList = sateRegion.getRegion().getMembers().getPlayers();
            String members = memberList.isEmpty() ? ConfigManager.getString("messages.commands.noMembers") : String.join(", ", memberList);

            if (sateRegion.getOwnerName().equals(player.getName()) || player.hasPermission("sateguard.admin")) {
                ConfigManager.sendMessage(player, "commands.info",
                        "regionName-%-" + sateRegion.getRegionType().getName(),
                        "owner-%-" + sateRegion.getOwnerName(),
                        "members-%-" + members,
                        "border1-%-" + sateRegion.getRegion().getMinimumPoint().toString(),
                        "border2-%-" + sateRegion.getRegion().getMaximumPoint().toString());
            }
        } else {
            ConfigManager.sendMessage(player, "outOfRegion");
        }
    }
}
