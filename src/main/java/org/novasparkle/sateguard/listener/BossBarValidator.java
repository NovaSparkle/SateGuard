package org.novasparkle.sateguard.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.novasparkle.lunaspring.API.commands.annotations.LunaHandler;
import org.novasparkle.sateguard.event.EventManager;
import org.novasparkle.sateguard.event.JudgmentNight;

@LunaHandler
public class BossBarValidator implements Listener {
    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        JudgmentNight judgmentNight = EventManager.getEvent();
        if (judgmentNight != null && judgmentNight.getBossBar().getPlayers().contains(event.getPlayer())) {
            judgmentNight.getBossBar().getBar().getPlayers().remove(event.getPlayer());
        }
    }
    @EventHandler
    private void onJoin(PlayerQuitEvent event) {
        JudgmentNight judgmentNight = EventManager.getEvent();
        if (judgmentNight != null) {
            judgmentNight.getBossBar().getBar().getPlayers().add(event.getPlayer());
        }
    }
}
