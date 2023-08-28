package com.shimmermare.megaspell.serverregistry.api;

import com.shimmermare.megaspell.serverregistry.server.OnlineServer;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

record ServerInfo(
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
        boolean passwordProtected
) {
    public static ServerInfo fromServer(OnlineServer server) {
        return new ServerInfo(
                server.host(),
                server.port(),
                server.gameVersion(),
                server.name(),
                server.mode(),
                server.map(),
                server.maxPlayers(),
                server.onlinePlayers(),
                server.offlineAuth(),
                server.steamAuth(),
                server.passwordProtected()
        );
    }
}
