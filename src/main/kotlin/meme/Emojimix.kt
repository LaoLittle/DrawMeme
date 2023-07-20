package org.laolittle.plugin.draw.meme

import io.ktor.client.call.*
import io.ktor.client.network.sockets.*
import io.ktor.client.request.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.draw.extension.Emoji
import org.laolittle.plugin.draw.httpClient
import org.laolittle.plugin.draw.json
import org.laolittle.plugin.draw.logger
import java.io.File

internal val emojiMixFolder by lazy {
    DrawMeme.dataFolder.resolve("emojimix")
        .also(File::mkdirs)
}

private const val emojiMixURL = "https://www.gstatic.com/android/keyboard/emojikitchen"
internal suspend fun getEmojiMix(main: Emoji, aux: Emoji): File? {
    var mainCode = main.code.toString(16)
    val auxCode = aux.code.toString(16)

    var fileName = "u${mainCode}_u${auxCode}"

    val date = supportedEmojis[fileName] ?: supportedEmojis["u${auxCode}_u${mainCode}"
        .also {
            fileName = it
            mainCode = auxCode
        }] ?: return null

    fileName += ".png"

    val file = emojiMixFolder.resolve(fileName)

    return runCatching {
        if (file.isFile) file
        else {
            file.writeBytes(httpClient.get("$emojiMixURL/$date/u$mainCode/$fileName").body())
            file
        }

    }.onFailure {
        when (it) {
            is SocketTimeoutException -> logger.error("${it.message} 获取图片失败，可能是没有这张EmojiMix图片: $main $aux")
            is ConnectTimeoutException -> logger.error("${it.message} EmojiMix连接失败，请检查你的网络")
            else -> logger.error(it)
        }
    }.getOrNull()
}

@OptIn(ExperimentalSerializationApi::class)
private val supportedEmojis by lazy {
    // logger.info { "开始获取支持的Emoji列表" }
    val emojis = DrawMeme::class.java.getResourceAsStream("/kitchen.json")?.use {
        String(it.readBytes())
    } ?: throw IllegalStateException("Unable to get emoji list")

    /*val regex = Regex("""\[\[(.+)], "(\d+)"""")
    val finds = regex.findAll(emojis)

    finds.forEach { result ->
        result.groupValues[1].split(",").forEach {
            emo[it.replace(" ", "").toInt()] = result.groupValues[2].toLong()
        }
    }*/

    val emo: Map<String, Long> = json.decodeFromString(emojis)
    emo
}