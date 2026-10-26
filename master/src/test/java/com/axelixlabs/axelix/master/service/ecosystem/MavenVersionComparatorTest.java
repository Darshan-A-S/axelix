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
package com.axelixlabs.axelix.master.service.ecosystem;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MavenVersionComparator}.
 *
 * @author Mikhail Polivakha
 */
class MavenVersionComparatorTest {

    private final MavenVersionComparator subject = MavenVersionComparator.INSTANCE;

    @ParameterizedTest(name = "{0} < {1}")
    @CsvSource({
        "1.9, 1.10",
        "1.9.9, 1.10.0",
        "2.0-alpha1, 2.0-beta1",
        "2.0-beta1, 2.0-M1",
        "2.0-M1, 2.0-RC1",
        "2.0-RC1, 2.0-SNAPSHOT",
        "2.0-SNAPSHOT, 2.0",
        "2.0, 2.0-SP1",
        "2.0, 2.0.1",
        "1.0-sp, 1.0.1",
        "33.3.0-android, 33.3.0-jre",
        "33.2.1-jre, 33.3.0-android",
        "1.2.3, 12345678901234567890.0"
    })
    void shouldOrderTheOlderVersionFirst(String older, String newer) {
        // when. then.
        assertThat(subject.compare(older, newer)).isNegative();
        assertThat(subject.compare(newer, older)).isPositive();
    }

    @ParameterizedTest(name = "{0} == {1}")
    @CsvSource({
        "1.0, 1.0.0",
        "6.5.3.Final, 6.5.3",
        "5.3.31.RELEASE, 5.3.31",
        "1.0-ga, 1",
        "2.0-RC1, 2.0-rc-1",
        "01.002, 1.2"
    })
    void shouldTreatEquivalentVersionsAsEqual(String left, String right) {
        // when. then.
        assertThat(subject.compare(left, right)).isZero();
    }

    @Test
    void shouldSortVersionsTheWayMavenDoes() {
        // given.
        List<String> versions = List.of("2.17.2", "2.9.10", "2.17.0-rc1", "2.10", "2.17.1");

        // when.
        List<String> sorted = versions.stream().sorted(subject).toList();

        // then.
        assertThat(sorted).containsExactly("2.9.10", "2.10", "2.17.0-rc1", "2.17.1", "2.17.2");
    }
}
