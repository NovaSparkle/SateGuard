package org.novasparkle.sateguard.command;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.novasparkle.lunaspring.API.commands.LunaCompleter;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

import java.util.List;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = "list")
public class RegionListCommand implements LunaCompleter {

    @Override
    public void invoke(CommandSender sender, String[] args) {
        OfflinePlayer checkPlayer = null;
        if (args.length == 1) {
            checkPlayer = (OfflinePlayer) sender;
        } else if (args.length == 2) {
            checkPlayer = Bukkit.getOfflinePlayerIfCached(args[1]);
        }
        List<SateRegion> regions = RegionManager.getRegions(checkPlayer);
        if (checkPlayer != null) {
            if (regions.isEmpty())
                ConfigManager.sendMessage(sender, "commands.regionList.empty", "player-%-" + checkPlayer.getName());
            else {
                ConfigManager.sendMessage(sender, "commands.regionList.title", "player-%-" + checkPlayer.getName());
                byte i = 1;
                for (SateRegion region : regions) {
                    String status = region.getOwnerName().equals(checkPlayer.getName()) ? "owner" : "member";
                    Location regionCenter = region.getCenter();
                    ConfigManager.sendMessage(sender, "commands.regionList.regionLine",
                            "num-%-" + i++,
                            "border1-%-" + region.getRegion().getMinimumPoint().toString(),
                            "border2-%-" + region.getRegion().getMaximumPoint().toString(),
                            "world-%-" + regionCenter.getWorld().getName(),
                            "status-%-" + ConfigManager.getString("messages.commands.regionList." + status),
                            "x-%-" + regionCenter.getBlockX(),
                            "y-%-" + regionCenter.getBlockY(),
                            "z-%-" + regionCenter.getBlockZ()
                    );
                }
            }
        } else ConfigManager.sendMessage(sender, "noSuchPlayer");

    }
    @Override
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 2 && sender.hasPermission("sateguard.admin")) return Utils.getPlayerNicks(args.get(1));
        return null;
    }
}
