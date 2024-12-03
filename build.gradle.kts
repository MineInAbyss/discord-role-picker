plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.graalvm.nativeimage)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kord)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kaml)
    implementation(libs.clikt)
    implementation(libs.slf4j)
}

kotlin {
    jvmToolchain(23) // want this for graalvm 23 despite kotlin not supporting yet
}
java {
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

application {
    mainClass = "MainKt"
}

graalvmNative {
    binaries {
        named("main") {
            fallback.set(false)
            verbose.set(false)

            buildArgs.addAll(
                "--initialize-at-build-time",
                "-Os"
            )
            imageName.set("discord-role-picker")
//            runtimeArgs.add("-Xmx10m")
        }
    }
}
