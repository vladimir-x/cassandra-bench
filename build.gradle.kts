plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "ru.dude"
version = "0.0.1-SNAPSHOT"
description = "Cassandra check project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}


// Определяем переменную для выбора драйвера (по умолчанию cassandra)
// ./gradlew clean bootRun -PdbDriver=cassandra
// ./gradlew clean bootRun -PdbDriver=scylla
val dbDriver = project.findProperty("dbDriver") ?: "cassandra"


dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-cassandra")

	if (dbDriver == "scylla") {
		// Исключаем стандартный драйвер Cassandra и берем Scylla
		configurations.all {
			exclude(group = "com.datastax.oss", module = "java-driver-core")
		}
		implementation("com.scylladb:java-driver-core:4.19.0.9")
	} else {
		// Используется стандартный драйвер из spring-boot-starter
		implementation("com.datastax.oss:java-driver-core:4.17.0")
	}

	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
