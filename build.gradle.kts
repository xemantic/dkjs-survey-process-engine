/*
 * dkjs-survey-process-engine - https://www.dkjs.de/
 * Copyright (C) 2022 Kazimierz Pogoda / https://xemantic.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.7.0"
  kotlin("plugin.serialization") version "1.7.0"
  kotlin("plugin.spring") version "1.7.0"
  kotlin("plugin.jpa") version "1.7.0"
  id("org.springframework.boot") version "2.7.0"
  id("com.github.ben-manes.versions") version "0.42.0"
  id("com.gorylenko.gradle-git-properties") version "2.4.1"
}

apply(plugin = "io.spring.dependency-management")

val kotlinxCoroutinesVersion = "1.6.2"
val kotlinxSerializationVersion = "1.3.3"
val ktorVersion = "2.0.2"
val opencsvVersion = "5.6"
val hibernateValidatorVersion = "7.0.4.Final"
val kotestVersion = "5.3.1"
val mockkVersion = "1.12.4"
val hsqldbVersion = "2.6.1"
val flywayVersion = "8.5.13"
val byteBuddyVersion = "1.12.10"
val springMockkVersion = "3.1.1"

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
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxCoroutinesVersion")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinxCoroutinesVersion")
  implementation("javax.inject:javax.inject:1")
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
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

  implementation("javax.validation:validation-api:2.0.1.Final")
  implementation("com.opencsv:opencsv:$opencsvVersion")
  implementation("org.flywaydb:flyway-core:$flywayVersion")
  implementation("net.bytebuddy:byte-buddy:$byteBuddyVersion")
  implementation("net.bytebuddy:byte-buddy-agent:$byteBuddyVersion")

  runtimeOnly("org.hibernate:hibernate-validator:$hibernateValidatorVersion")
  runtimeOnly("org.hsqldb:hsqldb:$hsqldbVersion")

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(module = "mockito-core")
  }
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.mockk:mockk:$mockkVersion")
  testImplementation("com.ninja-squad:springmockk:$springMockkVersion")
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
