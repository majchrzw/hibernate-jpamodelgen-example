plugins {
    kotlin("jvm") version "2.0.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.hibernate.orm:hibernate-core:6.6.0.Final")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("org.testcontainers:testcontainers:1.20.2")
    implementation("org.testcontainers:postgresql:1.20.2")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}