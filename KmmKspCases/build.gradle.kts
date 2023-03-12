buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("com.azharkova.kmm.plugin:kmm_plugin:0.1.2")
    }
}

plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("7.3.1").apply(false)
    id("com.android.library").version("7.3.1").apply(false)
    kotlin("android").version("1.8.10").apply(false)
    kotlin("multiplatform").version("1.8.10").apply(false)
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    id("com.google.devtools.ksp") version "1.8.10-1.0.9" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10" apply false
  id("kmm_plugin") version "0.1.2" apply true
   id("org.javamodularity.moduleplugin") version "1.8.12" apply false
}


