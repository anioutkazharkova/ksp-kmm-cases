import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    id("maven-publish")
}

dependencies {
    implementation("junit:junit:4.12")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.10")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:1.8.10")
    implementation(project(":kmm_plugin_runtime"))
    kapt("com.google.auto.service:auto-service:1.0-rc7")
    compileOnly("com.google.auto.service:auto-service-annotations:1.0-rc7")

    testImplementation(kotlin("test-junit"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")

    testImplementation("org.mockito:mockito-core:4.7.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.assertj:assertj-core:3.23.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.test {
    useJUnitPlatform()
}

group = "com.azharkova.kmm.plugin"
version = "0.1.2"

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}
