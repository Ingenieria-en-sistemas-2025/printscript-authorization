plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
    id("org.springframework.boot") version "3.5.6"
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
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("com.h2database:h2")
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

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

spotless {
    kotlin {
        target("**/*.kt")
        ktlint("0.50.0").editorConfigOverride(
            mapOf("max_line_length" to "400", "indent_size" to "4"),
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

jacoco { toolVersion = "0.8.10" }

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.register<JacocoCoverageVerification>("jacocoVerify") {
    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal() // 80%
            }
        }
    }
}

tasks.check {
    dependsOn("detekt", "spotlessCheck", "jacocoVerify")
}

// Git hooks
val gitDir = layout.projectDirectory.dir(".git")
val hooksSrc = layout.projectDirectory.dir("hooks")
val hooksDst = layout.projectDirectory.dir(".git/hooks")

tasks.register<Copy>("installGitHooks") {
    onlyIf { gitDir.asFile.exists() && hooksSrc.asFile.exists() }

    from(hooksSrc)
    into(hooksDst)
    fileMode = Integer.parseInt("775", 8)
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register("printGitHooksStatus") {
    outputs.upToDateWhen { false }
    doLast {
        val preCommit = hooksDst.file("pre-commit").asFile
        val prePush   = hooksDst.file("pre-push").asFile

        println("Git dir       : ${gitDir.asFile.absolutePath} " + if (gitDir.asFile.exists()) "(OK)" else "(MISSING)")
        println("Source hooks  : ${hooksSrc.asFile.absolutePath} " + if (hooksSrc.asFile.exists()) "(OK)" else "(MISSING)")
        println("Target hooks  : ${hooksDst.asFile.absolutePath} " + if (hooksDst.asFile.exists()) "(OK)" else "(MISSING)")
        println("pre-commit    : " + if (preCommit.exists()) "OK" else "MISSING")
        println("pre-push      : " + if (prePush.exists()) "OK" else "MISSING")
    }
}

tasks.register("ensureGitHooks") {
    dependsOn("installGitHooks", "printGitHooksStatus")
    onlyIf { gitDir.asFile.exists() }
}

listOf("build", "check", "test", "assemble").forEach { taskName ->
    tasks.named(taskName).configure { dependsOn("ensureGitHooks") }
}

