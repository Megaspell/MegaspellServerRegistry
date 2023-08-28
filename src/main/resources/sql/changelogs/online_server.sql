--liquibase formatted sql

--changeset shimmermare:initial-commit
CREATE TABLE online_server
(
    host               text        not null,
    port               int         not null check ( port > 0 and port < 65535),
    game_version       int         not null check ( game_version > 0 )    default 1,
    name               text,
    mode               text        not null,
    map                text,
    max_players        int         not null check ( max_players > 0 )     default 1,
    online_players     int         not null check ( online_players >= 0 ) default 0,
    offline_auth       bool        not null                               default false,
    steam_auth         bool        not null                               default false,
    password_protected bool        not null                               default false,
    online_since       timestamptz not null                               default now(),
    last_online        timestamptz not null                               default now(),
    primary key (host, port)
);

create index server_last_online_idx on online_server (last_online);
