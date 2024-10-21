import io.github.themrmilchmann.gradle.publish.curseforge.ChangelogFormat
import io.github.themrmilchmann.gradle.publish.curseforge.GameVersion
import io.github.themrmilchmann.gradle.publish.curseforge.ReleaseType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    id("fabric-loom") version "1.7.1"
    id("maven-publish")
    id("io.github.themrmilchmann.curseforge-publish") version "0.6.1"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 17
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("infinistorage") {
            sourceSet("main")
        }
    }
}

repositories {
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

curseforge {
    apiToken = providers.gradleProperty("curseForgeToken")
    publications {
        named("fabric") {
            projectId.set("1124656")

            artifacts.named("main") {
                displayName = "InfiniStorage - ${project.version}"
                releaseType = ReleaseType.RELEASE

                changelog {
                    format = ChangelogFormat.MARKDOWN

                    val modVersion = version.toString().split("-")[0]
                    val minecraftVersion =
                        version.toString().substring(startIndex = modVersion.length + 1).split("-")[0]
                    val minecraftVersionGroup =
                        if (minecraftVersion.count { it == '.' } == 1) minecraftVersion else minecraftVersion.substringBeforeLast(
                            '.'
                        )
                    content = File(
                        rootDir,
                        "docs/$minecraftVersionGroup/changelog-$modVersion-$minecraftVersion.md"
                    ).readText()
                }
            }
        }
    }
}