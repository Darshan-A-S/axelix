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

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CacheMcpServerTools}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class CacheMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    @Test
    void shouldSendTheCacheManagerAndCacheNameAsThePathVariables() throws InterruptedException {
        // when.
        String result = callTool(
                McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME,
                Map.of("instanceId", INSTANCE_ID, "cacheManager", "cacheManager", "cacheName", "owners"));

        // then.
        assertThat(result).isEqualTo(DONE_RESPONSE);

        // and.
        RecordedRequest request = takeRequest();
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-caches/cacheManager/owners/clear");
    }
}
