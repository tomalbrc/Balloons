package de.tomalbrc.balloons.storage;

import de.tomalbrc.balloons.util.StorageUtil;
import org.jetbrains.annotations.Nullable;

public class DatabaseConfig {
    public String host;
    public int port;
    public String user;
    public String password;
    public String filepath;
    public int maxPoolSize;
    public boolean sslEnabled;
    public String databaseName;

    public int connectionTimeout = 30_000;
    public int idleTimeout = 600_000;
    public int keepaliveTime = 300_000;
    public int validationTimeout = 5_000;

    @Nullable
    String authDatabase;
    boolean useSrv;

    public DatabaseConfig(String host, int port, String databaseName, String user, String password,
                          String filepath, int maxPoolSize, boolean sslEnabled,
                          @Nullable String authDatabase, boolean useSrv) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.filepath = filepath;
        this.maxPoolSize = maxPoolSize;
        this.sslEnabled = sslEnabled;
        this.databaseName = databaseName;
        this.authDatabase = authDatabase;
        this.useSrv = useSrv;
    }

    public static class Builder {
        private String host;
        private int port;
        private String user;
        private String password;
        private String filepath;
        private int maxPoolSize = 10; // default value
        private boolean sslEnabled = false; // default
        private String database;
        private String authDatabase;
        private boolean useSrv = false; // default

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder filepath(String filepath) {
            this.filepath = filepath;
            return this;
        }

        public Builder maxPoolSize(int maxPoolSize) {
            this.maxPoolSize = maxPoolSize;
            return this;
        }

        public Builder sslEnabled(boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder authDatabase(String authDatabase) {
            this.authDatabase = authDatabase;
            return this;
        }

        public Builder useSrv(boolean useSrv) {
            this.useSrv = useSrv;
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(host, port, database, user, password,
                    filepath, maxPoolSize, sslEnabled, authDatabase, useSrv);
        }
    }

    public String getJdbcUrl(StorageUtil.Type type) {
        return switch (type) {
            case MARIADB ->
                    "jdbc:mariadb://" + host + ":" + port + "/" + databaseName + "?useSSL=" + sslEnabled;
            case POSTGRESQL -> "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
            case SQLITE -> "jdbc:sqlite:" + filepath;
            default -> throw new IllegalArgumentException("Unsupported database type: " + type);
        };
    }

    public String getDriverClassName(StorageUtil.Type type) {
        return switch (type) {
            case MARIADB -> "org.mariadb.jdbc.Driver";
            case POSTGRESQL -> "org.postgresql.Driver";
            case SQLITE -> "org.sqlite.JDBC";
            default -> throw new IllegalArgumentException("Unsupported database type: " + type);
        };
    }

    public String mongoConnectionString() {
        String protocol = useSrv ? "mongodb+srv" : "mongodb";
        StringBuilder uri = new StringBuilder(protocol + "://");

        if (user != null && password != null) {
            uri.append(user).append(":").append(password).append("@");
        }

        uri.append(host);
        if (!useSrv) {
            uri.append(":").append(port);
        }

        uri.append("/").append(databaseName);

        boolean firstParam = true;
        if (sslEnabled) {
            uri.append("?").append("ssl=true");
            firstParam = false;
        }
        if (authDatabase != null && !authDatabase.isBlank()) {
            uri.append(firstParam ? "?" : "&").append("authSource=").append(authDatabase);
        }

        return uri.toString();
    }
}
