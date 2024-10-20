import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.JvmApplicationDistributions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

val appVersion by extra { "1.0.0" }

group = "me.sudodios.orangeplayer"
version = appVersion

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
    google()
}

fun getLibExt () : String = when {
    OperatingSystem.current().isWindows -> "dll"
    OperatingSystem.current().isLinux -> "so"
    OperatingSystem.current().isMacOsX -> "dylib"
    else -> ""
}

fun getLibName () : String = when {
    OperatingSystem.current().isWindows -> "orange_player"
    OperatingSystem.current().isLinux || OperatingSystem.current().isMacOsX  -> "liborange_player"
    else -> ""
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
    dependsOn("bundleLibs")
}

tasks.register("buildCore") {
    exec {
        commandLine("cargo","build","--manifest-path=src/native/Cargo.toml","--release")
    }
    doLast {
        delete(fileTree("src/main/resources").matching {
            include("**/*.so","**/*.dll")
        })
        copy {
            from("src/native/target/release/${getLibName()}.${getLibExt()}")
            into("src/main/resources")
        }
    }
}

tasks.register("bundleLibs") {
    doFirst {
        delete(fileTree("src/main/resources").matching {
            include("**/*.xz")
        })
    }
    doLast {
        when {
            OperatingSystem.current().isWindows -> {
                copy {
                    from("libs/mi-win-64.tar.xz","libs/vlc-win-64.tar.xz")
                    into("src/main/resources")
                }
            }
            OperatingSystem.current().isLinux -> {
                copy {
                    from("libs/mi-linux-64.tar.xz","libs/vlc-linux-64.tar.xz")
                    into("src/main/resources")
                }
            }
        }
    }
    dependsOn("buildCore")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(compose.desktop.currentOs) {
        exclude(group = "org.jetbrains.compose.material")
    }
    api(compose.animation)
    api(compose.foundation)
    api(compose.components.resources)
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation("com.github.adrielcafe.satchel:satchel-core:1.0.3")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.7.0")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("uk.co.caprica:vlcj:4.8.3")
}

fun JvmApplicationDistributions.addFileAssociations () {
    fileAssociation(
        extension = "mp3",
        mimeType = "audio/mpeg",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "aac",
        mimeType = "audio/aac",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "avi",
        mimeType = "video/x-msvideo",
        description = "VideoFile"
    )
    fileAssociation(
        extension = "flac",
        mimeType = "audio/flac",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "m4a",
        mimeType = "audio/mp4",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "mkv",
        mimeType = "video/x-matroska",
        description = "VideoFile"
    )
    fileAssociation(
        extension = "mov",
        mimeType = "video/quicktime",
        description = "VideoFile"
    )
    fileAssociation(
        extension = "mp4",
        mimeType = "video/mp4",
        description = "VideoFile"
    )
    fileAssociation(
        extension = "ogg",
        mimeType = "audio/ogg",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "wav",
        mimeType = "audio/wav",
        description = "AudioFile"
    )
    fileAssociation(
        extension = "wmv",
        mimeType = "video/x-ms-wmv",
        description = "VideoFile"
    )
}

compose.desktop {
    application {
        mainClass = "me.sudodios.orangeplayer.OrangePlayerKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("jdk.unsupported")
            packageName = "Orange Player"
            packageVersion = appVersion
            addFileAssociations()
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("icons/app-icon.png"))
                packageVersion = appVersion
                debPackageVersion = appVersion
            }
            windows {
                iconFile.set(iconsRoot.resolve("icons/app-icon.ico"))
                packageVersion = appVersion
                shortcut = true
            }
        }
        buildTypes.release.proguard {
            obfuscate.set(false)
            optimize.set(true)
            configurationFiles.from(project.file("rules.pro").absolutePath)
        }
    }
}