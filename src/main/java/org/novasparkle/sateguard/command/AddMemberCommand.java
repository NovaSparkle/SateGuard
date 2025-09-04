package org.novasparkle.sateguard.command;

import com.sk89q.worldguard.domains.DefaultDomain;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.LunaCompleter;
import org.novasparkle.lunaspring.API.commands.annotations.Args;
import org.novasparkle.lunaspring.API.commands.annotations.Flags;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.processor.ZeroArgCommand;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

import java.util.List;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"add"})
@Flags(ZeroArgCommand.AccessFlag.PLAYER_ONLY)
@Args(max = 2, min = 2)
public class AddMemberCommand implements LunaCompleter {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        String regionName = GuardManager.getRegionsIds(player.getLocation()).stream().findFirst().orElse(null);
        if (regionName != null) {
            SateRegion sateRegion = RegionManager.getRegion(regionName);
            if (sateRegion.getOwnerName().equals(player.getName()) || player.hasPermission("sateguard.admin")) {
                OfflinePlayer oPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
                if (oPlayer == null) {
                    ConfigManager.sendMessage(player, "noSuchPlayer", "name-%-" + args[1]);
                    return;
                }
                DefaultDomain domain = sateRegion.getRegion().getMembers();
                if (domain.contains(oPlayer.getUniqueId())) {
                    ConfigManager.sendMessage(player, "commands.alreadyMember", "player-%-" + oPlayer.getName());
                    return;
                }
                domain.addPlayer(oPlayer.getUniqueId());
                sateRegion.getRegion().setMembers(domain);
                ConfigManager.sendMessage(player, "commands.newMember", "player-%-" + oPlayer.getName());
            }
        } else {
            ConfigManager.sendMessage(player, "outOfRegion");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> args) {
        if (args.size() == 2) return Utils.getPlayerNicks(args.get(1));
        return null;
    }
}
