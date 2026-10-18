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
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

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
            // language=json
            assertThatJson(result).when(IGNORING_ARRAY_ORDER).isEqualTo("""
                    [
                      {
                        "id": { "instanceId": "instance-1" },
                        "applicationId": { "groupId": "com.axelixlabs", "artifactId": "test-object-factory-app" },
                        "name": "test-object-factory-instance",
                        "serviceVersion": "1.2.3-classifer-test",
                        "starterVersion": "1.0.0-SNAPSHOT",
                        "javaVersion": { "feature": 25, "interim": 0, "update": 0, "patch": 0 },
                        "springBootVersion": "3.5.2",
                        "springFrameworkVersion": "6.0.2",
                        "kotlinVersion": null,
                        "jdkVendor": "BellSoft",
                        "commitShaShort": "df027cf",
                        "deployedAt": "${json-unit.any-string}",
                        "latestHeartBeat": "${json-unit.any-string}",
                        "status": "UP",
                        "memoryUsage": { "heap": 1000.0 },
                        "actuatorUrl": "http://example.com",
                        "detectedFeatures": null
                      },
                      {
                        "id": { "instanceId": "instance-2" },
                        "applicationId": { "groupId": "com.axelixlabs", "artifactId": "test-object-factory-app" },
                        "name": "test-object-factory-instance",
                        "serviceVersion": "1.2.3-classifer-test",
                        "starterVersion": "1.0.0-SNAPSHOT",
                        "javaVersion": { "feature": 25, "interim": 0, "update": 0, "patch": 0 },
                        "springBootVersion": "3.5.2",
                        "springFrameworkVersion": "6.0.2",
                        "kotlinVersion": null,
                        "jdkVendor": "BellSoft",
                        "commitShaShort": "df027cf",
                        "deployedAt": "${json-unit.any-string}",
                        "latestHeartBeat": "${json-unit.any-string}",
                        "status": "UP",
                        "memoryUsage": { "heap": 1000.0 },
                        "actuatorUrl": "http://example.com",
                        "detectedFeatures": null
                      }
                    ]
                    """);
        }

        @Test
        void shouldReturnAllInstancesWhenQueryIsBlank() {
            // given.
            instanceRegistry.register(TestInstanceFactory.create("instance-1"));

            // when.
            String result = callTool(McpEndpoints.WALLBOARD_TOOL_NAME, Map.of("query", "   "));

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "id": { "instanceId": "instance-1" },
                        "applicationId": { "groupId": "com.axelixlabs", "artifactId": "test-object-factory-app" },
                        "name": "test-object-factory-instance",
                        "serviceVersion": "1.2.3-classifer-test",
                        "starterVersion": "1.0.0-SNAPSHOT",
                        "javaVersion": { "feature": 25, "interim": 0, "update": 0, "patch": 0 },
                        "springBootVersion": "3.5.2",
                        "springFrameworkVersion": "6.0.2",
                        "kotlinVersion": null,
                        "jdkVendor": "BellSoft",
                        "commitShaShort": "df027cf",
                        "deployedAt": "${json-unit.any-string}",
                        "latestHeartBeat": "${json-unit.any-string}",
                        "status": "UP",
                        "memoryUsage": { "heap": 1000.0 },
                        "actuatorUrl": "http://example.com",
                        "detectedFeatures": null
                      }
                    ]
                    """);
        }

        @Test
        void shouldReturnOnlyMatchingInstancesWhenQueryIsProvided() {
            // given.
            instanceRegistry.register(TestInstanceFactory.withName("instance-1", "invoice-internal-process"));
            instanceRegistry.register(TestInstanceFactory.withName("instance-2", "orders-service"));

            // when.
            String result = callTool(McpEndpoints.WALLBOARD_TOOL_NAME, Map.of("query", "invoice"));

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    [
                      {
                        "id": { "instanceId": "instance-1" },
                        "applicationId": { "groupId": "com.axelixlabs", "artifactId": "test-object-factory-app" },
                        "name": "invoice-internal-process",
                        "serviceVersion": "1.2.3-classifer-test",
                        "starterVersion": "1.0.0-SNAPSHOT",
                        "javaVersion": { "feature": 25, "interim": 0, "update": 0, "patch": 0 },
                        "springBootVersion": "3.5.2",
                        "springFrameworkVersion": "6.0.2",
                        "kotlinVersion": null,
                        "jdkVendor": "BellSoft",
                        "commitShaShort": "df027cf",
                        "deployedAt": "${json-unit.any-string}",
                        "latestHeartBeat": "${json-unit.any-string}",
                        "status": "UP",
                        "memoryUsage": { "heap": 1000.0 },
                        "actuatorUrl": "http://example.com",
                        "detectedFeatures": null
                      }
                    ]
                    """);
        }
    }
}
