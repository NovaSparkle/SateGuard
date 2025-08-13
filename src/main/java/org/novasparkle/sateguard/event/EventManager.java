package org.novasparkle.sateguard.event;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.command.CommandSender;
import org.novasparkle.sateguard.ConfigManager;

import java.io.*;

@UtilityClass
public class EventManager {
    @Getter
    private JudgmentNight event;
    private final String SAVE_FILE = "nightState.dat";

    public void startEvent(CommandSender sender) {
        if (event != null) {
            ConfigManager.sendMessage(sender, "event.alreadyStarted");
        } else {
            event = deserialize();
            if (event == null)
                event = new JudgmentNight();
            event.startEvent();
            ConfigManager.sendMessage(sender, "event.started");
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

    public void serialize() {
        if (event != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(""))) {
                oos.writeObject(SAVE_FILE);
            } catch (IOException e) {
                System.err.println("Ошибка сохранения состояния: " + e.getMessage());
            }
        }
    }

    public JudgmentNight deserialize() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(SAVE_FILE))) {
            return (JudgmentNight) ois.readObject();
        } catch (FileNotFoundException | EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки состояния: " + e.getMessage());
            return null;
        }
    }
}
