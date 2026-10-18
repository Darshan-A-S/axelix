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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

/**
 * Tests for {@link WallboardMcpServerTools}.
 *
 * @author Nikita Kirillov
 */
class WallboardMcpServerToolsTest extends AbstractMcpServerToolsTest {

    @Autowired
    private InstanceRegistry instanceRegistry;

    @AfterEach
    void deRegisterInstances() {
        instanceRegistry.deRegister(InstanceId.of("instance-1"));
        instanceRegistry.deRegister(InstanceId.of("instance-2"));
    }

    @Nested
    class GetWallboard {

        @Test
        void shouldReturnAllInstancesWhenQueryIsNull() {
            // given.
            instanceRegistry.register(TestInstanceFactory.create("instance-1"));
            instanceRegistry.register(TestInstanceFactory.create("instance-2"));

            // when.
            String result = callTool(McpEndpoints.WALLBOARD_TOOL_NAME, Map.of());

            // then.
            assertThatJson(result).isArray().hasSize(2);
        }

        @Test
        void shouldReturnAllInstancesWhenQueryIsBlank() {
            // given.
            instanceRegistry.register(TestInstanceFactory.create("instance-1"));

            // when.
            String result = callTool(McpEndpoints.WALLBOARD_TOOL_NAME, Map.of("query", "   "));

            // then.
            assertThatJson(result).isArray().hasSize(1);
        }

        @Test
        void shouldReturnOnlyMatchingInstancesWhenQueryIsProvided() {
            // given.
            instanceRegistry.register(TestInstanceFactory.withName("instance-1", "invoice-internal-process"));
            instanceRegistry.register(TestInstanceFactory.withName("instance-2", "orders-service"));

            // when.
            String result = callTool(McpEndpoints.WALLBOARD_TOOL_NAME, Map.of("query", "invoice"));

            // then.
            assertThatJson(result).isArray().hasSize(1);
            assertThatJson(result).node("[0].name").isEqualTo("invoice-internal-process");
        }
    }
}
