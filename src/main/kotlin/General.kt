package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.info
import java.io.File

internal fun String.split(): List<String>? {
    val words =
        when {
            this.length == 1 -> listOf(this, " ")
            contains("\n") -> split(Regex("\n+"))
            else -> trim().split(Regex("[\\s　]+"))
        }.toMutableList()

    if (words.isEmpty()) return null
    if (words.size == 1) {
        words.apply {
            val sentence = words[0].also { if (it.isBlank()) return null }
            clear()
            val left = sentence.length shr 1
            add(sentence.substring(0, left))
            if (sentence.length == 1)
                add(" ")
            else
                add(sentence.substring(left, sentence.length))
        }
    }
    return words
}

internal val emojiMixFolder by lazy {
    DrawMeme.dataFolder.resolve("emojimix")
        .also(File::mkdirs)
}

internal const val emojiMixURL = "https://www.gstatic.com/android/keyboard/emojikitchen"
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
        else HttpClient(OkHttp).use { client ->
            client.get<ByteArray>("$emojiMixURL/$date/u$mainCode/$fileName").also { bytes ->
                file.writeBytes(bytes)
            }
            file
        }
    }.onFailure { DrawMeme.logger.error(it) }.getOrNull()
}

private val supportedEmojis by lazy {
    runBlocking(DrawMeme.coroutineContext) {
        val logger by DrawMeme::logger
        logger.info { "开始获取支持的Emoji列表" }

        val emo = mutableMapOf<Int, Long>()
        val returnStr: String = HttpClient(OkHttp).use {
            it.get("https://tikolu.net/emojimix/emojis.js")
        }

        val regex = Regex("""\[\[(.+)], "(\d+)"""")
        val finds = regex.findAll(returnStr)

        finds.forEach { result ->
            result.groupValues[1].split(",").forEach {
                emo[it.replace(" ", "").toInt()] = result.groupValues[2].toLong()
            }
        }
        emo
    }
}

internal suspend fun MessageEvent.getOrWaitImage(): Image? {
    return (message.takeIf { m -> m.contains(Image) } ?: runCatching {
        subject.sendMessage("请在30s内发送图片")
        nextMessage(30_000) { event -> event.message.contains(Image) }
    }.getOrElse { e ->
        when (e) {
            is TimeoutCancellationException -> {
                subject.sendMessage(PlainText("超时未发送").plus(message.quote()))
                return null
            }
            else -> throw e
        }
    }).firstIsInstanceOrNull<Image>()
}