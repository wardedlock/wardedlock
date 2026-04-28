plugins {
    id("wardedlock.java-conventions")
    id("wardedlock.spring-boot-microservice")
    id("wardedlock.jpa-postgres-conventions")
    id("wardedlock.web-security-conventions")
}

description = "auth-service"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
}
