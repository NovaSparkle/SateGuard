package org.novasparkle.sateguard;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.novasparkle.lunaspring.API.configuration.IConfig;
import org.novasparkle.lunaspring.API.util.service.managers.ColorManager;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public final class ConfigManager {
    private final IConfig config;
    static {
        config = new IConfig(SateGuard.getInstance());
    }
    public void reload() {
        config.reload(SateGuard.getInstance());
    }
    public String getString(String path) {
        return ColorManager.color(config.getString(path));
    }
    public int getInt(String path) {
        return config.getInt(path);
    }
    public ConfigurationSection getSection(String path) {
        return config.getSection(path);
    }
    public List<String> getStringList(String path) {
        return config.getStringList(path).stream().map(ColorManager::color).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender sender, String id, String... replacements) {
        config.sendMessage(sender, id, replacements);
    }

    public static Material getMaterial(String path) {
        return Material.valueOf(getString(path));
    }

    public static boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
}
