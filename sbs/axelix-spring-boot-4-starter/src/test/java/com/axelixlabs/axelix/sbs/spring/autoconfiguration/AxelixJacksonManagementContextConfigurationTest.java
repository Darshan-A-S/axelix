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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import net.javacrumbs.jsonunit.assertj.JsonAssertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.Main;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixJacksonManagementContextConfiguration}.
 *
 * @author Darshan A S
 */
@IgnoreTestContextArchitecture(reason = "Requires custom ObjectMapper to verify Jackson wire format isolation")
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = "management.endpoints.web.exposure.include="
                + AxelixJacksonManagementContextConfigurationTest.ENDPOINT_ID)
@Import(AxelixJacksonManagementContextConfigurationTest.CustomJacksonConfig.class)
class AxelixJacksonManagementContextConfigurationTest {

    static final String ENDPOINT_ID = "jacksonisolation";

    @TestConfiguration
    static class CustomJacksonConfig {

        @Bean
        @Primary
        public JsonMapper customMapper() {
            return JsonMapper.builder()
                    .disable(MapperFeature.USE_ANNOTATIONS)
                    .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .build();
        }

        @Bean
        public JacksonIsolationEndpoint jacksonIsolationEndpoint() {
            return new JacksonIsolationEndpoint();
        }
    }

    @Endpoint(id = ENDPOINT_ID)
    static class JacksonIsolationEndpoint {

        @ReadOperation
        public Payload read() {
            return new Payload("value");
        }
    }

    public static class Payload {

        private final String propertyName;

        Payload(String propertyName) {
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }

    @Autowired
    private TestRestTemplateBuilder testRestTemplate;

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Test // GH-1796
    void shouldBackApplicationJacksonConverterWithCustomMapper() {
        // when.
        JacksonJsonHttpMessageConverter applicationConverter = handlerAdapter.getMessageConverters().stream()
                .filter(converter -> converter.getClass() == JacksonJsonHttpMessageConverter.class)
                .map(JacksonJsonHttpMessageConverter.class::cast)
                .findFirst()
                .orElseThrow();

        // then.
        assertThat(applicationConverter.getMapper().serializationConfig().getPropertyNamingStrategy())
                .isSameAs(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test // GH-1796
    void shouldKeepCamelCaseWireFormatEvenWhenJacksonConfiguredWithSnakeCase() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.withoutToken().getForEntity("/actuator/" + ENDPOINT_ID, String.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        JsonAssertions.assertThatJson(response.getBody())
                .isObject()
                .containsKey("propertyName")
                .doesNotContainKey("property_name");
    }
}
