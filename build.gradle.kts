plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.0"
}

group = "org.laolittle.plugin.draw"
version = "1.0.3"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}

fun skikoAwt(ver: String) = "org.jetbrains.skiko:skiko-awt-runtime-$ver"

dependencies {
    val skikoVer = "0.7.12"
    implementation(skikoAwt("windows-x64:$skikoVer"))
    implementation(skikoAwt("linux-x64:$skikoVer"))
    implementation(skikoAwt("linux-arm64:$skikoVer"))
    implementation(skikoAwt("linux-arm64:$skikoVer"))
    implementation("com.github.LaoLittle:SkikoMirai:1.0.3")
}