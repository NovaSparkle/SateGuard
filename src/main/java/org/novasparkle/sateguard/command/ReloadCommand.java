package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.annotations.Permissions;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.lunaspring.API.commands.annotations.TabCompleteIgnore;
import org.novasparkle.sateguard.ConfigManager;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"reload", "rl"})
@Permissions(permissionList = {"sateguard.reload"})
@TabCompleteIgnore(ignoreList = "rl")
public class ReloadCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] args) {
        ConfigManager.reload();
        ConfigManager.sendMessage(sender, "reloaded");
    }
}
