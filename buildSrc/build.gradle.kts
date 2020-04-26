plugins {
    `kotlin-dsl`
    `java-library`
    idea
}

subprojects {
    dependencies {
        gradleApi()
        gradleKotlinDsl()
    }
    apply(plugin = "org.gradle.kotlin.kotlin-dsl")
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    jcenter()
    maven(url = "https://files.minecraftforge.net/maven")
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    implementation("net.minecrell.licenser:net.minecrell.licenser.gradle.plugin:0.4.1")
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.+")
}
