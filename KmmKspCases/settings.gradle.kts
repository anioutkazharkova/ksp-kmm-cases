pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
        google()
        maven (url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/bootstrap")
    }
}

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "KmmKspCases"
include(":androidApp")
include(":shared")
include(":processor")
include(":annotations")
include(":network")
include(":core")
include(":kmm_plugin")
include(":kmm_plugin_runtime")
include(":kmm_plugin_native")
include(":kmm_plugin_gradle")
