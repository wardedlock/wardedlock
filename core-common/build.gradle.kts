plugins {
    id("wardedlock.core.java-conventions")
    id("wardedlock.core.i18n-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-test-fixtures`
}

description = "core-common"

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-security")

    compileOnly("jakarta.servlet:jakarta.servlet-api")

    testFixturesImplementation("org.springframework.boot:spring-boot-starter-validation")
    testFixturesImplementation("org.assertj:assertj-core")

    testFixturesApi("org.springframework.boot:spring-boot-starter-test")

    testFixturesApi("org.testcontainers:testcontainers:1.20.4")
    testFixturesApi("org.testcontainers:junit-jupiter:1.20.4")
    testFixturesCompileOnly("org.testcontainers:postgresql:1.20.4")

    testFixturesCompileOnly("org.springframework.boot:spring-boot-starter-flyway")
    testFixturesCompileOnly("org.flywaydb:flyway-database-postgresql")

}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
