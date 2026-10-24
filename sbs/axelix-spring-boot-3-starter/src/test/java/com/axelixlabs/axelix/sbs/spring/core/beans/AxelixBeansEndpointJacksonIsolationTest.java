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
package com.axelixlabs.axelix.sbs.spring.core.beans;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.AxelixCoreNamingAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests verifying that custom Jackson settings on the application level
 * (such as property naming strategy) do not leak into Axelix endpoints wire format.
 *
 * @author Darshan A S
 */
@IgnoreTestContextArchitecture(reason = "Requires custom ObjectMapper to verify Jackson wire format isolation")
@TestPropertySource(
        properties = {"axelix.prop.test.name=axelix-beans", "spring.jackson.property-naming-strategy=SNAKE_CASE"})
@EnableAutoConfiguration(exclude = AxelixCoreNamingAutoConfiguration.class)
@Import(AxelixBeansEndpointJacksonIsolationTest.CustomJacksonConfig.class)
class AxelixBeansEndpointJacksonIsolationTest extends AbstractBeansSharedContextTest {

    @TestConfiguration
    static class CustomJacksonConfig {
        @Bean
        @Primary
        public ObjectMapper customObjectMapper() {
            return JsonMapper.builder()
                    .disable(MapperFeature.USE_ANNOTATIONS)
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .build();
        }
    }

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Test // GH-1796
    void shouldKeepCamelCaseWireFormatEvenWhenJacksonConfiguredWithSnakeCase() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-beans", String.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        String beanPath = "$.beans[?(@.beanName == '" + CommonConfiguration.CUSTOM_SUPPLIER + "')]";

        JsonAssertions.assertThatJson(response.getBody())
                .inPath(beanPath)
                .isArray()
                .first()
                .isObject()
                .containsKeys(
                        "beanName",
                        "className",
                        "scope",
                        "proxyType",
                        "aliases",
                        "dependencies",
                        "isPrimary",
                        "isLazyInit",
                        "isConfigPropsBean",
                        "qualifiers",
                        "beanSource");
    }
}
