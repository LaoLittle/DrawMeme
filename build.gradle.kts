plugins {
    val kotlinVersion = "1.6.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.10.0"
}

group = "org.laolittle.plugin.draw"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

fun skikoAwt(ver: String) = "org.jetbrains.skiko:skiko-awt-runtime-$ver"

dependencies {
    val skikoVer = "0.7.5"
    implementation(skikoAwt("windows-x64:$skikoVer"))
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}