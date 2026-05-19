plugins {
    id("wardedlock.core.java-conventions")
    id("wardedlock.core.boot")
    id("wardedlock.web.mvc")
    id("wardedlock.data.jpa")
    id("wardedlock.web.security")
    id("wardedlock.codegen.mapstruct")
}

description = "role-service"

dependencies {
    implementation(project(":core-common"))
}
