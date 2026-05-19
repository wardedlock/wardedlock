plugins {
    id("wardedlock.core.java-conventions")
    id("wardedlock.core.boot")
    id("wardedlock.web.mvc")
    id("wardedlock.data.jpa")
    id("wardedlock.web.security")
    id("wardedlock.codegen.mapstruct")
    id("wardedlock.data.redis-reactive")
    id("wardedlock.integration.mail")
}

description = "notification-service"

dependencies {
    implementation(project(":core-common"))
    
    testImplementation("org.springframework.boot:spring-boot-starter-mail-test")
}
