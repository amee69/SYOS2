package com.syos.infrastructure.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final HikariDataSource dataSource;

    private DatabaseManager() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("config/database.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(prop.getProperty("db.password"));
            config.setDriverClassName("org.postgresql.Driver");
            config.setMaximumPoolSize(20);       // max 20 connections
            config.setMinimumIdle(5);            // keep 5 ready at all times
            config.setConnectionTimeout(30000);  // wait max 30 seconds
            config.setIdleTimeout(600000);       // close idle after 10 minutes
            config.setPoolName("SYOS-Pool");

            this.dataSource = new HikariDataSource(config);
            System.out.println("SYOS: HikariCP connection pool initialized.");

        } catch (IOException e) {
            throw new RuntimeException("Could not load database config", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("SYOS: Connection pool shut down.");
        }
    }
}