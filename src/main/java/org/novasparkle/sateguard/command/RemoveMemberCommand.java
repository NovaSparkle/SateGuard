package org.novasparkle.sateguard.command;

import com.sk89q.worldguard.domains.DefaultDomain;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.ZeroArgCommand;
import org.novasparkle.lunaspring.API.commands.annotations.Args;
import org.novasparkle.lunaspring.API.commands.annotations.Flags;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.annotations.TabCompleteIgnore;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"remove", "delete"})
@Flags(flagList = ZeroArgCommand.AccessFlag.PLAYER_ONLY)
@TabCompleteIgnore(ignoreList = "delete")
@Args(max = Integer.MAX_VALUE, min = 2)
public class RemoveMemberCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String regionName = GuardManager.getRegionsIds(player.getLocation()).stream().findFirst().orElse(null);
        if (regionName != null) {
            SateRegion sateRegion = RegionManager.getRegion(regionName);
            if (sateRegion.getOwnerName().equals(player.getName()) || player.hasPermission("sateguard.admin")) {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayer(args[1]);
                if (!oPlayer.hasPlayedBefore()) {
                    ConfigManager.sendMessage(player, "noSuchPlayer", "name-%-" + oPlayer.getName());
                    return;
                }
                DefaultDomain domain = sateRegion.getRegion().getMembers();
                if (!domain.contains(oPlayer.getUniqueId())) {
                    ConfigManager.sendMessage(player, "commands.notMember", "player-%-" + oPlayer.getName());
                    return;
                }
                domain.removePlayer(oPlayer.getUniqueId());
                sateRegion.getRegion().setMembers(domain);
                ConfigManager.sendMessage(player, "commands.removedMember", "player-%-" + oPlayer.getName());
            }
        } else {
            ConfigManager.sendMessage(player, "outOfRegion");
        }
    }
}
