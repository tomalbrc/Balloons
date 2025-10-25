package de.tomalbrc.balloons.storage.hikari;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.ResourceLocation;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PostgresStorage extends AbstractHikariStorage {

    public PostgresStorage(DatabaseConfig cfg) {
        super(StorageUtil.Type.POSTGRESQL, cfg);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_balloons (" +
                    "uuid TEXT PRIMARY KEY," +
                    "active TEXT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_available (" +
                    "uuid TEXT NOT NULL," +
                    "balloon TEXT NOT NULL," +
                    "PRIMARY KEY (uuid, balloon)" +
                    ")");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create balloons tables", e);
        }
    }

    @Override
    public boolean setActive(UUID playerUUID, ResourceLocation id) {
        String query = "INSERT INTO " + Balloons.MODID + "_balloons (uuid, active) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (uuid) DO UPDATE SET active = EXCLUDED.active";
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

    public boolean add(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;

        String query = "INSERT INTO " + Balloons.MODID + "_available (uuid, balloon) " +
                "VALUES (?, ?) ON CONFLICT (uuid, balloon) DO NOTHING";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean remove(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;

        String query = "DELETE FROM " + Balloons.MODID + "_available WHERE uuid = ? AND balloon = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<ResourceLocation> list(UUID playerUUID) {
        String query = "SELECT balloon FROM " + Balloons.MODID + "_available WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                List<ResourceLocation> result = new ArrayList<>();
                while (rs.next()) {
                    String balloonStr = rs.getString("balloon");
                    if (balloonStr != null) result.add(ResourceLocation.parse(balloonStr));
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
