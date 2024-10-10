import org.apache.tools.ant.taskdefs.condition.Os
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
    Os.isFamily(Os.FAMILY_WINDOWS) -> "dll"
    Os.isFamily(Os.FAMILY_UNIX) -> "so"
    Os.isFamily(Os.FAMILY_MAC) -> "dylib"
    else -> ""
}

fun getLibName () : String = when {
    Os.isFamily(Os.FAMILY_WINDOWS) -> "orange_player"
    Os.isFamily(Os.FAMILY_UNIX) || Os.isFamily(Os.FAMILY_MAC)  -> "liborange_player"
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
            Os.isFamily(Os.FAMILY_WINDOWS) -> {
                copy {
                    from("libs/mi-win-64.tar.xz","libs/vlc-win-64.tar.xz")
                    into("src/main/resources")
                }
            }
            Os.isFamily(Os.FAMILY_UNIX) -> {
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
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation("com.github.adrielcafe.pufferdb:core:1.1.1")
    implementation("org.jetbrains.compose.material3:material3-desktop:1.6.11")
    implementation("dev.icerock.moko:mvvm-livedata-compose:0.16.1")
    implementation("uk.co.caprica:vlcj:4.8.3")
}

compose.desktop {
    application {
        mainClass = "me.sudodios.orangeplayer.OrangePlayerKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            modules("jdk.unsupported")
            packageName = "Orange Player"
            packageVersion = appVersion
            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("icons/app-icon.png"))
                packageVersion = appVersion
                debPackageVersion = appVersion
            }
            windows {
                iconFile.set(iconsRoot.resolve("icons/app-icon.ico"))
                packageVersion = appVersion
            }
        }
        buildTypes.release.proguard {
            obfuscate.set(false)
            optimize.set(true)
            configurationFiles.from(project.file("rules.pro").absolutePath)
        }
    }
}