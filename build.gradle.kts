/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion = "1.6.20"

plugins {
  kotlin("jvm") version "1.6.20"
  kotlin("plugin.serialization") version "1.6.20"
  kotlin("plugin.spring") version "1.6.20"
  kotlin("plugin.jpa") version "1.6.20"
  id("org.springframework.boot") version "2.6.6"
  id("com.github.ben-manes.versions") version "0.42.0"
}

apply(plugin = "io.spring.dependency-management")

val ktorVersion = "2.0.0"
val kotlinxSerializationVersion = "1.3.2"
val openCsvVersion = "5.6"
val hibernateValidatorVersion = "7.0.4.Final"
val kotestVersion = "5.2.3"
val mockkVersion = "1.12.3"
val hsqldbVersion = "2.6.1"
val flywayVersion = "8.5.8"

group = "de.dkjs.survey"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

tasks.withType<Test> {
  useJUnitPlatform()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("javax.inject:javax.inject:1")
  implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-rest") {
    exclude("org.springframework.boot", "spring-boot-starter-tomcat")
  }
  implementation("org.springframework.boot:spring-boot-starter-undertow")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("org.springframework.data:spring-data-rest-hal-explorer")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-auth:$ktorVersion")
  implementation("io.ktor:ktor-client-serialization:$ktorVersion")

  implementation("javax.validation:validation-api:2.0.1.Final")
  implementation("com.opencsv:opencsv:$openCsvVersion")
  implementation("org.flywaydb:flyway-core:$flywayVersion")

  runtimeOnly("org.hibernate:hibernate-validator:$hibernateValidatorVersion")
  runtimeOnly("org.hsqldb:hsqldb:$hsqldbVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.mockk:mockk:$mockkVersion")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "17"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
