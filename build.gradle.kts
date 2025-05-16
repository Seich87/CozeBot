import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.2"
	id("io.spring.dependency-management") version "1.1.4"
	kotlin("jvm") version "1.9.22"
	kotlin("plugin.spring") version "1.9.22"
	kotlin("plugin.jpa") version "1.9.22"
	kotlin("kapt") version "1.9.22"
}

group = "com.chatassist"
version = "1.0.0-SNAPSHOT"
description = "Telegram-бот с интеграцией Coze API для обработки запросов с использованием нейромодели, системой тарифных планов и платежной интеграцией ЮKassa."

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {
	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")

	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// Telegram Bot API
	implementation("org.telegram:telegrambots:6.8.0")
	implementation("org.telegram:telegrambots-spring-boot-starter:6.8.0")

	// База данных
	implementation("org.postgresql:postgresql")
	implementation("org.flywaydb:flyway-core")

	// Утилиты
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
	implementation("com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0")

	// Lombok
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	// Тестирование
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.mockito:mockito-core:5.3.1")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")

	// Документация API
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

// Настройка Flyway миграций
//flyway {
//	url = "jdbc:postgresql://localhost:5432/cozetalk"
//	user = "cozetalk_user"
//	password = "your_password"
//	baselineOnMigrate = true
//}

// Создание исполняемого JAR
tasks.bootJar {
	archiveFileName.set("cozetalk.jar")
	launchScript()
}

tasks.jar {
	enabled = false
}