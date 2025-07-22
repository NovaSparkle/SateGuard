package org.novasparkle.sateguard.db;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.novasparkle.lunaspring.API.util.utilities.LunaMath;
import org.novasparkle.sateguard.ConfigManager;
import org.novasparkle.sateguard.SateGuard;
import org.novasparkle.sateguard.regions.SateRegion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Getter
public class DataBase {
    private Connection connection;
    private final String tableName;
    @SneakyThrows
    public DataBase() {
        String url = ConfigManager.getString("mysql.url");
        String username = ConfigManager.getString("mysql.username");
        String password = ConfigManager.getString("mysql.password");
        synchronized (SateGuard.getInstance()) {
            if (this.getConnection() == null || this.getConnection().isClosed()) {
                Class.forName(ConfigManager.getString("mysql.driverClass"));
                this.connection = DriverManager.getConnection(url, username, password);
            }
        }
        this.tableName = ConfigManager.getString("mysql.tableName");
        this.createTable(this.tableName);
    }

    @SneakyThrows
    public void createTable(String name) {
        try (PreparedStatement statement = this.connection.prepareStatement(String.format("create table if not exists %s " +
                "(ID int PRIMARY KEY NOT NULL AUTO_INCREMENT," +
                "RegionName varchar(50)," +
                "RegionId varchar(50)," +
                "Health int," +
                "Level int," +
                "Shards int)", name))) {
            statement.executeUpdate();
        }
    }
    @SneakyThrows
    public void serializeRegion(SateRegion region) {
        try (PreparedStatement statement = this.connection.prepareStatement(String.format("INSERT INTO %s (RegionName, RegionId, Health, Level) VALUES (?, ?, ?, ?)", this.tableName))) {
            statement.setString(1, region.getRegion().getId());
            statement.setString(2, region.getRegionType().getRegionId());
            statement.setInt(3, region.getHealth());
            statement.setInt(4, region.getLevel());
            statement.setInt(5, region.getShards());
            statement.executeUpdate();
        }
    }

    @SneakyThrows
    public SateRegion getRegion(String name) {
        SateRegion sateRegion;
        try (PreparedStatement statement = this.connection.prepareStatement(String.format("SELECT FROM %s WHERE RegionName=%s", this.tableName, name))) {
            ResultSet set = statement.executeQuery();
            String[] nameArray = set.getString(1).split("_");
            Location location = new Location(Bukkit.getWorld(nameArray[3]), LunaMath.toInt(nameArray[0]),LunaMath.toInt(nameArray[1]),LunaMath.toInt(nameArray[2]));
            sateRegion = new SateRegion(location, set.getString(2), set.getInt(3), set.getInt(4), set.getInt(5));
        }
        return sateRegion;
    }

    @SneakyThrows
    public void removeRegion(String name) {
        try (PreparedStatement statement = this.connection.prepareStatement(String.format("DELETE FROM %s WHERE RegionName=?", this.tableName))) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }
}
