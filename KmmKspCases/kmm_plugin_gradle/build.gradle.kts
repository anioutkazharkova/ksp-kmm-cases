import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    id("maven-publish")
}

group = "com.azharkova.kmm.plugin"
version = "0.1.2"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

gradlePlugin {
    plugins {
        create("kmm_plugin") {
            id = "kmm_plugin"
            displayName = "Kotlin Debug Log compiler plugin"
            description = "Kotlin compiler plugin to add debug logging to functions"
            implementationClass = "com.azharkova.kmm.plugin.KmmGradlePlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}
