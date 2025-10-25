package de.tomalbrc.balloons.storage.hikari;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.tomalbrc.balloons.storage.DatabaseConfig;
import de.tomalbrc.balloons.util.StorageUtil;

public abstract class AbstractHikariStorage implements StorageUtil.Provider {
    final HikariDataSource dataSource;

    public AbstractHikariStorage(StorageUtil.Type type, DatabaseConfig cfg) {
        try {
            Class.forName(cfg.getDriverClassName(type));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC Driver not found: " + cfg.getDriverClassName(type), e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(cfg.getJdbcUrl(type));
        hikariConfig.setUsername(cfg.user);
        hikariConfig.setPassword(cfg.password);
        hikariConfig.setMaximumPoolSize(cfg.maxPoolSize);

        hikariConfig.setIdleTimeout(cfg.idleTimeout);
        hikariConfig.setKeepaliveTime(cfg.keepaliveTime);
        hikariConfig.setConnectionTimeout(cfg.connectionTimeout);
        hikariConfig.setValidationTimeout(cfg.validationTimeout);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void close() {
        if (dataSource != null) dataSource.close();
    }
}
