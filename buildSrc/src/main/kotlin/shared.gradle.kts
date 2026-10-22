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
import Dependencies.jspecifyVersion
import Dependencies.jsonwebtokenVersion

plugins {
    id("java-library")
}

dependencies {
    // Compile
    // JSpecify is compileOnly so that the starters do not bring it onto the users' runtime classpath (GH-1748).
    // This cannot break the users' code: the JVM silently ignores annotations whose classes are absent at
    // runtime, and nothing in our code references the JSpecify annotations as classes (e.g. Nullable.class).
    // Spring Framework 7 (Spring Boot 4) depends on JSpecify anyway, while on Spring Boot 2/3 nothing reads
    // these annotations at runtime: the actuator endpoints there rely on org.springframework.lang.Nullable.
    compileOnly("org.jspecify:jspecify:${jspecifyVersion}")
    testImplementation("org.jspecify:jspecify:${jspecifyVersion}")

    // Api
    api("io.jsonwebtoken:jjwt-api:${jsonwebtokenVersion}")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")

    // Test
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")
}
