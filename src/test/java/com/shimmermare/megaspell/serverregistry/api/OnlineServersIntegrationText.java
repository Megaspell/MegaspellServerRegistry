package com.shimmermare.megaspell.serverregistry.api;

import com.shimmermare.megaspell.serverregistry.EmbeddedPostgresConfig;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(EmbeddedPostgresConfig.class)
@AutoConfigureEmbeddedDatabase(refresh = AutoConfigureEmbeddedDatabase.RefreshMode.BEFORE_EACH_TEST_METHOD)
@WebAppConfiguration
@ActiveProfiles("test")
public class OnlineServersIntegrationText {
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Value("${megaspell.real-ip-header}")
    private String realIpHeader;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void registerThenUpdateThenUnregisterOnlineServer() throws Exception {
        mockMvc.perform(
                        get("/online-servers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        String request = """
                {
                    "host": "localhost",
                    "port": 7777,
                    "gameVersion": 123,
                    "name": "Server name",
                    "mode": "Story",
                    "map": "Stable55Entrance",
                    "maxPlayers": 20,
                    "onlinePlayers": 0,
                    "offlineAuth": true,
                    "steamAuth": false,
                    "passwordProtected": false
                }
                """;

        mockMvc.perform(
                        post("/online-servers")
                                .header(realIpHeader, "127.0.0.1")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json(request));

        mockMvc.perform(
                        get("/online-servers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[" + request + "]"));


        String keepalive = """
                {
                    "onlinePlayers": 5
                }
                """;
        mockMvc.perform(
                        post("/online-servers/localhost:7777/keepalive")
                                .header(realIpHeader, "127.0.0.1")
                                .content(keepalive)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/online-servers/localhost:7777")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("onlinePlayers").value(5));

        mockMvc.perform(
                        delete("/online-servers/localhost:7777")
                                .header(realIpHeader, "127.0.0.1")
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/online-servers")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void registrationFailsIfWrongIp() throws Exception {
        String request = """
                {
                    "host": "localhost",
                    "port": 7777,
                    "gameVersion": 123,
                    "name": "Server name",
                    "mode": "Story",
                    "map": "Stable55Entrance",
                    "maxPlayers": 20,
                    "onlinePlayers": 0,
                    "offlineAuth": true,
                    "steamAuth": false,
                    "passwordProtected": false
                }
                """;

        mockMvc.perform(
                        post("/online-servers")
                                .header(realIpHeader, "127.0.0.2")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void cantUnregisterIfWrongIp() throws Exception {
        String request = """
                {
                    "host": "localhost",
                    "port": 7777,
                    "gameVersion": 123,
                    "name": "Server name",
                    "mode": "Story",
                    "map": "Stable55Entrance",
                    "maxPlayers": 20,
                    "onlinePlayers": 0,
                    "offlineAuth": true,
                    "steamAuth": false,
                    "passwordProtected": false
                }
                """;

        mockMvc.perform(
                        post("/online-servers")
                                .header(realIpHeader, "127.0.0.1")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        delete("/online-servers/localhost:7777")
                                .header(realIpHeader, "127.0.0.2")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    public void cantKeepaliveIfWrongIp() throws Exception {
        String request = """
                {
                    "host": "localhost",
                    "port": 7777,
                    "gameVersion": 123,
                    "name": "Server name",
                    "mode": "Story",
                    "map": "Stable55Entrance",
                    "maxPlayers": 20,
                    "onlinePlayers": 0,
                    "offlineAuth": true,
                    "steamAuth": false,
                    "passwordProtected": false
                }
                """;

        mockMvc.perform(
                        post("/online-servers")
                                .header(realIpHeader, "127.0.0.1")
                                .content(request)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        String keepalive = """
                {
                    "onlinePlayers": 5
                }
                """;
        mockMvc.perform(
                        post("/online-servers/localhost:7777/keepalive")
                                .header(realIpHeader, "127.0.0.2")
                                .content(keepalive)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }
}
