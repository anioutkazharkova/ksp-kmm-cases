import org.jetbrains.kotlin.cli.js.internal.main

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
}

kotlin {
    android()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":processor"))
                implementation(project(":network"))
                implementation(project(":core"))
                kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
            }
        }
        val androidMain by getting {
            dependencies {
                kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
        }
    }
}

android {
    namespace = "com.azharkova.kmmkspcases"
    compileSdk = 32
    defaultConfig {
        minSdk = 21
        targetSdk = 32
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}