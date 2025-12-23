package de.tomalbrc.balloons.storage.hikari;

import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.Identifier;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MariaStorage extends AbstractHikariStorage {
    public MariaStorage(DatabaseConfig cfg) {
        super(StorageUtil.Type.MARIADB, cfg);

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_balloons (" +
                    "uuid VARCHAR(36) PRIMARY KEY," +
                    "active VARCHAR(255)" +
                    ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_balloons_items (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "balloon VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (uuid, balloon)" +
                    ") ENGINE=InnoDB");

            stmt.execute("CREATE TABLE IF NOT EXISTS " + Balloons.MODID + "_balloons_favourites (" +
                    "uuid VARCHAR(36) NOT NULL," +
                    "balloon VARCHAR(255) NOT NULL," +
                    "PRIMARY KEY (uuid, balloon)" +
                    ") ENGINE=InnoDB");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create balloons tables", e);
        }
    }

    @Override
    public boolean setActive(UUID playerUUID, Identifier id) {
        String query = "INSERT INTO " + Balloons.MODID + "_balloons (uuid, active) " +
                "VALUES (?, ?) ON DUPLICATE KEY UPDATE active = VALUES(active)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Identifier getActive(UUID playerUUID) {
        String query = "SELECT active FROM " + Balloons.MODID + "_balloons WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String activeStr = rs.getString("active");
                    return activeStr != null ? Identifier.parse(activeStr) : null;
                }
            }
        } catch (SQLException ignored) {}
        return null;
    }

    @Override
    public boolean add(UUID playerUUID, Identifier id) {
        if (id == null) return false;

        String query = "INSERT IGNORE INTO " + Balloons.MODID + "_balloons_items (uuid, balloon) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean remove(UUID playerUUID, Identifier id) {
        if (id == null) return false;

        String query = "DELETE FROM " + Balloons.MODID + "_balloons_items WHERE uuid = ? AND balloon = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Identifier> list(UUID playerUUID) {
        String query = "SELECT balloon FROM " + Balloons.MODID + "_balloons_items WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Identifier> result = new ArrayList<>();
                while (rs.next()) {
                    String balloonStr = rs.getString("balloon");
                    if (balloonStr != null) result.add(Identifier.parse(balloonStr));
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean addFav(UUID player, Identifier id) {
        if (id == null) return false;
        String query = "INSERT IGNORE INTO " + Balloons.MODID + "_balloons_favourites (uuid, balloon) VALUES (?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeFav(UUID player, Identifier id) {
        if (id == null) return false;
        String query = "DELETE FROM " + Balloons.MODID + "_balloons_favourites WHERE uuid = ? AND balloon = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.toString());
            stmt.setString(2, id.toString());
            return stmt.executeUpdate() > 0;
        } catch (SQLException ignored) {}

        return false;
    }

    @Override
    public List<Identifier> listFavs(UUID player) {
        String query = "SELECT balloon FROM " + Balloons.MODID + "_balloons_favourites WHERE uuid = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, player.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                List<Identifier> result = new ArrayList<>();
                while (rs.next()) {
                    String balloonStr = rs.getString("balloon");
                    if (balloonStr != null) result.add(Identifier.parse(balloonStr));
                }
                return result;
            }
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }
}
