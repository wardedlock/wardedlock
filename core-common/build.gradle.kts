plugins {
    id("wardedlock.java-conventions")
    id("wardedlock.i18n-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

description = "core-common"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}
