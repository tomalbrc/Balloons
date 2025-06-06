package de.tomalbrc.balloons.util;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import de.tomalbrc.balloons.config.MongoConfig;
import net.minecraft.resources.ResourceLocation;
import org.bson.Document;
import org.bson.UuidRepresentation;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

public class BalloonDatabaseStorage implements StorageUtil.Provider {
    private final MongoClient client;
    private final MongoCollection<Document> collection;

    public BalloonDatabaseStorage(MongoConfig mongoConfig) {
        this.client = BalloonDatabaseStorage.createClient(mongoConfig);
        MongoDatabase database = this.client.getDatabase(mongoConfig.database);
        this.collection = database.getCollection(mongoConfig.collection);

        StorageUtil.addProvider(this);
    }

    public static MongoClient createClient(MongoConfig config) {
        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(Collections.singletonList(new ServerAddress(config.host, config.port))))
                .applyToSslSettings(builder -> builder.enabled(config.useSSL))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(10, TimeUnit.SECONDS)
                                .readTimeout(15, TimeUnit.SECONDS)
                )
                .uuidRepresentation(UuidRepresentation.STANDARD);

        if (config.username != null && !config.username.isEmpty() && config.password != null && !config.password.isEmpty()) {
            MongoCredential credential = MongoCredential.createCredential(config.username, config.authSource, config.password.toCharArray());
            settingsBuilder.credential(credential);
        }

        MongoClientSettings settings = settingsBuilder.build();
        return MongoClients.create(settings);
    }

    public void setActiveBalloon(UUID playerUUID, ResourceLocation id) {
        Document playerDoc = new Document("_id", playerUUID)
                .append("active", id.toString());

        collection.replaceOne(eq("_id", playerUUID), playerDoc, new ReplaceOptions().upsert(true));
    }

    public void removeActiveBalloon(UUID playerUUID) {
        collection.deleteOne(eq("_id", playerUUID));
    }

    @Override
    public ResourceLocation getActiveBalloon(UUID playerUUID) {
        Document doc = collection.find(eq("_id", playerUUID)).first();
        if (doc == null || !doc.containsKey("active")) return null;

        String stringId = doc.getString("active");
        return ResourceLocation.parse(stringId);
    }
}