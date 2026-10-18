/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.mcp.tools;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for the MCP tools tests. The tools are invoked the same way an AI agent invokes them - through the
 * MCP SDK client talking to the running Axelix Master over the streamable HTTP transport.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.master.mcp-server.enabled=true",
            "axelix.master.auth.options.local.enabled=true",
            "axelix.master.auth.options.super-admin.credentials.username=admin",
            "axelix.master.auth.options.super-admin.credentials.password=admin",
        })
abstract class AbstractMcpServerToolsTest {

    @LocalServerPort
    private int port;

    @Autowired
    private McpServerStreamableHttpProperties mcpProperties;

    /**
     * The text the AI agent receives from the tools that do not return anything ({@code void} methods), i.e. the
     * {@code "Done"} JSON string.
     * <p>
     * Neither Axelix nor the MCP specification produces this response. It is the Spring AI MCP server toolkit
     * that, upon detecting that the {@code @McpTool} method is {@code void}, responds with the {@code "Done"}
     * string serialized to JSON (hence the quotes), so that the AI agent receives at least something back.
     */
    protected static final String DONE_RESPONSE = "\"Done\"";

    private McpSyncClient mcpClient;

    @BeforeEach
    void connectMcpClient() {
        String credentials = Base64.getEncoder().encodeToString("admin:admin".getBytes(StandardCharsets.UTF_8));

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(
                        "http://localhost:" + port)
                .endpoint(mcpProperties.getMcpEndpoint())
                .httpRequestCustomizer(
                        (request, _, _, _, _) -> request.header(HttpHeaders.AUTHORIZATION, "Basic " + credentials))
                .build();

        mcpClient = McpClient.sync(transport).build();
        mcpClient.initialize();
    }

    @AfterEach
    void closeMcpClient() {
        mcpClient.closeGracefully();
    }

    /**
     * Calls the given MCP tool, expecting it to succeed.
     *
     * @return the text the tool responded with
     */
    protected String callTool(String toolName, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = mcpClient.callTool(
                McpSchema.CallToolRequest.builder(toolName).arguments(arguments).build());

        assertThat(result.isError()).isFalse();
        assertThat(result.content()).singleElement().isInstanceOf(McpSchema.TextContent.class);

        return ((McpSchema.TextContent) result.content().get(0)).text();
    }
}
