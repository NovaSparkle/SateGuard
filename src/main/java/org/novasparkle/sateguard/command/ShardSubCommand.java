package org.novasparkle.sateguard.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.LunaCompleter;
import org.novasparkle.lunaspring.API.commands.annotations.Args;
import org.novasparkle.lunaspring.API.commands.annotations.Check;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.processor.ZeroArgCommand;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;

import java.util.List;
import java.util.stream.Collectors;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = "shard")
@Check(permissions = "#.giveshard", flags = ZeroArgCommand.AccessFlag.PLAYER_ONLY)
@Args(min = 2, max = 3)
public class ShardSubCommand implements LunaCompleter {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        NonMenuItem shardItem = new NonMenuItem(ConfigManager.getSection("items.ShardItem"));
        Player receiver = null;
        int amount = 0;
        if (args.length == 2) {
            receiver = (Player) sender;
            amount = Integer.parseInt(args[1]);
        } else if (args.length == 3) {
            receiver = Bukkit.getPlayer(args[1]);
            if (receiver == null) {
                ConfigManager.sendMessage(sender, "playerOffline", "player-%-" + args[2]);
                return;
            }
            amount = Integer.parseInt(args[2]);
        }
        shardItem.setAmount(amount);
        assert receiver != null;
        shardItem.give(receiver);

        ConfigManager.sendMessage(sender, "giveShard", "amount-%-" + shardItem.getAmount(), "player-%-" + receiver.getName());
    }
    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list) {
        switch (list.size()) {
            case 1 -> {
                return Utils.getPlayerNicks(list.get(0));
            }
            case 2 -> {
                return Utils.getSlotList(List.of("1-9")).stream().map(String::valueOf).collect(Collectors.toList());
            }
            default -> {
                return null;
            }
        }
    }
}
