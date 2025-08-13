package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.annotations.Permissions;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.sateguard.event.EventManager;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"stop", "forceStop"})
@Permissions(permissionList = {"sateguard.forceStop"})
public class ForceStopEventCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] strings) {
        EventManager.stopEvent(sender);
    }
}
