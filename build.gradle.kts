import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.collections.*
plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "land.generic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://kotlin.bintray.com/kotlinx")
}
object Versions {
    const val kord = "0.7.0-SNAPSHOT"
    const val clikt = "3.1.0"
    const val coroutines_core = "1.4.2"
    const val kotlinx_cli = "0.3.1"
    const val better_parse = "0.4.1"
}
dependencies {
    implementation("commons-beanutils:commons-beanutils:1.9.3")
    implementation("org.apache.commons:commons-configuration2:2.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines_core}")
    implementation("com.github.ajalt.clikt:clikt:${Versions.clikt}")
    implementation("dev.kord:kord-core:${Versions.kord}")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:${Versions.kotlinx_cli}")
    implementation("com.github.h0tk3y.betterParse:better-parse:${Versions.better_parse}")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("io.github.microutils:kotlin-logging:2.0.4")
    implementation("org.slf4j:slf4j-log4j12:1.7.29")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.1")
    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}
tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes(
            "Main-Class" to "land.generic.dorfbot.MainKt"
        )
    }

    // To add all of the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
application {
    mainClassName = "MainKt"
}
