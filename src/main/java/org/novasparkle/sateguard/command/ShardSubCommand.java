package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.novasparkle.lunaspring.API.commands.LunaCompleter;
import org.novasparkle.lunaspring.API.commands.ZeroArgCommand;
import org.novasparkle.lunaspring.API.commands.annotations.Check;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.configuration.IConfig;
import org.novasparkle.lunaspring.API.menus.items.NonMenuItem;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;

import java.util.List;
import java.util.stream.Collectors;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = "shard")
@Check(permissions = "sateguard.giveshard", flags = ZeroArgCommand.AccessFlag.PLAYER_ONLY)
public class ShardSubCommand implements LunaCompleter {
    @Override
    public void invoke(CommandSender commandSender, String[] strings) {
        NonMenuItem shardItem = new NonMenuItem(new IConfig(SateGuard.getInstance().getDataFolder(), "levelMenu").getSection("items.shard"));
        shardItem.setAmount(Integer.parseInt(strings[1]));
        shardItem.give((Player) commandSender);
        ConfigManager.sendMessage(commandSender, "giveShard", "amount-%-" + shardItem.getAmount());
    }
    @Override
    public List<String> tabComplete(CommandSender commandSender, List<String> list) {
        if (list.size() == 2) {
            return Utils.getSlotList(List.of("1-9")).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return null;
    }
}
