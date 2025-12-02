package de.tomalbrc.balloons.storage.hikari;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteStorage extends AbstractHikariStorage {

    private final Gson gson = new Gson();
    private final Type listType = new TypeToken<List<String>>() {}.getType();

    public SqliteStorage(DatabaseConfig cfg) {
        super(StorageUtil.Type.SQLITE, cfg);

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_balloons (" +
                    "uuid TEXT PRIMARY KEY," +
                    "active TEXT," +
                    "available TEXT," +   // JSON string
                    "favourites TEXT" +   // JSON string for favourites
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create balloons table", e);
        }
    }

    @Override
    public boolean setActive(UUID playerUUID, ResourceLocation id) {
        String query = "INSERT INTO " + Balloons.MODID + "_balloons (uuid, active) " +
                "VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET active=excluded.active";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, id != null ? id.toString() : null);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeActive(UUID playerUUID) {
        String query = "UPDATE " + Balloons.MODID + "_balloons SET active = NULL WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public ResourceLocation getActive(UUID playerUUID) {
        String query = "SELECT active FROM " + Balloons.MODID + "_balloons WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String activeStr = rs.getString("active");
                    return activeStr != null ? ResourceLocation.parse(activeStr) : null;
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    private List<String> getAvailableStrings(UUID playerUUID) {
        String query = "SELECT available FROM " + Balloons.MODID + "_balloons WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("available");
                    if (json != null && !json.isEmpty()) {
                        List<String> parsed = gson.fromJson(json, listType);
                        return parsed != null ? parsed : new ArrayList<>();
                    }
                }
            }
        } catch (SQLException ignored) {}
        return new ArrayList<>();
    }

    private void setAvailableStrings(UUID playerUUID, List<String> balloons) {
        String json = gson.toJson(balloons);
        String query = "INSERT INTO " + Balloons.MODID + "_balloons (uuid, available) " +
                "VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET available=excluded.available";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, json);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private List<String> getFavStrings(UUID playerUUID) {
        String query = "SELECT favourites FROM " + Balloons.MODID + "_balloons WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = rs.getString("favourites");
                    if (json != null && !json.isEmpty()) {
                        List<String> parsed = gson.fromJson(json, listType);
                        return parsed != null ? parsed : new ArrayList<>();
                    }
                }
            }
        } catch (SQLException ignored) {}
        return new ArrayList<>();
    }

    private void setFavStrings(UUID playerUUID, List<String> favs) {
        String json = gson.toJson(favs);
        String query = "INSERT INTO " + Balloons.MODID + "_balloons (uuid, favourites) " +
                "VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET favourites=excluded.favourites";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, json);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public List<ResourceLocation> list(UUID playerUUID) {
        List<String> strings = getAvailableStrings(playerUUID);
        List<ResourceLocation> result = new ArrayList<>();
        for (String s : strings) {
            result.add(ResourceLocation.parse(s));
        }
        return result;
    }

    public boolean add(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;
        List<String> current = getAvailableStrings(playerUUID);
        if (!current.contains(id.toString())) {
            current.add(id.toString());
            setAvailableStrings(playerUUID, current);
            return true;
        }
        return false;
    }

    public boolean remove(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;
        List<String> current = getAvailableStrings(playerUUID);
        if (current.remove(id.toString())) {
            setAvailableStrings(playerUUID, current);

            // also remove from favourites if present
            List<String> favs = getFavStrings(playerUUID);
            if (favs.remove(id.toString())) {
                setFavStrings(playerUUID, favs);
            }

            return true;
        }
        return false;
    }

    public List<ResourceLocation> listFavs(UUID playerUUID) {
        List<String> favs = getFavStrings(playerUUID);
        List<ResourceLocation> result = new ArrayList<>();
        for (String s : favs) {
            result.add(ResourceLocation.parse(s));
        }
        return result;
    }

    public boolean addFav(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;
        List<String> favs = getFavStrings(playerUUID);
        if (!favs.contains(id.toString())) {
            favs.add(id.toString());
            setFavStrings(playerUUID, favs);
            return true;
        }
        return false;
    }

    public boolean removeFav(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;
        List<String> favs = getFavStrings(playerUUID);
        if (favs.remove(id.toString())) {
            setFavStrings(playerUUID, favs);
            return true;
        }
        return false;
    }
}
