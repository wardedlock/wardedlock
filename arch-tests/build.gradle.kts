
plugins {
    java
    id("io.spring.dependency-management")
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.6")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2025.1.1")
    }
}

dependencies {
    testImplementation(project(":gateway"))
    testImplementation(project(":auth-service"))
    testImplementation(project(":account-service"))
    testImplementation(project(":role-service"))
    testImplementation(project(":app-management-service"))
    testImplementation(project(":notification-service"))
    testImplementation(testFixtures(project(":core-common")))
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework:spring-web:6.1.4")
    testImplementation("org.springframework:spring-context:6.1.4")
    testImplementation("org.springframework.data:spring-data-commons:3.2.3")
    testImplementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    testImplementation("jakarta.inject:jakarta.inject-api:2.0.1")
}

tasks.test {
    useJUnitPlatform()
}
