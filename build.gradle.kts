plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.11.0-M2"
}

group = "org.laolittle.plugin.draw"
version = "1.0.4"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

fun skikoAwt(ver: String) = "org.jetbrains.skiko:skiko-awt-runtime-$ver"

dependencies {
    val skikoVer = "0.7.16"
    implementation(skikoAwt("windows-x64:$skikoVer"))
    implementation(skikoAwt("linux-x64:$skikoVer"))
    implementation(skikoAwt("linux-arm64:$skikoVer"))
    compileOnly("com.github.LaoLittle:SkikoMirai:1.0.4")
    testImplementation(kotlin("test"))
}