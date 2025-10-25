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
import com.mongodb.client.result.UpdateResult;
import de.tomalbrc.balloons.Balloons;
import de.tomalbrc.balloons.util.StorageUtil;
import net.minecraft.resources.ResourceLocation;
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

    private String loadActiveStringFromDb(String uuidStr) {
        Document doc = collection.find(eq("_id", UUID.fromString(uuidStr)))
                .projection(Projections.include("active"))
                .first();
        if (doc == null) return "";
        String a = doc.getString("active");
        return a == null ? "" : a;
    }

    @Override
    public boolean setActive(UUID playerUUID, ResourceLocation id) {
        String idStr = id == null ? null : id.toString();
        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                new Document("$set", new Document("active", idStr)),
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
                    new Document("$unset", new Document("active", "")));
            activeCache.invalidate(playerUUID.toString());
            success = updRes.wasAcknowledged() && updRes.getModifiedCount() > 0;
        }

        return success;
    }

    public ResourceLocation getActive(UUID playerUUID) {
        String uuidStr = playerUUID.toString();
        try {
            String activeStr = activeCache.get(uuidStr);
            if (activeStr == null || activeStr.isEmpty()) return null;
            return ResourceLocation.tryParse(activeStr);
        } catch (ExecutionException ex) {
            Document found = collection.find(eq("_id", playerUUID))
                    .projection(Projections.include("active"))
                    .first();
            if (found == null) return null;
            String s = found.getString("active");
            if (s == null || s.isEmpty()) return null;
            return ResourceLocation.tryParse(s);
        }
    }

    public void setAvailable(UUID playerUUID, List<ResourceLocation> ids) {
        List<String> strings = new ArrayList<>();
        if (ids != null) {
            for (ResourceLocation rl : ids) {
                if (rl != null) strings.add(rl.toString());
            }
        }
        collection.updateOne(eq("_id", playerUUID),
                new Document("$set", new Document("available", strings)),
                new UpdateOptions().upsert(true));
    }

    @Override
    public List<ResourceLocation> list(UUID playerUUID) {
        Document doc = collection.find(eq("_id", playerUUID))
                .projection(Projections.include("available"))
                .first();
        if (doc == null) return Collections.emptyList();

        List<String> raw = doc.getList("available", String.class);
        if (raw == null) return Collections.emptyList();

        List<ResourceLocation> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if (rl != null) out.add(rl);
        }
        return out;
    }

    @Override
    public boolean add(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;

        Document doc = collection.find(eq("_id", playerUUID)).first();
        List<String> available;
        if (doc != null && doc.containsKey("available")) {
            available = doc.getList("available", String.class);
        } else {
            available = new ArrayList<>();
        }

        String idStr = id.toString();
        if (available.contains(idStr)) return false;
        available.add(idStr);

        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                new Document("$set", new Document("available", available)),
                new UpdateOptions().upsert(true));
        return res.wasAcknowledged() && (res.getModifiedCount() > 0 || res.getUpsertedId() != null);
    }

    @Override
    public boolean remove(UUID playerUUID, ResourceLocation id) {
        if (id == null) return false;

        Document doc = collection.find(eq("_id", playerUUID)).first();
        if (doc == null || !doc.containsKey("available")) return false;

        List<String> available = new ArrayList<>(doc.getList("available", String.class));
        if (!available.remove(id.toString())) return false;

        UpdateResult res = collection.updateOne(eq("_id", playerUUID),
                new Document("$set", new Document("available", available)));
        return res.wasAcknowledged() && res.getModifiedCount() > 0;
    }
}
