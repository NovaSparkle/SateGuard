package org.novasparkle.sateguard.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.annotations.Flags;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.annotations.TabCompleteIgnore;
import org.novasparkle.lunaspring.API.commands.processor.ZeroArgCommand;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"info", "i"})
@Flags(ZeroArgCommand.AccessFlag.PLAYER_ONLY)
@TabCompleteIgnore("i")
public class RegionInfoCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String regionName = GuardManager.getRegionsIds(player.getLocation()).stream().findFirst().orElse(null);

        if (regionName != null) {
            SateRegion sateRegion = RegionManager.getRegion(regionName);
            Set<UUID> memberList = sateRegion.getRegion().getMembers().getUniqueIds();
            if (player.getName().equals(sateRegion.getOwnerName()) || memberList.contains(player.getUniqueId()) || player.hasPermission("sateguard.admin")) {
                String members = memberList.isEmpty() ? ConfigManager.getString("messages.commands.noMembers") : String.join(", ", memberList.stream().map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.toList()));

                ConfigManager.sendMessage(player, "commands.info",
                        "regionName-%-" + sateRegion.getRegionType().getName(),
                        "owner-%-" + sateRegion.getOwnerName(),
                        "members-%-" + members,
                        "border1-%-" + sateRegion.getRegion().getMinimumPoint().toString(),
                        "border2-%-" + sateRegion.getRegion().getMaximumPoint().toString());

            } else ConfigManager.sendMessage(sender, "forbiddenForeignRegion");
        } else {
            ConfigManager.sendMessage(player, "outOfRegion");
        }
    }
}
