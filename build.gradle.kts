plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinx.serialization)
    application
    id("com.google.cloud.tools.jib") version "3.4.3"
    id("com.ryandens.jlink-application") version "0.4.0"
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    implementation(libs.kordExtensions)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.clikt)
    implementation(libs.slf4j)
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass = "com.mineinabyss.discord.bot.MainKt"
}

tasks.jibDockerBuild {
    dependsOn("jlinkJre")
}

jlinkJre {
    modules.set(setOf("java.logging", "java.management", "jdk.crypto.ec", "java.naming")) // defaults to only java.base
}

jib {
    from.image = "gcr.io/distroless/java-base-debian11:nonroot-amd64"
    to.image = "ghcr.io/mineinabyss/discord-role-picker"
    extraDirectories {
        paths {
            path {
                setFrom(project.file("build/jlink-jre/jre"))
                into = "/usr/local"
            }
        }
        permissions = mapOf("/usr/local/bin/java" to "755")
    }
    container {
        environment = mapOf("BOT_CONFIG" to "/app/config.yml")
        creationTime = "USE_CURRENT_TIMESTAMP"
        ports = listOf("8080")

        // good defauls intended for Java 8 (>= 8u191) containers
        jvmFlags = listOf(
            "-server",
            "-Djava.awt.headless=true",
            "-XX:InitialRAMFraction=2",
            "-XX:MinRAMFraction=2",
            "-XX:MaxRAMFraction=2",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=100",
            "-XX:+UseStringDeduplication"
        )
    }
}

