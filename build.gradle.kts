plugins {
    val kotlinVersion = "1.6.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.1"
}

group = "org.laolittle.plugin.draw"
version = "1.0.4"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

dependencies {
    val smVer = "1.0.6"
    compileOnly("com.github.LaoLittle:SkikoMirai:$smVer")
    testImplementation("com.github.LaoLittle:SkikoMirai:$smVer")
    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-windows-x64:0.7.18")
    testImplementation(kotlin("test"))
}

kotlin {
    sourceSets.all {
        languageSettings.apply {
            languageVersion = "1.7"
        }
    }
}