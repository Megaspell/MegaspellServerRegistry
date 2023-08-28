package com.shimmermare.megaspell.serverregistry;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class EmbeddedPostgresConfig {
    @Bean
    public Consumer<EmbeddedPostgres.Builder> embeddedPostgresCustomizer(
            @Value("${embedded-postgres.port}") int port
    ) {
        return builder -> builder.setPort(port);
    }
}
