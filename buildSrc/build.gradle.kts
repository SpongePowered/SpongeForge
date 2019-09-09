plugins {
    `kotlin-dsl`
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
    jcenter()
    maven(url = "https://files.minecraftforge.net/maven")
}

dependencies {
    implementation("net.minecrell.licenser:net.minecrell.licenser.gradle.plugin:0.4.1")
    implementation("net.minecraftforge.gradle:ForgeGradle:3.0.141")
    implementation(group = "org.spongepowered", name = "SpongeGradle", version = "0.11.0-SNAPSHOT")
}

