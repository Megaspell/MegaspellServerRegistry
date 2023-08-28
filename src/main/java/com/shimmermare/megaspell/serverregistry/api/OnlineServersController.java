package com.shimmermare.megaspell.serverregistry.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.shimmermare.megaspell.serverregistry.server.OnlineServer;
import com.shimmermare.megaspell.serverregistry.server.ServerRegistryService;
import com.shimmermare.megaspell.serverregistry.server.ServerRegistryService.ServerNotOnlineException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("online-servers")
class OnlineServersController {
    private final ServerRegistryService serverRegistryService;
    private final String realIpHeader;

    OnlineServersController(
            ServerRegistryService serverRegistryService,
            @Value("${megaspell.real-ip-header}") String realIpHeader
    ) {
        this.serverRegistryService = serverRegistryService;
        this.realIpHeader = realIpHeader;
    }

    /**
     * Get all currently online servers.
     */
    @GetMapping
    public List<ServerInfo> getOnlineServers() {
        return serverRegistryService.getOnlineServers().stream()
                .map(ServerInfo::fromServer)
                .toList();
    }

    /**
     * Register online server.
     * Method can be called only from the same host, otherwise 403 will be returned.
     * Server is identified by host and port. If one already exists - it'll be re-registered with new data.
     */
    @PostMapping
    public ServerInfo registerOnlineServer(
            @RequestBody RegisterOnlineServerRequest request,
            HttpServletRequest servletRequest
    ) {
        checkSameHost(servletRequest, request.host());

        OnlineServer server = new OnlineServer(
                request.host(),
                request.port(),
                request.gameVersion(),
                request.name(),
                request.mode(),
                request.map(),
                request.maxPlayers(),
                request.onlinePlayers(),
                request.offlineAuth(),
                request.steamAuth(),
                request.passwordProtected(),
                Instant.EPOCH,
                Instant.EPOCH
        );
        OnlineServer result = serverRegistryService.registerOnlineServer(server);
        return ServerInfo.fromServer(result);
    }

    /**
     * Server ID path param is "host:port".
     * If such server doesn't exist, 404 will be returned.
     */
    @GetMapping("/{hostAndPort}")
    public Optional<ServerInfo> getOnlineServer(@PathVariable("hostAndPort") String hostAndPortStr) {
        HostAndPort hostAndPort = parseHostAndPort(hostAndPortStr);
        return serverRegistryService.getOnlineServer(hostAndPort.host(), hostAndPort.port()).map(ServerInfo::fromServer);
    }

    /**
     * Confirm that server is still online.
     * Method can be called only from the same host, otherwise 403 will be returned.
     * If such server doesn't exist - 404 will be returned.
     * ID path param is "host:port".
     */
    @PostMapping("/{hostAndPort}/keepalive")
    public ResponseEntity<?> keepaliveOnlineServer(
            @PathVariable("hostAndPort") String hostAndPortStr,
            @RequestBody KeepaliveOnlineServerRequest request,
            HttpServletRequest servletRequest
    ) {
        HostAndPort hostAndPort = parseHostAndPort(hostAndPortStr);

        checkSameHost(servletRequest, hostAndPort.host());

        try {
            serverRegistryService.keepaliveOnlineServer(hostAndPort.host(), hostAndPort.port(), request.onlinePlayers());
        } catch (ServerNotOnlineException e) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Unregister online server.
     * Method can be called only from the same host, otherwise 403 will be returned.
     * ID path param is "host:port".
     */
    @DeleteMapping("/{hostAndPort}")
    public ResponseEntity<?> unregisterOnlineServer(
            @PathVariable("hostAndPort") String hostAndPortStr,
            HttpServletRequest servletRequest
    ) {
        HostAndPort hostAndPort = parseHostAndPort(hostAndPortStr);

        checkSameHost(servletRequest, hostAndPort.host());

        serverRegistryService.unregisterOnlineServer(hostAndPort.host(), hostAndPort.port());
        return ResponseEntity.ok().build();
    }

    private HostAndPort parseHostAndPort(String id) {
        try {
            return HostAndPort.parse(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Server ID is invalid", e);
        }
    }

    private void checkSameHost(HttpServletRequest servletRequest, String host) {
        String realIpStr = servletRequest.getHeader(realIpHeader);
        if (realIpStr == null || realIpStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request must come from the same host");
        }

        InetAddress realIp;
        try {
            realIp = InetAddress.getByName(realIpStr);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request must come from the same host");
        }

        InetAddress serverIp;
        try {
            serverIp = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request must come from the same host");
        }

        if (!realIp.equals(serverIp)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request must come from the same host");
        }
    }

    private record RegisterOnlineServerRequest(
            @JsonProperty(required = true)
            @Nonnull
            String host,
            @JsonProperty(required = true)
            int port,
            @JsonProperty(required = true)
            int gameVersion,
            @Nullable
            String name,
            @JsonProperty(required = true)
            @Nonnull
            String mode,
            @Nullable
            String map,
            @JsonProperty(required = true)
            int maxPlayers,
            @JsonProperty(defaultValue = "0")
            int onlinePlayers,
            @JsonProperty(defaultValue = "false")
            boolean offlineAuth,
            @JsonProperty(defaultValue = "false")
            boolean steamAuth,
            @JsonProperty(defaultValue = "false")
            boolean passwordProtected
    ) {
    }

    private record KeepaliveOnlineServerRequest(
            @JsonProperty(required = true)
            int onlinePlayers
    ) {
    }
}
