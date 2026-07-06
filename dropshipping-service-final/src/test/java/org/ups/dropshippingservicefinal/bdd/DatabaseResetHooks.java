package org.ups.dropshippingservicefinal.bdd;

import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

public class DatabaseResetHooks {

    private final DataSource dataSource;

    @Autowired
    public DatabaseResetHooks(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Before
    public void resetDatabase() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/cleanup.sql"),
                new ClassPathResource("db/schema.sql"),
                new ClassPathResource("db/data.sql")
        );
        populator.execute(dataSource);
    }
}
