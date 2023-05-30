plugins {
    `core-script`
    id("fabric-loom")
    id("io.github.juuxel.loom-quiltflower")
}

repositories {
    mavenCentral()
    maven {
        name = "JitPack"
        setUrl("https://jitpack.io")
    }
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

dependencies {
    minecraft("com.mojang:minecraft:1.19.4")
    mappings(loom.officialMojangMappings())

    val ktorVersion = property("ktorVersion")
    val silkVersion = "1.9.8"
    modImplementation("net.silkmc:silk-commands:$silkVersion")
    modImplementation("net.silkmc:silk-core:$silkVersion")
    modImplementation("net.silkmc:silk-nbt:$silkVersion")
    modImplementation("net.silkmc:silk-persistence:$silkVersion")
//    modImplementation("net.silkmc:silk-network:1.9.6") -> Only for Client
    modImplementation("net.fabricmc:fabric-loader:0.14.19")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.80.0+1.19.4")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
    modImplementation(include("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT"))
    modImplementation(include("net.kyori:adventure-platform-fabric:5.8.0")!!)
    modImplementation(include("org.yaml:snakeyaml:1.33")!!)

    transitiveInclude("io.ktor:ktor-server-core-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    transitiveInclude("io.ktor:ktor-server-cio:$ktorVersion")

    implementation(include(project(":vanilla"))!!)

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xskip-prerelease-check"
        }
    }
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("net.silkmc.silk.core.annotations.ExperimentalSilkApi")
        }
    }
}
