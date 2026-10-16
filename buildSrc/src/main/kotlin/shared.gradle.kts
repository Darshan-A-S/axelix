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
