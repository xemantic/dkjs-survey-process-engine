/*
 * Copyright (c) 2021 Kazimierz Pogoda / Xemantic
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.6.10"
  kotlin("plugin.serialization") version "1.6.10"
  id("org.springframework.boot") version "2.6.2"
}

apply(plugin = "io.spring.dependency-management")

val ktorVersion = "1.6.7"
val kotlinxSerializationVersion = "1.3.2"
val kotestVersion = "5.0.3"
val greenmailVersion = "2.0.0-alpha-2"

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
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

  implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude("org.springframework.boot", "spring-boot-starter-tomcat")
  }
  implementation("org.springframework.boot:spring-boot-starter-undertow")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-data-rest")
  implementation("org.springframework.boot:spring-boot-starter-mail")
  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("org.springframework.data:spring-data-rest-hal-explorer")

  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-cio:$ktorVersion")
  implementation("io.ktor:ktor-client-auth:$ktorVersion")
  implementation("io.ktor:ktor-client-serialization:$ktorVersion")

  implementation("javax.validation:validation-api:2.0.1.Final")
  implementation("org.hibernate:hibernate-validator:7.0.1.Final")
  implementation("org.hsqldb:hsqldb:2.6.1")
  implementation("com.opencsv:opencsv:5.5.2")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.mockk:mockk:1.12.2")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    jvmTarget = "17"
  }
}
