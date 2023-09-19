package com.shimmermare.megaspell.serverregistry.server;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ServerRegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerRegistryService.class);

    private final OnlineServerRepository onlineServerRepository;
    private final TransactionTemplate transactionTemplate;
    private final int serverOnlineTimeoutSec;

    private final Counter serverRegisteredMetricCounter;
    private final Counter serverUnregisteredMetricCounter;

    public ServerRegistryService(
            OnlineServerRepository onlineServerRepository,
            TransactionTemplate transactionTemplate,
            @Value("${megaspell.server-online-timeout-seconds:120}")
            int serverOnlineTimeoutSec,
            MeterRegistry meterRegistry
    ) {
        this.onlineServerRepository = onlineServerRepository;
        this.transactionTemplate = transactionTemplate;
        this.serverOnlineTimeoutSec = serverOnlineTimeoutSec;

        serverRegisteredMetricCounter = meterRegistry.counter("server_registered");
        serverUnregisteredMetricCounter = meterRegistry.counter("server_unregistered");
    }

    public List<OnlineServer> getOnlineServers() {
        return onlineServerRepository.findAll();
    }

    public Optional<OnlineServer> getOnlineServer(@Nonnull String host, int port) {
        return onlineServerRepository.find(host, port);
    }

    /**
     * Register new online server.
     * If server with given host:port already online - will override existing server.
     */
    public OnlineServer registerOnlineServer(OnlineServer server) {
        return transactionTemplate.execute(tr -> {
            Optional<OnlineServer> existing = onlineServerRepository.find(server.host(), server.port());
            if (existing.isPresent()) {
                LOGGER.info("Server {}:{} already online, re-registering", server.host(), server.port());
            }
            OnlineServer toRegister = server.with()
                    .onlineSince(Instant.now())
                    .lastOnline(Instant.now())
                    .build();
            onlineServerRepository.createOrUpdate(toRegister);
            serverRegisteredMetricCounter.increment();
            LOGGER.info("Server {}:{} is registered as online: {}", server.host(), server.port(), toRegister);
            return toRegister;
        });
    }

    public void keepaliveOnlineServer(@Nonnull String host, int port, int onlinePlayers) {
        transactionTemplate.executeWithoutResult(tr -> {
            Optional<OnlineServer> existing = onlineServerRepository.find(host, port);
            if (existing.isEmpty()) {
                LOGGER.info("Can't keepalive server {}:{} because it's not online", host, port);
                throw new ServerNotOnlineException("Can't keepalive server " + host + ":" + port + " because it's not online");
            }
            OnlineServer server = existing.get().with()
                    .onlinePlayers(onlinePlayers)
                    .lastOnline(Instant.now())
                    .build();
            onlineServerRepository.createOrUpdate(server);
            LOGGER.info("Keeping alive online server {}:{}. Online players: {}", server.host(), server.port(), onlinePlayers);
        });
    }

    public void unregisterOnlineServer(@Nonnull String host, int port) {
        if (onlineServerRepository.delete(host, port)) {
            serverUnregisteredMetricCounter.increment();
            LOGGER.info("Unregistered online server {}:{}", host, port);
        } else {
            LOGGER.info("Attempted to unregister online server {}:{} but there was no such server", host, port);
        }
    }

    public void unregisterStaleOnlineServers() {
        Instant threshold = Instant.now().minusSeconds(serverOnlineTimeoutSec);
        int deleted = onlineServerRepository.deleteWhereLastOnlineIsBefore(threshold);
        if (deleted > 0) {
            serverUnregisteredMetricCounter.increment(deleted);
            LOGGER.info("Unregistered {} servers which didn't have keepalive in {}s", deleted, serverOnlineTimeoutSec);
        }
    }

    public static class ServerNotOnlineException extends RuntimeException {
        public ServerNotOnlineException(String message) {
            super(message);
        }
    }
}
