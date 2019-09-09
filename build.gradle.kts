plugins {
    id("org.spongepowered.gradle.sponge.impl")
    id("net.minecraftforge.gradle")
}

val commonProj = project(":SpongeCommon")


dependencies {
    minecraft("net.minecraftforge:forge:1.14.4-28.0.95")
}

minecraft {
    mappings("snapshot", commonProj.properties["mcpMappings"]!! as String)
//
//    runs {
//        server {
//            workingDirectory = project.file("../run")
//
//        }
//    }
}


spongeDev {
    api = project(":SpongeCommon:SpongeAPI")
    common = commonProj
}
