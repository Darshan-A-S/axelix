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

import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.master.contract.logger.LogLevelChangeRequest;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeed;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeedGroup;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeedLogger;
import com.axelixlabs.axelix.master.contract.logger.LoggersGroupProfile;
import com.axelixlabs.axelix.master.contract.logger.SingleLoggerProfile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LoggersMcpServerTools}.
 *
 * @author Nikita Kirillov
 */
class LoggersMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    private final JsonMapper objectMapper = new JsonMapper();

    @Test
    void shouldReturnAvailableLogLevels() {
        // given.
        stubAllLoggers(new LoggersFeed()
                .levels(List.of("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"))
                .loggers(List.of())
                .groups(List.of()));

        // when.
        String result = callTool("getAvailableLoggingLevels", Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThat(result).isEqualTo("[OFF, ERROR, WARN, INFO, DEBUG, TRACE]");
    }

    @Test
    void shouldReturnAllGroupsAsJson() {
        // given.
        LoggersFeedGroup web = feedGroup("web", "INFO", List.of("com.example.app.web"));
        LoggersFeedGroup sql = feedGroup("sql", null, List.of("com.example.app.sql"));
        stubAllLoggers(new LoggersFeed().levels(List.of()).loggers(List.of()).groups(List.of(web, sql)));

        // when.
        String result = callTool("getLoggerGroupsFeed", Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThatJson(result).isArray().hasSize(2);
        assertThatJson(result).node("[0].name").isEqualTo("web");
        assertThatJson(result).node("[0].configuredLevel").isEqualTo("INFO");
        assertThatJson(result).node("[0].members").isArray().containsExactly("com.example.app.web");
        assertThatJson(result).node("[1].name").isEqualTo("sql");
        assertThatJson(result).node("[1].configuredLevel").isEqualTo(null);
    }

    @Test
    void shouldReturnAllLoggersAsJson() {
        // given.
        LoggersFeedLogger root = feedLogger("ROOT", "INFO", "INFO");
        LoggersFeedLogger appLogger = feedLogger("com.example.app", null, "INFO");
        stubAllLoggers(new LoggersFeed()
                .levels(List.of())
                .loggers(List.of(root, appLogger))
                .groups(List.of()));

        // when.
        String result = callTool("getLoggersFeed", Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThatJson(result).isArray().hasSize(2);
        assertThatJson(result).node("[0].name").isEqualTo("ROOT");
        assertThatJson(result).node("[0].configuredLevel").isEqualTo("INFO");
        assertThatJson(result).node("[0].effectiveLevel").isEqualTo("INFO");
        assertThatJson(result).node("[1].name").isEqualTo("com.example.app");
        assertThatJson(result).node("[1].configuredLevel").isEqualTo(null);
    }

    @Nested
    class FindLoggersByName {

        @Test
        void shouldReturnLoggerFoundViaTheDirectEndpoint() throws InterruptedException {
            // given.
            SingleLoggerProfile appLogger = logger("com.example.app", "DEBUG", "DEBUG");
            respondWith("/axelix-loggers/logger/com.example.app", objectMapper.writeValueAsString(appLogger));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "com.example.app"));

            // then.
            assertThatJson(result).isObject().containsOnlyKeys("com.example.app");
            assertThatJson(result).node("com\\.example\\.app.name").isEqualTo("com.example.app");
            assertThatJson(result).node("com\\.example\\.app.configuredLevel").isEqualTo("DEBUG");
            assertThatJson(result).node("com\\.example\\.app.effectiveLevel").isEqualTo("DEBUG");

            assertThat(takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/com.example.app");
        }

        @Test
        void shouldFallBackToFilteringAllLoggersWhenDirectEndpointRespondsWithBadRequest() {
            // given. the exact-name lookup fails - the provided name is only a partial match.
            respondWithBadRequest("/axelix-loggers/logger/com.example.app");

            // and.
            LoggersFeedLogger appService = feedLogger("com.example.app.service", null, "INFO");
            LoggersFeedLogger appRepository = feedLogger("com.example.app.repository", null, "INFO");
            LoggersFeedLogger root = feedLogger("ROOT", "INFO", "INFO");
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(appService, appRepository, root))
                    .groups(List.of()));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "com.example.app"));

            // then.
            assertThatJson(result).isObject().containsOnlyKeys("com.example.app.service", "com.example.app.repository");
            assertThatJson(result)
                    .node("com\\.example\\.app\\.service.effectiveLevel")
                    .isEqualTo("INFO");
        }

        @Test
        void shouldReturnEmptyMapWhenNoLoggerMatchesInTheFallback() {
            // given.
            respondWithBadRequest("/axelix-loggers/logger/does-not-exist");

            // and.
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(feedLogger("ROOT", "INFO", "INFO")))
                    .groups(List.of()));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "does-not-exist"));

            // then.
            assertThatJson(result).isObject().isEmpty();
        }
    }

    @Nested
    class FindGroupsByName {

        @Test
        void shouldReturnGroupFoundViaTheDirectEndpoint() throws InterruptedException {
            // given.
            LoggersGroupProfile web = group("web", "WARN", List.of("com.example.app.web"));
            respondWith("/axelix-loggers/group/web", objectMapper.writeValueAsString(web));

            // when.
            String result = callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "web"));

            // then.
            assertThatJson(result).isObject().containsOnlyKeys("web");
            assertThatJson(result).node("web.name").isEqualTo("web");
            assertThatJson(result).node("web.configuredLevel").isEqualTo("WARN");

            assertThat(takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/group/web");
        }

        @Test
        void shouldFallBackToFilteringAllGroupsWhenDirectEndpointRespondsWithBadRequest() {
            // given.
            respondWithBadRequest("/axelix-loggers/group/w");

            // and.
            LoggersFeedGroup web = feedGroup("web", "INFO", List.of("com.example.app.web"));
            LoggersFeedGroup sql = feedGroup("sql", null, List.of("com.example.app.sql"));
            stubAllLoggers(
                    new LoggersFeed().levels(List.of()).loggers(List.of()).groups(List.of(web, sql)));

            // when.
            String result = callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "w"));

            // then.
            assertThatJson(result).isObject().containsOnlyKeys("web");
            assertThatJson(result).node("web.configuredLevel").isEqualTo("INFO");
        }

        @Test
        void shouldReturnEmptyMapWhenNoGroupMatchesInTheFallback() {
            // given.
            respondWithBadRequest("/axelix-loggers/group/does-not-exist");

            // and.
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of())
                    .groups(List.of(feedGroup("web", "INFO", List.of()))));

            // when.
            String result =
                    callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "does-not-exist"));

            // then.
            assertThatJson(result).isObject().isEmpty();
        }
    }

    @Nested
    class ChangeLoggingLevelByLoggerName {

        @Test
        void shouldSendTheLoggerNameAsThePathVariableAndTheLevelWithTtlAsTheBody() throws InterruptedException {
            // when.
            callTool(
                    "changeLoggingLevelByLoggerName",
                    Map.of(
                            "instanceId",
                            INSTANCE_ID,
                            "loggerName",
                            "com.example.app",
                            "loggerLevel",
                            "DEBUG",
                            "ttlSeconds",
                            120));

            // then.
            RecordedRequest request = takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath())
                    .isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/com.example.app/change-level");

            LogLevelChangeRequest body =
                    objectMapper.readValue(request.getBody().readUtf8(), LogLevelChangeRequest.class);
            assertThat(body.getConfiguredLevel()).isEqualTo("DEBUG");
            assertThat(body.getTtlSeconds()).isEqualTo(120L);
        }

        @Test
        void shouldSendAPermanentChangeWhenTtlIsOmitted() throws InterruptedException {
            // when.
            callTool(
                    "changeLoggingLevelByLoggerName",
                    Map.of("instanceId", INSTANCE_ID, "loggerName", "ROOT", "loggerLevel", "WARN"));

            // then.
            RecordedRequest request = takeRequest();
            assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/ROOT/change-level");

            LogLevelChangeRequest body =
                    objectMapper.readValue(request.getBody().readUtf8(), LogLevelChangeRequest.class);
            assertThat(body.getConfiguredLevel()).isEqualTo("WARN");
            assertThat(body.getTtlSeconds()).isNull();
        }
    }

    @Nested
    class ChangeLoggingLevelByGroupName {

        @Test
        void shouldSendTheGroupNameAsThePathVariableAndAlwaysAPermanentChange() throws InterruptedException {
            // when.
            callTool(
                    "changeLoggingLevelByGroupName",
                    Map.of("instanceId", INSTANCE_ID, "groupName", "web", "loggerLevel", "WARN"));

            // then.
            RecordedRequest request = takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/group/web/change-level");

            LogLevelChangeRequest body =
                    objectMapper.readValue(request.getBody().readUtf8(), LogLevelChangeRequest.class);
            assertThat(body.getConfiguredLevel()).isEqualTo("WARN");
            assertThat(body.getTtlSeconds()).isNull();
        }
    }

    @Nested
    class ResetLoggingLevelByLoggerName {

        @Test
        void shouldSendTheLoggerNameAsThePathVariableAndNoBody() throws InterruptedException {
            // when.
            callTool(
                    "resetLoggingLevelByLoggerName",
                    Map.of("instanceId", INSTANCE_ID, "loggerName", "com.example.app"));

            // then.
            RecordedRequest request = takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/com.example.app/reset");
            assertThat(request.getBodySize()).isZero();
        }
    }

    private void stubAllLoggers(LoggersFeed feed) {
        respondWith("/axelix-loggers", objectMapper.writeValueAsString(feed));
    }

    private static SingleLoggerProfile logger(String name, String configuredLevel, String effectiveLevel) {
        return new SingleLoggerProfile()
                .name(name)
                .configuredLevel(configuredLevel)
                .effectiveLevel(effectiveLevel);
    }

    private static LoggersGroupProfile group(String name, String configuredLevel, List<String> members) {
        return new LoggersGroupProfile()
                .name(name)
                .configuredLevel(configuredLevel)
                .members(members);
    }

    private static LoggersFeedLogger feedLogger(String name, String configuredLevel, String effectiveLevel) {
        return new LoggersFeedLogger()
                .name(name)
                .configuredLevel(configuredLevel)
                .effectiveLevel(effectiveLevel);
    }

    private static LoggersFeedGroup feedGroup(String name, String configuredLevel, List<String> members) {
        return new LoggersFeedGroup()
                .name(name)
                .configuredLevel(configuredLevel)
                .members(members);
    }
}
