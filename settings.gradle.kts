rootProject.name = "SpongeForge"

include(":SpongeCommon")
include(":SpongeCommon:SpongeAPI")
// This is only needed so SpongeCommon can be imported properly, we don't want differences between execution goals
// of scope.
include(":SpongeCommon:SpongeVanilla")
project(":SpongeCommon:SpongeVanilla").projectDir =file("SpongeCommon/vanilla")

pluginManagement {
    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven("https://repo.spongepowered.org/maven")
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("org.spongepowered.gradle.")) {
                val version = requested.version ?: "0.11.3"
                useModule("org.spongepowered:SpongeGradle:$version")
            }
        }
    }

}