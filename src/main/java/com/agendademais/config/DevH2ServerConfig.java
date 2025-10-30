package com.agendademais.config;

import org.h2.tools.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.sql.SQLException;

/**
 * Dev-only H2 TCP server so desktop clients (DBeaver) can connect to the same
 * file-based H2 DB.
 */
@Configuration
@Profile("dev")
public class DevH2ServerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer() throws SQLException {
        // start TCP server on default port 9092, allow other connections
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
