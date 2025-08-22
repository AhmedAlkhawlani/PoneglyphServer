//package com.nova.poneglyph.config.database;
//
//
//
//import org.flywaydb.core.Flyway;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import javax.sql.DataSource;
//
//@Configuration
//public class FlywayConfig {
//
//    @Value("${spring.flyway.locations}")
//    private String[] flywayLocations;
//
//    @Bean
//    public Flyway flyway(DataSource dataSource) {
//        Flyway flyway = Flyway.configure()
//                .dataSource(dataSource)
//                .locations(flywayLocations)
//                .baselineOnMigrate(true)
//                .load();
//        flyway.migrate();
//        return flyway;
//    }
//}
