package de.tomalbrc.balloons.storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.Identifier;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class MongoStorage implements StorageUtil.Provider {

    private final MongoClient client;
    private final MongoCollection<Document> collection;
    private final LoadingCache<String, String> activeCache;

    public MongoStorage(DatabaseConfig mongoConfig) {
        this.client = createClient(mongoConfig);
        MongoDatabase database = client.getDatabase(mongoConfig.databaseName);
        this.collection = database.getCollection(Balloons.MODID);

        this.activeCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.SECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public @NotNull String load(@NotNull String uuid) {
                        return loadActiveStringFromDb(uuid);
                    }
                });
    }

    public static MongoClient createClient(DatabaseConfig config) {
        ConnectionString connString = new ConnectionString(config.mongoConnectionString());
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToConnectionPoolSettings(b -> b.maxSize(config.maxPoolSize))
                .build();
        return MongoClients.create(settings);
    }

    public void close() {
        client.close();
    }

    @Override
    public boolean addFav(UUID player, Identifier id) {
        if (id == null) return false;
        String idStr = id.toString();

        UpdateResult res = collection.updateOne(
                eq("_id", player),
                Updates.addToSet("favourites", idStr),
                new UpdateOptions().upsert(true)
        );

        return res.wasAcknowledged() && (res.getModifiedCount() > 0 || res.getUpsertedId() != null);
    }

    @Override
    public boolean removeFav(UUID player, Identifier id) {
        if (id == null) return false;
        String idStr = id.toString();

        UpdateResult res = collection.updateOne(
                eq("_id", player),
                Updates.pull("favourites", idStr)
        );

        return res.wasAcknowledged() && res.getModifiedCount() > 0;
    }

    @Override
    public List<Identifier> listFavs(UUID player) {
        Document doc = collection.find(eq("_id", player))
                .projection(Projections.include("favourites"))
                .first();
        if (doc == null || !doc.containsKey("favourites")) return Collections.emptyList();

        List<String> raw = doc.getList("favourites", String.class);
        if (raw == null) return Collections.emptyList();

        List<Identifier> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            Identifier rl = Identifier.tryParse(s);
            if (rl != null) out.add(rl);
        }
        return out;
    }

    private String loadActiveStringFromDb(String uuidStr) {
        Document doc = collection.find(eq("_id", UUID.fromString(uuidStr)))
                .projection(Projections.include("active"))
                .first();
        if (doc == null) return "";
        String a = doc.getString("active");
        return a == null ? "" : a;
    }

    @Override
    public boolean setActive(UUID playerUUID, Identifier id) {
        String idStr = id == null ? null : id.toString();
        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                Updates.set("active", idStr),
                new UpdateOptions().upsert(true));
        activeCache.invalidate(playerUUID.toString());
        return res.wasAcknowledged() && (res.getModifiedCount() > 0 || res.getUpsertedId() != null);
    }

    @Override
    public boolean removeActive(UUID playerUUID) {
        Document doc = collection.find(eq("_id", playerUUID))
                .projection(Projections.include("available"))
                .first();

        boolean success;
        if (doc == null) {
            activeCache.invalidate(playerUUID.toString());
            return false;
        }

        if (!doc.containsKey("available")) {
            var delRes = collection.deleteOne(eq("_id", playerUUID));
            activeCache.invalidate(playerUUID.toString());
            success = delRes.getDeletedCount() > 0;
        } else {
            var updRes = collection.updateOne(eq("_id", playerUUID),
                    Updates.unset("active"));
            activeCache.invalidate(playerUUID.toString());
            success = updRes.wasAcknowledged() && updRes.getModifiedCount() > 0;
        }

        return success;
    }

    public Identifier getActive(UUID playerUUID) {
        String uuidStr = playerUUID.toString();
        try {
            String activeStr = activeCache.get(uuidStr);
            if (activeStr == null || activeStr.isEmpty()) return null;
            return Identifier.tryParse(activeStr);
        } catch (ExecutionException ex) {
            Document found = collection.find(eq("_id", playerUUID))
                    .projection(Projections.include("active"))
                    .first();
            if (found == null) return null;
            String s = found.getString("active");
            if (s == null || s.isEmpty()) return null;
            return Identifier.tryParse(s);
        }
    }

    @Override
    public List<Identifier> list(UUID playerUUID) {
        Document doc = collection.find(eq("_id", playerUUID))
                .projection(Projections.include("available"))
                .first();
        if (doc == null) return Collections.emptyList();

        List<String> raw = doc.getList("available", String.class);
        if (raw == null) return Collections.emptyList();

        List<Identifier> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            Identifier rl = Identifier.tryParse(s);
            if (rl != null) out.add(rl);
        }
        return out;
    }

    @Override
    public boolean add(UUID playerUUID, Identifier id) {
        if (id == null) return false;

        String idStr = id.toString();
        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                Updates.addToSet("available", idStr),
                new UpdateOptions().upsert(true));
        return res.wasAcknowledged() && (res.getModifiedCount() > 0 || res.getUpsertedId() != null);
    }

    @Override
    public boolean remove(UUID playerUUID, Identifier id) {
        if (id == null) return false;

        String idStr = id.toString();
        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                Updates.pull("available", idStr));
        return res.wasAcknowledged() && res.getModifiedCount() > 0;
    }
}
