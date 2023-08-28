package com.shimmermare.megaspell.serverregistry.server;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.time.Instant;

@RecordBuilder()
public record OnlineServer(
        @Nonnull
        String host,
        int port,

        int gameVersion,
        @Nullable
        String name,
        @Nonnull
        String mode,
        @Nullable
        String map,
        int maxPlayers,
        int onlinePlayers,
        boolean offlineAuth,
        boolean steamAuth,
        boolean passwordProtected,
        @Nonnull
        Instant onlineSince,
        @Nonnull
        Instant lastOnline
) implements OnlineServerBuilder.With {
    public OnlineServer {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("Host can't be blank or empty");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port '" + port + "' is not a valid port");
        }
        if (gameVersion <= 0) {
            throw new IllegalArgumentException("Game version can't be less than 1");
        }
        if (mode == null || mode.isEmpty()) {
            throw new IllegalArgumentException("Mode can't be blank or empty");
        }
        if (maxPlayers <= 0) {
            throw new IllegalArgumentException("Max players can't be less than 1");
        }
        if (onlinePlayers < 0) {
            throw new IllegalArgumentException("Online players can't be less than 0");
        }
        if (onlineSince == null) {
            throw new IllegalArgumentException("Online since timestamp can't be null");
        }
        if (lastOnline == null) {
            throw new IllegalArgumentException("Last online timestamp can't be null");
        }
    }
}
