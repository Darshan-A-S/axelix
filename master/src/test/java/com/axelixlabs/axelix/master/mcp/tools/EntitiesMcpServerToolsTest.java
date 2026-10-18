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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import com.axelixlabs.axelix.common.domain.insights.GarbageCollector;
import com.axelixlabs.axelix.master.contract.metadata.Association;
import com.axelixlabs.axelix.master.contract.metadata.AssociationProblem;
import com.axelixlabs.axelix.master.contract.metadata.FlaggedAssociation;
import com.axelixlabs.axelix.master.contract.metadata.JpaEntities;
import com.axelixlabs.axelix.master.contract.metadata.MappedEntity;
import com.axelixlabs.axelix.master.contract.metadata.PersistenceInsights;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot;
import com.axelixlabs.axelix.master.domain.HistoricalApplicationSnapshot.SnapshotId;
import com.axelixlabs.axelix.master.domain.Insights;
import com.axelixlabs.axelix.master.domain.JavaVersion;
import com.axelixlabs.axelix.master.domain.JdkVendor;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link EntitiesMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class EntitiesMcpServerToolsTest extends AbstractMcpServerToolsTest {

    private static final String GROUP_ID = "com.example";
    private static final String ARTIFACT_ID = "orders-service";

    @Autowired
    private JdbcAggregateTemplate jdbcAggregateTemplate;

    @AfterEach
    void deleteSnapshots() {
        jdbcAggregateTemplate.deleteAll(HistoricalApplicationSnapshot.class);
    }

    @Nested
    class WhenEntityNameNotSpecified {

        @Test
        void shouldReturnProfileOfEveryMappedEntity() {
            // given.
            MappedEntity order =
                    entity("Order", "orders", 2, List.of(flagged("Order", "items", AssociationProblem.EAGER_FETCHING)));
            MappedEntity customer = entity("Customer", "customers", 0, List.of());
            stubApplication(new JpaEntities().entities(List.of(order, customer)));

            // when.
            String result = getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThatJson(result).isArray().hasSize(2);
            assertThat(result).contains("Order").contains("Customer");
        }
    }

    @Nested
    class WhenEntityNameSpecified {

        @Test
        void shouldReturnOnlyProfileOfMatchingEntity() {
            // given.
            MappedEntity order =
                    entity("Order", "orders", 3, List.of(flagged("Order", "items", AssociationProblem.EAGER_FETCHING)));
            MappedEntity customer = entity("Customer", "customers", 0, List.of());
            stubApplication(new JpaEntities().entities(List.of(order, customer)));

            // when.
            String result = getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, "Order");

            // then.
            assertThatJson(result).node("name").isEqualTo("Order");
            assertThatJson(result).node("table").isEqualTo("orders");
            assertThatJson(result).node("associationsCount").isEqualTo(3);
            assertThatJson(result).node("flaggedAssociations").isArray().hasSize(1);
            assertThatJson(result)
                    .node("flaggedAssociations[0].association.field")
                    .isEqualTo("items");
            assertThatJson(result)
                    .node("flaggedAssociations[0].problems")
                    .isArray()
                    .containsExactly("EAGER_FETCHING");
            assertThat(result).doesNotContain("Customer");
        }

        @Test
        void shouldReturnMessageWhenNoMatchingEntity() {
            // given.
            stubApplication(new JpaEntities().entities(List.of(entity("Customer", "customers", 0, List.of()))));

            // when.
            String result = getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, "Order");

            // then.
            assertThat(result).contains("No entity named 'Order'");
        }
    }

    @Nested
    class ApplicationLookup {

        @Test
        void shouldLookUpApplicationByEmptyGroupId() {
            // given. an application registered with an empty group id (e.g. a Gradle service without 'group').
            jdbcAggregateTemplate.insert(snapshot(
                    "", new JpaEntities().entities(List.of(entity("Customer", "customers", 0, List.of())))));

            // when. the groupId is sent as an empty string, as the tool instructs.
            String result = getApplicationEntitiesProfile("", ARTIFACT_ID, null);

            // then.
            assertThat(result).contains("Customer");
        }

        @Test
        void shouldReturnMessageWhenApplicationNotFound() {
            // when. no snapshot of the application is recorded.
            String result = getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThat(result).contains("No application found");
        }

        @Test
        void shouldReturnMessageWhenNoEntitiesMapRecorded() {
            // given.
            stubApplication(null);

            // when.
            String result = getApplicationEntitiesProfile(GROUP_ID, ARTIFACT_ID, null);

            // then.
            assertThat(result).contains("No entities map");
        }
    }

    private void stubApplication(@Nullable JpaEntities jpaEntities) {
        jdbcAggregateTemplate.insert(snapshot(GROUP_ID, jpaEntities));
    }

    private String getApplicationEntitiesProfile(String groupId, String artifactId, @Nullable String entityName) {
        Map<String, Object> arguments = new HashMap<>(Map.of("groupId", groupId, "artifactId", artifactId));
        if (entityName != null) {
            arguments.put("entityName", entityName);
        }
        return callTool(McpEndpoints.ENTITIES_PROFILE_TOOL_NAME, arguments);
    }

    private static MappedEntity entity(
            String name, String table, int associationsCount, List<FlaggedAssociation> flaggedAssociations) {
        return new MappedEntity()
                .name(name)
                .table(table)
                .associationsCount(associationsCount)
                .flaggedAssociations(flaggedAssociations);
    }

    private static FlaggedAssociation flagged(String entity, String field, AssociationProblem problem) {
        return new FlaggedAssociation()
                .association(new Association().entity(entity).field(field))
                .mapping("@ManyToOne(fetch = FetchType.EAGER)")
                .problems(List.of(problem));
    }

    private static HistoricalApplicationSnapshot snapshot(String groupId, @Nullable JpaEntities jpaEntities) {
        return new HistoricalApplicationSnapshot(
                new SnapshotId(groupId, ARTIFACT_ID, LocalDate.now(ZoneOffset.UTC)),
                new Insights(
                        new Insights.HotSpot(
                                new Insights.HotSpot.ProjectLeyden(false, false),
                                new Insights.HotSpot.GarbageCollector(false, GarbageCollector.G1),
                                new Insights.HotSpot.ProjectLilliput(false)),
                        new Insights.SpringFramework(false),
                        new PersistenceInsights().transactions(List.of()).entitiesMap(jpaEntities)),
                new HistoricalApplicationSnapshot.Versions(
                        "1.0.0", "3.5.0", "6.2.0", new JavaVersion(21, 0, 0, 0), JdkVendor.ADOPTIUM, null));
    }
}
