package org.laolittle.plugin.draw

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object FunctionConfig : AutoSavePluginConfig("FunctionConfig") {
    @kotlinx.serialization.Serializable
    data class Configuration(
        val enable: Boolean = true,
        val groups: MutableSet<Long> = mutableSetOf()
    )
}

val configuration by FunctionConfig.value(hashMapOf<String, FunctionConfig.Configuration>())