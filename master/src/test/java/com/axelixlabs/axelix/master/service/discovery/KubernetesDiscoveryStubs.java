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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.instancio.Instancio;
import org.instancio.Select;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.axelixlabs.axelix.master.service.discovery.k8s.KubernetesServiceInstance;

import static com.axelixlabs.axelix.master.utils.ContentType.ACTUATOR_RESPONSE_CONTENT_TYPE;

/**
 * Stubs the Kubernetes side of the instance discovery for tests: the pods that the mocked {@link DiscoveryClient}
 * returns, and the actuator responses that these pods serve via the {@link MockWebServer}.
 *
 * @author Mikhail Polivakha
 */
class KubernetesDiscoveryStubs {

    private final DiscoveryClient discoveryClient;
    private final MockWebServer mockWebServer;

    KubernetesDiscoveryStubs(DiscoveryClient discoveryClient, MockWebServer mockWebServer) {
        this.discoveryClient = discoveryClient;
        this.mockWebServer = mockWebServer;
    }

    /**
     * @return the pod that serves its actuator endpoints via the {@link MockWebServer}.
     */
    ServiceInstance k8sInstance(String serviceId, String instanceId) {
        return Instancio.of(KubernetesServiceInstance.class)
                .set(Select.field("instanceId"), instanceId)
                .set(Select.field("serviceId"), serviceId)
                .set(Select.field("secure"), false)
                .set(Select.field("host"), mockWebServer.getHostName())
                .set(Select.field("port"), mockWebServer.getPort())
                .create();
    }

    /**
     * Makes the {@link DiscoveryClient} discover the given pods, grouped into services by their service id.
     */
    void stubDiscovery(ServiceInstance... instances) {
        Map<String, List<ServiceInstance>> instancesByService = Arrays.stream(instances)
                .collect(Collectors.groupingBy(ServiceInstance::getServiceId, LinkedHashMap::new, Collectors.toList()));

        Mockito.when(discoveryClient.getServices()).thenReturn(List.copyOf(instancesByService.keySet()));
        instancesByService.forEach((serviceId, serviceInstances) ->
                Mockito.when(discoveryClient.getInstances(serviceId)).thenReturn(serviceInstances));
    }

    /**
     * Enqueues the given actuator responses, which are served in order, regardless of the requested path.
     */
    void enqueueResponses(String... responses) {
        for (String response : responses) {
            mockWebServer.enqueue(
                    new MockResponse().setBody(response).addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE));
        }
    }

    /**
     * Serves the given actuator response on the given path, any number of times, and responds with 404 on others.
     */
    void respondOn(String path, String response) {
        mockWebServer.setDispatcher(new Dispatcher() {
            @Override
            public @NotNull MockResponse dispatch(@NotNull RecordedRequest request) {
                if (path.equals(request.getPath())) {
                    return new MockResponse()
                            .setBody(response)
                            .addHeader("Content-Type", ACTUATOR_RESPONSE_CONTENT_TYPE);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
    }
}
