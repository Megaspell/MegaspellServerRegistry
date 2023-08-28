package com.shimmermare.megaspell.serverregistry.api;

import jakarta.annotation.Nonnull;

record HostAndPort(@Nonnull String host, int port) {
    public static HostAndPort parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Value is null or empty");
        }

        int portSepIndex = value.lastIndexOf(':');
        if (value.length() < 3 || portSepIndex <= 0 || portSepIndex == value.length() - 1) {
            throw new IllegalArgumentException("Value is not a host:port pair");
        }

        String host = value.substring(0, portSepIndex);
        String portStr = value.substring(portSepIndex + 1);

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Port '" + portStr + "' is not a number");
        }
        if (port <= 0 || port > 65535) {
            throw new IllegalArgumentException("Port '" + portStr + "' is not a valid port");
        }

        return new HostAndPort(host, port);
    }
}
