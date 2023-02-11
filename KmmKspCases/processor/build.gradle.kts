plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

repositories {
    google()
    mavenLocal()
    mavenCentral()
    maven(url = "https://jitpack.io")
}

kotlin {
    //this is only used as kapt (annotation processor, so pure jvm)
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    )
    sourceSets {

        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(project(":network"))
                implementation(project(":core"))
                implementation(project(":annotations"))
                implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.8")
                implementation("com.squareup:kotlinpoet-ksp:1.12.0")
                implementation("com.squareup:kotlinpoet:1.12.0")
            }
        }
    }
}

