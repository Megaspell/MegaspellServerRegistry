plugins {
    java
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
}

group = "com.shimmermare.megaspell"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("io.soabase.record-builder:record-builder-processor:37")
    compileOnly("io.soabase.record-builder:record-builder-core:37")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("org.liquibase:liquibase-core")
    implementation("org.quartz-scheduler:quartz:2.3.2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.zonky.test:embedded-database-spring-test:2.3.0")
    testImplementation("io.zonky.test:embedded-postgres:2.0.4")
}

tasks.withType<Test> {
    useJUnitPlatform()
}