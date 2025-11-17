plugins {
    // para compilar Kotlin en la JVM (Java)
    kotlin("jvm") version "1.9.23"

    // integra Kotlin con Spring
    kotlin("plugin.spring") version "1.9.23"

    // trabajar con JPA (entidades Kotlin + Hibernate)
    kotlin("plugin.jpa") version "1.9.23"

    id("org.springframework.boot") version "3.5.6"

    // Manejo automatico de versiones de dependencias de Spring
    id("io.spring.dependency-management") version "1.1.7"

    id("jacoco")

    id("com.diffplug.spotless") version "6.25.0"

    id("io.gitlab.arturbosch.detekt") version "1.23.6"
}

group = "com.printscript"
version = "0.0.1-SNAPSHOT"
description = "PrintScript Snippets microservice"

java {
    toolchain {
        // a Gradle -> usá Java 21 para compilar
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    // Repositorio publico para dependencias de Java/Kotlin
    mavenCentral()
}

dependencies {
    // JPA + Hibernate + Repositorios (para usar @Entity, JpaRepository, etc.)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Controladores REST, JSON, servidor HTTP (para usar @RestController, @GetMapping, etc.)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    // Reflexion en Kotlin (Spring lo usa internamente para instanciar beans)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Validacion de datos con anotaciones como @NotBlank, @Email, @Valid
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("com.newrelic.agent.java:newrelic-api:8.10.0")

    // desarrollo (no prod): hot reload, restart automatico, LiveReload
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // Driver JDBC (Java Database Connectivity) de PostgreSQL
    runtimeOnly("org.postgresql:postgresql")

    // Starter general de tests de Spring Boot: JUnit 5, Mockito, AssertJ, etc.
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    // Aserciones y helpers especificos de Kotlin para JUnit 5
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("com.h2database:h2")
    // Herramientas para testear endpoints protegidos: mockear .jwt en MockMvc
    testImplementation("org.springframework.security:spring-security-test")
}

// allOpen es necesario pq en Kotlin las clases son final por defecto
// JPA/Hibernate necesita las entidades open para poder proxificarlas (extender)
allOpen {
    annotation("jakarta.persistence.Entity") // @Entity
    annotation("jakarta.persistence.MappedSuperclass") // @MappedSuperclass
}

// Config gral para tareas de test: usar JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
    // Cuando terminen los tests, ejecutar automaticamente el reporte de Jacoco
    finalizedBy(tasks.jacocoTestReport)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.50.0").editorConfigOverride(
            mapOf(
                "max_line_length" to "400",
                "indent_size" to "4",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.50.0")
        trimTrailingWhitespace()
        endWithNewline()
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$rootDir/config/detekt/detekt.yml")
}

jacoco {
    toolVersion = "0.8.10"
}

val jacocoExcludes = listOf(
    "com/printscript/authorization/PingController*",
    "com/printscript/authorization/AuthorizationApplication*",
    "com/printscript/authorization/AuthorizationApplicationKt*",
    "com/printscript/authorization/config/*",
    "com/printscript/authorization/controller/Error500Controller*",
)

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(jacocoExcludes)
                }
            },
        ),
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal() // 80%
            }
        }
    }
    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(jacocoExcludes)
                }
            },
        ),
    )
}

tasks.check {
    dependsOn("detekt", "spotlessCheck", tasks.jacocoTestCoverageVerification)
}

// Git hooks
val gitDir = layout.projectDirectory.dir(".git")
val hooksSrc = layout.projectDirectory.dir("hooks")
val hooksDst = layout.projectDirectory.dir(".git/hooks")

// Tarea para instalar los hooks desde la carpeta hooks/ hacia .git/hooks
tasks.register<Copy>("installGitHooks") {
    onlyIf { gitDir.asFile.exists() && hooksSrc.asFile.exists() }
    from(hooksSrc)
    into(hooksDst)
    fileMode = Integer.parseInt("775", 8) // chmod +x
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register("ensureGitHooks") {
    dependsOn("installGitHooks")
    onlyIf { gitDir.asFile.exists() }
}
