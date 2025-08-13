package org.novasparkle.sateguard.command;

import org.bukkit.command.CommandSender;
import org.novasparkle.lunaspring.API.commands.Invocation;
import org.novasparkle.lunaspring.API.commands.annotations.Permissions;
import org.novasparkle.lunaspring.API.commands.annotations.SubCommand;
import org.novasparkle.sateguard.event.EventManager;

@SubCommand(appliedCommand = "sateguard", commandIdentifiers = {"start", "forceRun"})
@Permissions(permissionList = {"sateguard.forceStart"})
public class ForceStartEventCommand implements Invocation {
    @Override
    public void invoke(CommandSender sender, String[] strings) {
        EventManager.startEvent(sender);
    }
}
