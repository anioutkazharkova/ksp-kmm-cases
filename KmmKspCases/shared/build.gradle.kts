import org.jetbrains.kotlin.cli.js.internal.main

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization")
    id("kmm_plugin")
}
apply(plugin =  "kmm_plugin")

kotlin {
    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + "-Xallow-result-return-type"
            }
        }
    }
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
        configureEach {
            kotlin.srcDir("$buildDir/generated/ksp/$name/kotlin/")
        }
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
                implementation(project(":processor"))
                implementation(project(":network"))
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
              implementation("com.azharkova.kmm.plugin:kmm_plugin_runtime:0.1.2")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
            }
        }
        val androidMain by getting {
            dependencies {
             // kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
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
           // kotlin.srcDir("${buildDir.absolutePath}/generated/ksp/")
        }
    }
}

android {
    namespace = "com.azharkova.kmmkspcases"
    compileSdk = 33
    defaultConfig {
        minSdk = 26
        targetSdk = 33
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":processor"))
    add("kspCommonMainMetadata", project(":processor"))
    add("kspIosX64", project(":processor"))
    add("kspIosSimulatorArm64",  project(":processor"))
    add("kspIosArm64", project(":processor"))
    add("kspAndroid", project(":processor"))
}