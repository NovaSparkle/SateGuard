package org.novasparkle.sateguard.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.novasparkle.lunaspring.API.events.LunaHandler;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.event.JudgmentNight;

@LunaHandler
public class BossBarValidator implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    private void onJoin(PlayerJoinEvent event) {
        JudgmentNight judgmentNight = EventManager.getEvent();
        if (judgmentNight != null) {
            judgmentNight.getBossBar().addPlayer(event.getPlayer());
        }
    }
    @EventHandler(priority = EventPriority.HIGH)
    private void onQuit(PlayerQuitEvent event) {
        JudgmentNight judgmentNight = EventManager.getEvent();
        if (judgmentNight != null && judgmentNight.getBossBar().getPlayers().contains(event.getPlayer())) {
            judgmentNight.getBossBar().removePlayer(event.getPlayer());
        }
    }
}
