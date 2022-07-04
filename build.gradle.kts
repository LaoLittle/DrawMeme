plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.3"
}

group = "org.laolittle.plugin.draw"
version = "1.2.4"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

dependencies {
    val smVer = "1.0.8"
    compileOnly("com.github.LaoLittle:SkikoMirai:$smVer")
    testImplementation("com.github.LaoLittle:SkikoMirai:$smVer")
    testImplementation(kotlin("test"))

    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X" -> "macos"
        osName.startsWith("Win") -> "windows"
        osName.startsWith("Linux") -> "linux"
        else -> error("Unsupported OS: $osName")
    }

    val targetArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: $osArch")
    }

    val version = "0.7.22"
    val target = "${targetOs}-${targetArch}"

    testImplementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
}