package org.novasparkle.sateguard.db;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.novasparkle.lunaspring.API.util.service.managers.worldguard.GuardManager;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.regions.SateRegion;
import org.novasparkle.sateguard.regions.menu.level.Fear;

import java.util.List;

@Getter
public class DataBase {
    private final AsyncExecutor executor;
    private final String tableName;

    public DataBase() {
        this.executor = new AsyncExecutor(ConfigManager.getSection("mysql"));
        this.tableName = ConfigManager.getString("mysql.tableName");
        this.createTable(this.tableName);
    }

    @SneakyThrows
    private void createTable(String name) {
        this.executor.executeSync(String.format("CREATE TABLE IF NOT EXISTS %s " +
                "(ID int PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "RegionName varchar(50)," +
                "RegionId varchar(50)," +
                "Health int," +
                "Level int," +
                "Shards int," +
                "Fear varchar(50))", name));
    }
    @SneakyThrows
    public void serializeRegion(SateRegion region) {
        this.executor.executeAsync(String.format("INSERT INTO %s (RegionName, RegionId, Health, Level, Shards, Fear) VALUES (?, ?, ?, ?, ?, ?)", this.tableName),
                region.getRegion().getId(),
                region.getRegionType().getRegionId(),
                region.getHealth(),
                region.getLevel(),
                region.getShards(),
                region.getFear() == null ? null : region.getFear().name());
    }
    @SneakyThrows
    public void updateShards(SateRegion region) {
        this.executor.executeAsync(String.format("UPDATE %s SET Shards=? WHERE RegionName=?", this.tableName),
                region.getShards(),
                region.getRegion().getId());
    }
    @SneakyThrows
    public void updateHealth(SateRegion region) {
        this.executor.executeAsync(String.format("UPDATE %s SET Health=? WHERE RegionName=?", this.tableName),
                region.getHealth(),
                region.getRegion().getId());
    }
    @SneakyThrows
    public void updateLevel(SateRegion region) {
        this.executor.executeAsync(String.format("UPDATE %s SET Health=?, Level=?, Fear=? WHERE RegionName=?", this.tableName),
                region.getHealth(),
                region.getLevel(),
                region.getFear() == null ? null : region.getFear().name(),
                region.getRegion().getId());
    }

    public void update(SateRegion region) {
        this.executor.executeAsync(String.format("UPDATE %s SET Health=?, Shards=?, Level=?, Fear=? WHERE RegionName=?", this.tableName),
                region.getHealth(),
                region.getShards(),
                region.getLevel(),
                region.getFear() == null ? null : region.getFear().name(),
                region.getRegion().getId());
    }

    @SneakyThrows
    public void removeRegion(String name) {
        this.executor.executeAsync(String.format("DELETE FROM %s WHERE RegionName=?", this.tableName), name);
    }

    public List<SateRegion> getAllRegion() {
        ResultSetHandler<SateRegion> handler = set -> {
            String name = set.getString("RegionName");
            String[] nameArray = name.split("_");
            Location location = new Location(Bukkit.getWorld(nameArray[3]), LunaMath.toInt(nameArray[0]),LunaMath.toInt(nameArray[1]),LunaMath.toInt(nameArray[2]));
            if (GuardManager.getRegion(name) == null) {
                this.removeRegion(name);
                return null;
            }
            return new SateRegion(location, set.getString("RegionId"), set.getInt("Health"), set.getInt("Level"), set.getInt("Shards"), Fear.getFear(set.getString("Fear")));
        };
        return this.executor.executeQuery("SELECT RegionName, RegionId, Health, Level, Shards, Fear FROM " + this.tableName, handler);
    }
}
