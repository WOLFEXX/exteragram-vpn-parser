plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("maven-publish")
    id("signing")
}

group = "com.exteragram.plugin"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.github.com/${project.group}/${project.name}") }
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-json:3.0.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    // Exteragram plugin API is provided at runtime by Exteragram
    // API interfaces are included locally in this plugin
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Exteragram/Exteragram")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GPR_USER") ?: ""
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GPR_KEY") ?: ""
            }
        }
    }
}

signing {
    useInMemoryPgpKeys(
        project.findProperty("signing.key") as String? ?: "",
        project.findProperty("signing.password") as String? ?: ""
    )
    sign(publishing.publications["maven"])
}
