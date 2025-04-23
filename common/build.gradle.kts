plugins {
    application
    kotlin("jvm") version "2.0.10"
    id("org.jetbrains.dokka-javadoc") version "2.0.0"
    id("org.jetbrains.dokka") version "2.0.0"
    kotlin("plugin.serialization") version "2.1.20"
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("documentation/html"))
}

application {
    mainClass = "MainKt"
}

group = "org.itmo"
version = "2.0-beta"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // Set JDK version (e.g., Java 17)
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("reflect"))
    implementation("com.rabbitmq:amqp-client:5.18.0")
    implementation("org.slf4j:slf4j-nop:2.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    // Otherwise you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
    // To avoid the duplicate handling strategy error
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    // To add all of the dependencies otherwise a "NoClassDefFoundError" error
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}