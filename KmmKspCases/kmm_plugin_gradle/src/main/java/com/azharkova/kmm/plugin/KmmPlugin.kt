package com.azharkova.kmm.plugin

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption


class KmmGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true


    override fun getCompilerPluginId(): String = "com.azharkova.kmm.plugin.kmm-plugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.azharkova.kmm.plugin",
        artifactId = "kmm_plugin",
        version = "0.1.2"
    )

    override fun getPluginArtifactForNative(): SubpluginArtifact = SubpluginArtifact(
        groupId = "com.azharkova.kmm.plugin",
        artifactId = "kmm_plugin" + "_native",
        version = "0.1.2"
    )

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val gradleExtension = kotlinCompilation.target.project.extensions.findByType(KmmPluginGradleExtension::class.java) ?: KmmPluginGradleExtension()

        return kotlinCompilation.target.project.provider {
            val options:List<SubpluginOption> = mutableListOf()//(SubpluginOption("enabled", gradleExtension.enabled.toString()))
            options
        }
    }

}