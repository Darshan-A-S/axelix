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

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.utils.TestInstanceFactory;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for the tests of the MCP tools that proxy the call to the actuator endpoints of the managed
 * instance. The managed instance is emulated by the {@link MockWebServer}.
 *
 * @author Mikhail Polivakha
 */
abstract class AbstractInstanceMcpServerToolsTest extends AbstractMcpServerToolsTest {

    protected static final String INSTANCE_ID = "instance-1";

    protected static final String ACTUATOR_PATH = "/actuator";

    @Autowired
    private InstanceRegistry instanceRegistry;

    private final Map<String, MockResponse> responses = new ConcurrentHashMap<>();

    private MockWebServer managedInstance;

    @BeforeEach
    void registerManagedInstance() throws IOException {
        managedInstance = new MockWebServer();
        managedInstance.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                String path = Objects.requireNonNull(request.getPath()).substring(ACTUATOR_PATH.length());
                return responses.getOrDefault(path, new MockResponse().setResponseCode(200));
            }
        });
        managedInstance.start();

        instanceRegistry.register(TestInstanceFactory.create(
                INSTANCE_ID, managedInstance.url(ACTUATOR_PATH).toString()));
    }

    @AfterEach
    void deRegisterManagedInstance() throws IOException {
        instanceRegistry.deRegister(InstanceId.of(INSTANCE_ID));
        managedInstance.shutdown();
    }

    /**
     * Makes the managed instance respond with the given JSON body to requests to the given actuator path (relative
     * to the {@link #ACTUATOR_PATH}, including the query string, if any). Paths that are not stubbed respond with
     * {@code 200 OK} and an empty body.
     */
    protected void respondWith(String path, String body) {
        responses.put(
                path,
                new MockResponse().setBody(body).addHeader(HttpHeaders.CONTENT_TYPE, ACTUATOR_RESPONSE_CONTENT_TYPE));
    }

    /**
     * Makes the managed instance respond with {@code 400 Bad Request} to requests to the given actuator path.
     */
    protected void respondWithBadRequest(String path) {
        responses.put(path, new MockResponse().setResponseCode(400));
    }

    /**
     * @return the next request the managed instance has received
     */
    protected RecordedRequest takeRequest() throws InterruptedException {
        RecordedRequest request = managedInstance.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).as("Managed instance did not receive the request").isNotNull();
        return request;
    }
}
