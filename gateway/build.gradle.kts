plugins {
    id("wardedlock.core.java-conventions")
    id("wardedlock.core.boot")
    id("wardedlock.web.webflux")
    id("wardedlock.data.redis-reactive")
    // TODO: gateway JWT verification plugin pending decision (SRS §5.1)
}

description = "gateway"

dependencies {
    implementation(project(":core-common"))
    
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webmvc")
    
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-reactive-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
}
