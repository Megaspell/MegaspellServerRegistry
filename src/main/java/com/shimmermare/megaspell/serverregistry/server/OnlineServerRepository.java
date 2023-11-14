package com.shimmermare.megaspell.serverregistry.server;

import jakarta.annotation.Nonnull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
public class OnlineServerRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public OnlineServerRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int count() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from online_server",
                EmptySqlParameterSource.INSTANCE, Integer.class);
        return Objects.requireNonNullElse(count, 0);
    }

    public int countOnlinePlayers() {
        Integer count = jdbcTemplate.queryForObject("select sum(online_players) from online_server",
                EmptySqlParameterSource.INSTANCE, Integer.class);
        return Objects.requireNonNullElse(count, 0);
    }

    public List<OnlineServer> findAll() {
        return jdbcTemplate.query("select * from online_server", this::mapServerRow);
    }

    public Optional<OnlineServer> find(@Nonnull String host, int port) {
        try {
            String query = "select * from online_server where host = :host and port = :port";
            var props = Map.of("host", host, "port", port);
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, props, this::mapServerRow));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void createOrUpdate(@Nonnull OnlineServer server) {
        String query = """
                insert into online_server(
                    host,
                    port,
                    game_version,
                    name,
                    mode,
                    map,
                    max_players,
                    online_players,
                    offline_auth,
                    steam_auth,
                    password_protected,
                    online_since,
                    last_online
                )
                values (
                    :host,
                    :port,
                    :game_version,
                    :name,
                    :mode,
                    :map,
                    :max_players,
                    :online_players,
                    :offline_auth,
                    :steam_auth,
                    :password_protected,
                    :online_since,
                    :last_online
                )
                on conflict (host, port) do update set
                    host = :host,
                    port = :port,
                    game_version = :game_version,
                    name = :name,
                    mode = :mode,
                    map = :map,
                    max_players = :max_players,
                    online_players = :online_players,
                    offline_auth = :offline_auth,
                    steam_auth = :steam_auth,
                    password_protected = :password_protected,
                    online_since = :online_since,
                    last_online = :last_online
                """;
        var params = new HashMap<String, Object>();
        params.put("host", server.host());
        params.put("port", server.port());
        params.put("game_version", server.gameVersion());
        params.put("name", server.name());
        params.put("mode", server.mode());
        params.put("map", server.map());
        params.put("max_players", server.maxPlayers());
        params.put("online_players", server.onlinePlayers());
        params.put("offline_auth", server.offlineAuth());
        params.put("steam_auth", server.steamAuth());
        params.put("password_protected", server.passwordProtected());
        params.put("online_since", Timestamp.from(server.onlineSince()));
        params.put("last_online", Timestamp.from(server.lastOnline()));

        jdbcTemplate.update(query, params);
    }

    public boolean delete(@Nonnull String host, int port) {
        String query = "delete from online_server where host = :host and port = :port";
        return jdbcTemplate.update(query, Map.of("host", host, "port", port)) > 0;
    }

    public int deleteWhereLastOnlineIsBefore(Instant threshold) {
        String query = "delete from online_server where last_online < :threshold";
        var props = Map.of("threshold", Timestamp.from(threshold));
        return jdbcTemplate.update(query, props);
    }

    private OnlineServer mapServerRow(ResultSet rs, int rowNum) throws SQLException {
        return new OnlineServer(
                rs.getString("host"),
                rs.getInt("port"),
                rs.getInt("game_version"),
                rs.getString("name"),
                rs.getString("mode"),
                rs.getString("map"),
                rs.getInt("max_players"),
                rs.getInt("online_players"),
                rs.getBoolean("offline_auth"),
                rs.getBoolean("steam_auth"),
                rs.getBoolean("password_protected"),
                rs.getTimestamp("online_since").toInstant(),
                rs.getTimestamp("last_online").toInstant()
        );
    }
}
