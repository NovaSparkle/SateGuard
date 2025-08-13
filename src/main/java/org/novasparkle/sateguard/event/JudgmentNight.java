package org.novasparkle.sateguard.event;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;
import org.novasparkle.lunaspring.API.util.utilities.AnnounceUtils;
import org.novasparkle.lunaspring.API.util.utilities.LunaBossBar;
import org.novasparkle.lunaspring.API.util.utilities.LunaTask;
import org.novasparkle.lunaspring.API.util.utilities.Utils;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.RegionManager;
import org.novasparkle.sateguard.regions.SateRegion;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class JudgmentNight implements Serializable {
    @Getter
    private final NightBossBar bossBar;
    private final int lifeTime;
    private final LocalDateTime endTime;
    private final BarUpdater updater;

    public JudgmentNight() {

        ConfigurationSection nightSection = ConfigManager.getSection("settings.night");
        assert nightSection != null;
        this.lifeTime = nightSection.getInt("endIn");
        this.updater = new BarUpdater(lifeTime);
        this.bossBar = new NightBossBar(Objects.requireNonNull(nightSection.getString("title")), nightSection.getString("barColor"), nightSection.getString("barStyle"), SateGuard.getInstance());
        this.endTime = LocalDateTime.now().plusSeconds(lifeTime);
    }

    private void scheduleEventEnd() {
        long betweenSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), this.endTime);
        Bukkit.getScheduler().runTaskLater(SateGuard.getInstance(), () -> EventManager.stopEvent(Bukkit.getConsoleSender()), betweenSeconds * 20);
    }

    private void updateBar() {
        long betweenSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), this.endTime);
        this.bossBar.setProgress((float) (betweenSeconds) / lifeTime);
        Duration duration = Duration.ofSeconds(betweenSeconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        this.bossBar.updateTitle(this.bossBar.getDefaultTitle().replace("[lifeTime]", String.format("%02d:%02d:%02d", hours, minutes, seconds)));
    }

    public void startEvent() {
        this.scheduleEventEnd();

        this.bossBar.getPlayers().forEach(p -> p.showBossBar((BossBar) this.bossBar.getBar()));
        this.updater.runTaskAsynchronously(SateGuard.getInstance());
        this.applyFlags();
        Bukkit.getScheduler().runTask(SateGuard.getInstance(), () -> Bukkit.getWorlds().forEach(world -> {
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            this.animateSunset(world);
        }));

        Utils.playersAction(player -> {
            bossBar.addPlayer(player);
            this.sendTitle(player, ConfigManager.getSection("messages.event.nightStarted"));
            ConfigManager.sendMessage(player, "messages.event.nightStarted.broadcast");
        });
    }

    private void sendTitle(Player player, ConfigurationSection section) {
        String title = ColorManager.color(section.getString("title"));
        String subTitle = ColorManager.color(section.getString("subTitle"));

        AnnounceUtils.title(player, title, subTitle);
        BukkitScheduler scheduler = Bukkit.getScheduler();

        scheduler.runTaskLaterAsynchronously(SateGuard.getInstance(), () -> {
            AnnounceUtils.title(player, title, subTitle);

            scheduler.runTaskLaterAsynchronously(SateGuard.getInstance(), () -> {
                AnnounceUtils.title(player, title, subTitle);
            }, 45);
        }, 45);
    }

    private void applyFlags() {
        for (SateRegion sateRegion : RegionManager.getRegionList()) {
            sateRegion.onNightStarted();
        }
    }

    private void animateSunset(World world) {
        boolean newDay = false;
        long currentTime = world.getTime();
        if (currentTime < 18000L) newDay = true;

        while (true) {
            if (currentTime == 24000L) {
                currentTime = 0;
                newDay = true;
            }

            if (newDay && currentTime == 18000L) {
                break;
            }
            world.setTime(currentTime);
            currentTime++;
        }
    }

    public void stopEvent() {
        for (SateRegion sateRegion : RegionManager.getRegionList()) {
            sateRegion.onStopEvent();
        }
        Utils.playersAction(player -> {
            bossBar.removePlayer(player);
            this.sendTitle(player, ConfigManager.getSection("messages.event.nightEnded"));
            ConfigManager.sendMessage(player, "messages.event.nightEnded.broadcast");
        });
        Bukkit.getWorlds().forEach(world -> world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true));
        this.bossBar.delete();
        this.updater.cancel();
    }

    public class BarUpdater extends LunaTask implements Serializable {
        private int seconds;
        public BarUpdater(int seconds) {
            super(0);
            this.seconds = seconds;
        }

        @Override
        @SneakyThrows
        public void start() {
            while (this.seconds > 0) {
                if (!this.isActive() || EventManager.getEvent() == null) return;
                this.seconds--;
                JudgmentNight.this.updateBar();
                Thread.sleep(1000L);
            }
        }
    }

    public static class NightBossBar extends LunaBossBar implements Serializable {

        public NightBossBar(@NotNull String title, String strBarColor, String strBarStyle, @NotNull Plugin plugin) {
            super(title, strBarColor, strBarStyle, plugin);
        }
    }
}
