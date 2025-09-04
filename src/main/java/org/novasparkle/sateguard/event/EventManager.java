package org.novasparkle.sateguard.event;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;

import java.io.*;
import java.time.LocalDateTime;

@UtilityClass
public class EventManager {
    @Getter
    private JudgmentNight event;
    private final String SAVE_FILE = "NightState.dat";

    public void startEvent(CommandSender sender) {
        if (event != null) {
            ConfigManager.sendMessage(sender, "event.alreadyStarted");
        } else {
            event = new JudgmentNight();
            event.startEvent();
            ConfigManager.sendMessage(sender, "event.started");
        }
    }
    public void startEvent(@NotNull JudgmentNight judgmentNight) {
        if (event != null) {
            ConfigManager.sendMessage(Bukkit.getConsoleSender(), "event.alreadyStarted");
        } else {
            event = judgmentNight;
            event.startEvent();
            ConfigManager.sendMessage(Bukkit.getConsoleSender(), "event.started");
        }
    }

    public void stopEvent(CommandSender sender) {
        if (event == null) {
            ConfigManager.sendMessage(sender, "event.eventNotActive");
        } else {
            event.stopEvent();
            event = null;
            ConfigManager.sendMessage(sender, "event.stopped");
        }
    }

    @SneakyThrows
    public void serialize() {
        if (event != null) {
            File file = new File(SateGuard.getInstance().getDataFolder(), SAVE_FILE);
            if (!file.exists() && file.createNewFile()) {
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream(file))) {
                    oos.writeObject(event.getEndTime());
                } catch (IOException e) {
                    System.err.println("Ошибка сохранения состояния: " + e.getMessage());
                }
            }

        }
    }

    public JudgmentNight deserialize() {
        File file = new File(SateGuard.getInstance().getDataFolder(), SAVE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                LocalDateTime endTime = (LocalDateTime) ois.readObject();
                return new JudgmentNight(endTime);
            } catch (FileNotFoundException | EOFException e) {
                return null;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ошибка загрузки состояния: " + e.getMessage());
                return null;
            } finally {
                file.delete();
            }
        }
        return null;
    }
}
