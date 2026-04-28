plugins {
    id("wardedlock.java-conventions")
    id("wardedlock.spring-boot-microservice")
    id("wardedlock.web-security-conventions")
}

description = "notification-service"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-reactive-test")
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
}
