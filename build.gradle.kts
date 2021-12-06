plugins {
  kotlin("jvm") version "1.6.0"
  id("org.springframework.boot") version "2.6.1"
}

apply(plugin = "io.spring.dependency-management")

group = "de.dkjs.survey"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("org.springframework.boot:spring-boot-starter-web") {
    exclude("org.springframework.boot", "spring-boot-starter-tomcat")
  }
  implementation("org.springframework.boot:spring-boot-starter-undertow")
}
