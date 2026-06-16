package com.rx.admin.framework.datasource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SecondaryFlywayConfig {

    @Bean
    public Flyway secondaryFlyway(@Qualifier("secondDataSource") DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/second")
                .baselineOnMigrate(true)
                .baselineVersion("1")
                .baselineDescription("Baseline")
                .cleanDisabled(true)
                .load();
        flyway.migrate();
        return flyway;
    }
}
