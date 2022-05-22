package org.laolittle.plugin.draw.meme

import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.utils.info
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.draw.Emoji
import org.laolittle.plugin.draw.httpClient
import org.laolittle.plugin.draw.logger
import java.io.File

internal val emojiMixFolder by lazy {
    DrawMeme.dataFolder.resolve("emojimix")
        .also(File::mkdirs)
}

private const val emojiMixURL = "https://www.gstatic.com/android/keyboard/emojikitchen"
internal suspend fun getEmojiMix(main: Emoji, aux: Emoji): File? {
    val mainCode = main.code.toString(16)
    val auxCode = aux.code.toString(16)
    val date = supportedEmojis[main.code] ?: return null

    val fileName = "u${mainCode}_u${auxCode}.png"
    val file = emojiMixFolder
        .resolve(fileName)
    val giaFile = emojiMixFolder.resolve("u${auxCode}_u${mainCode}.png")

    return runCatching {
        if (file.isFile) file
        else if (giaFile.isFile) giaFile
        else {
            httpClient.get<ByteArray>("$emojiMixURL/$date/u$mainCode/$fileName").also { bytes ->
                file.writeBytes(bytes)
            }
            file
        }

    }.onFailure { logger.error(it) }.getOrNull()
}

private val supportedEmojis by lazy {
    runBlocking(DrawMeme.coroutineContext) {

        logger.info { "开始获取支持的Emoji列表" }

        val emo = mutableMapOf<Int, Long>()
        val emojis = kotlin.runCatching {
            httpClient.get<String>("https://tikolu.net/emojimix/emojis.js")
        }.getOrElse {
            DrawMeme::class.java.getResourceAsStream("/emojis.txt")?.use {
                String(it.readBytes())
            } ?: throw IllegalStateException("Unable to get emoji list")
        }

        val regex = Regex("""\[\[(.+)], "(\d+)"""")
        val finds = regex.findAll(emojis)

        finds.forEach { result ->
            result.groupValues[1].split(",").forEach {
                emo[it.replace(" ", "").toInt()] = result.groupValues[2].toLong()
            }
        }
        emo
    }
}