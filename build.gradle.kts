import org.spongepowered.gradle.dev.SourceType
import org.spongepowered.gradle.dev.sourceSet

buildscript {
    repositories {
        maven("https://repo.spongepowered.org/maven")
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}
plugins {
    id("org.spongepowered.gradle.sponge.impl") version "0.11.4-SNAPSHOT"
    id("net.minecraftforge.gradle")
}

apply {
    plugin("org.spongepowered.mixin")
}
tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xmaxerrs", "1000"))
    }
}

spongeDev {
    common(project.project(":SpongeCommon"))
    api(common.map { it.project("SpongeAPI") })
    addForgeFlower.set(true)
    addedSourceSets {
        register("mixins") {
            sourceType.set(SourceType.Mixin)
            configurations += arrayOf("launch", "minecraft")
        }
        register("accessors") {
            sourceType.set(SourceType.Accessor)
        }
        register("launch") {
            sourceType.set(SourceType.Launch)
        }
        register("modlauncher") {
            dependsOn += "launch"
            configurations += "launch"
        }
        register("invalid") {
            sourceType.set(SourceType.Invalid)
            configurations += arrayOf("launch", "minecraft")
        }
    }
}

val spongeForge = this
val common by spongeDev.common
val mcpType: String by common
val mcpMappings: String by common
val launch by configurations.creating
val forgeVersion: String by project

dependencies {
    minecraft("net.minecraftforge:forge:$forgeVersion")
    implementation(project(common.path)) {
        exclude(group = "net.minecraft", module = "server")
    }

    launch("net.sf.jopt-simple:jopt-simple:5.0.4")
    launch(group = "org.spongepowered", name = "plugin-meta", version = "0.4.1")
    "mixinsImplementation"(project(common.path)) {
        exclude(group = "net.minecraft", module = "server")
    }
    // Annotation Processor
    "accessorsAnnotationProcessor"(launch)
    "mixinsAnnotationProcessor"(launch)
    "accessorsAnnotationProcessor"("org.spongepowered:mixin:0.8")
    "mixinsAnnotationProcessor"("org.spongepowered:mixin:0.8")
}

minecraft {
    mappings(mcpType, mcpMappings)
    runs {
        create("server") {
            workingDirectory(project.file("./run"))
            mods {
                create("sponge") {
                    source(project.sourceSets["main"])
                }
            }
        }
        create("client") {
            workingDirectory(project.file("./run"))

            // Recommended logging data for a userdev environment
//            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
//            property 'forge.logging.console.level', 'debug'

            mods {
                create("sponge") {
                    source(project.sourceSets["main"])
                }
            }
        }

        create("server2") {
            workingDirectory(project.file("./run"))

            // Recommended logging data for a userdev environment
//            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
//            property 'forge.logging.console.level', 'debug'

            mods {
                create("sponge") {
                    source(project.sourceSets["main"])
                }
            }
        }

        create("data") {
            workingDirectory(project.file("./run"))

            // Recommended logging data for a userdev environment
//            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
//            property 'forge.logging.console.level', 'debug'

//            args '--mod', 'examplemod', '--all', '--output', file('src/generated/resources/')

            mods {
                create("sponge") {
                    source(project.sourceSets["main"])
                }
            }
        }
    }

    project.sourceSets["main"].resources
            .filter { it.name.endsWith("_at.cfg") }
            .files
            .forEach { accessTransformer(it) }

    common.afterEvaluate {
        common.sourceSets["main"].resources
                    .filter { it.name.endsWith("_at.cfg") }
                    .files
                    .forEach { accessTransformer(it) }
    }
}

