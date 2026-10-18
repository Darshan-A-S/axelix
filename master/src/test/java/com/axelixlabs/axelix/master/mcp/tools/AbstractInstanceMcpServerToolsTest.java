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
 * instances. There are two managed instances registered, each emulated by its own {@link MockWebServer}, so that
 * the tools that fan the operation out to several instances can be tested as well.
 *
 * @author Mikhail Polivakha
 */
abstract class AbstractInstanceMcpServerToolsTest extends AbstractMcpServerToolsTest {

    protected static final String INSTANCE_ID = "instance-1";
    protected static final String ANOTHER_INSTANCE_ID = "instance-2";

    protected static final String ACTUATOR_PATH = "/actuator";

    @Autowired
    private InstanceRegistry instanceRegistry;

    protected final ManagedInstance instance = new ManagedInstance(INSTANCE_ID);
    protected final ManagedInstance anotherInstance = new ManagedInstance(ANOTHER_INSTANCE_ID);

    @BeforeEach
    void registerManagedInstances() throws IOException {
        instance.start(instanceRegistry);
        anotherInstance.start(instanceRegistry);
    }

    @AfterEach
    void deRegisterManagedInstances() throws IOException {
        instance.shutdown(instanceRegistry);
        anotherInstance.shutdown(instanceRegistry);
    }

    /**
     * The managed instance, registered in the {@link InstanceRegistry}, with the actuator emulated by
     * the {@link MockWebServer}.
     */
    protected static final class ManagedInstance {

        private final String id;

        private final Map<String, MockResponse> responses = new ConcurrentHashMap<>();

        private final MockWebServer server = new MockWebServer();

        private ManagedInstance(String id) {
            this.id = id;
        }

        private void start(InstanceRegistry instanceRegistry) throws IOException {
            server.setDispatcher(new Dispatcher() {
                @Override
                public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                    String path = Objects.requireNonNull(request.getPath()).substring(ACTUATOR_PATH.length());
                    return responses.getOrDefault(path, new MockResponse().setResponseCode(200));
                }
            });
            server.start();

            instanceRegistry.register(
                    TestInstanceFactory.create(id, server.url(ACTUATOR_PATH).toString()));
        }

        private void shutdown(InstanceRegistry instanceRegistry) throws IOException {
            instanceRegistry.deRegister(InstanceId.of(id));
            server.shutdown();
        }

        /**
         * Makes the instance respond with the given JSON body to requests to the given actuator path (relative
         * to the {@link #ACTUATOR_PATH}, including the query string, if any). Paths that are not stubbed respond
         * with {@code 200 OK} and an empty body.
         */
        void respondWith(String path, String body) {
            responses.put(
                    path,
                    new MockResponse()
                            .setBody(body)
                            .addHeader(HttpHeaders.CONTENT_TYPE, ACTUATOR_RESPONSE_CONTENT_TYPE));
        }

        /**
         * Makes the instance respond with {@code 400 Bad Request} to requests to the given actuator path.
         */
        void respondWithBadRequest(String path) {
            responses.put(path, new MockResponse().setResponseCode(400));
        }

        /**
         * @return the next request the instance has received
         */
        RecordedRequest takeRequest() throws InterruptedException {
            RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request)
                    .as("Managed instance '%s' did not receive the request", id)
                    .isNotNull();
            return request;
        }
    }
}
