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

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LogsMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class LogsMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    @Test
    void shouldSendTheLimitAsTheQueryParameterAndReturnTheLogsAsThePlainText() throws InterruptedException {
        // given.
        respondWith("/axelix-logs?limit=25", """
                {"entries":[{"timestamp":1760264130123,"level":"INFO","logger":"com.example.OrderService",\
                "thread":"main","message":"Order created"}]}
                """);

        // when.
        String result =
                callTool(McpEndpoints.LOG_ENTRIES_TAIL_TOOL_NAME, Map.of("instanceId", INSTANCE_ID, "limit", 25));

        // then.
        assertThat(result).isEqualTo("2025-10-12T10:15:30.123Z INFO [main] com.example.OrderService - Order created\n");
        assertThat(takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-logs?limit=25");
    }

    @Test
    void shouldNotSendTheLimit_WhenItIsAbsent() throws InterruptedException {
        // given.
        respondWith("/axelix-logs", "{\"entries\":[]}");

        // when.
        String result = callTool(McpEndpoints.LOG_ENTRIES_TAIL_TOOL_NAME, Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThat(result).isEmpty();
        assertThat(takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-logs");
    }
}
