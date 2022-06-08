package config

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object FunctionConfig : AutoSavePluginConfig("FunctionConfig") {
    @Serializable
    data class Configuration(
        val enable: Boolean = true,
        val groups: MutableSet<Long> = mutableSetOf()
    )

    val configuration by value(hashMapOf<String, Configuration>())
}

val configuration by FunctionConfig::configuration