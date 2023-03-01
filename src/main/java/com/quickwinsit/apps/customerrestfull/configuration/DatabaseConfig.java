package com.quickwinsit.apps.customerrestfull.configuration;

import com.quickwinsit.apps.customerrestfull.exception.GetConfigCatKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.configcat.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    private String POSTGRES_URL;
    private String POSTGRES_USER;
    private String POSTGRES_PASSWD;


    private String getConfigCatKey() throws GetConfigCatKeyException {
        logger.info("Getting ConfigCat Key");

        String key = System.getenv().getOrDefault("KEY", "NOKEY");

        if(key.equals("NOKEY")){
            logger.error("Cannot get ConfigCat api key");
            throw new GetConfigCatKeyException();
        }

        return key;

    }

    @Autowired
    public DatabaseConfig(Environment ev) {
        logger.info("Getting database configuration");
        try
        {
            String key = getConfigCatKey();
            ConfigCatClient client = ConfigCatClient.newBuilder()
               .mode(PollingModes.manualPoll())
               .logLevel(LogLevel.ERROR) // <-- Set the log level to INFO to track how your feature flags were evaluated. When moving to production, you can remove this line to avoid too detailed logging.
               .build(key);
            client.forceRefresh();
            POSTGRES_URL = client.getValue(String.class, "postgres_url", "Default");
            POSTGRES_USER = client.getValue(String.class, "postgres_user", "Default");
            POSTGRES_PASSWD = client.getValue(String.class, "postgres_password", "Default");
        } // <-- This is the actual SDK Key for your 'Production' environment.
        catch (GetConfigCatKeyException ge) {
            throw new RuntimeException(ge);
        }
    }

    @Bean
    public DataSource getDataSource() {
        logger.info("Creating datasource for application");
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.url(this.POSTGRES_URL);
        dataSourceBuilder.username(this.POSTGRES_USER);
        dataSourceBuilder.password(this.POSTGRES_PASSWD);

        return dataSourceBuilder.build();
    }
}
