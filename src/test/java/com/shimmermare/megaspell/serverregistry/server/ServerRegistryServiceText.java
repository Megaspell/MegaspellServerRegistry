package com.shimmermare.megaspell.serverregistry.server;

import com.shimmermare.megaspell.serverregistry.EmbeddedPostgresConfig;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

@SpringBootTest
@Import(EmbeddedPostgresConfig.class)
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class ServerRegistryServiceText {
    @Autowired
    private OnlineServerRepository onlineServerRepository;
    @Autowired
    private ServerRegistryService serverRegistryService;

    @Value("${megaspell.server-online-timeout-seconds:120}")
    private int serverOnlineTimeoutSec;

    @Test
    public void testUnregisterStaleServers() {
        var aliveServer = new OnlineServer("localhost", 7777, 1, null, "Story", "Map", 20, 10,
                true, false, false, Instant.now(), Instant.now());
        var deadServer = new OnlineServer("localhost", 7778, 1, null, "Story", "Map", 20, 10,
                true, false, false, Instant.now(), Instant.now().minusSeconds(serverOnlineTimeoutSec + 1));
        onlineServerRepository.createOrUpdate(aliveServer);
        onlineServerRepository.createOrUpdate(deadServer);

        Assertions.assertEquals(onlineServerRepository.findAll().size(), 2);

        serverRegistryService.unregisterStaleOnlineServers();

        Assertions.assertTrue(onlineServerRepository.find("localhost", 7777).isPresent());
        Assertions.assertTrue(onlineServerRepository.find("localhost", 7778).isEmpty());
    }
}
