package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
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
internal suspend fun getEmojiMix(main: Emoji, aux: Emoji): ByteArray? {
    val mainCode = main.code.toString(16)
    val auxCode = aux.code.toString(16)
    val date = supportedEmojis[main.code] ?: return null

    val fileName = "u${mainCode}_u${auxCode}.png"
    val file = emojiMixFolder
        .resolve(fileName)
    val giaFile = emojiMixFolder.resolve("u${auxCode}_u${mainCode}.png")

    return runCatching {
        if (file.isFile) file.readBytes()
        else if (giaFile.isFile) giaFile.readBytes()
        else HttpClient(OkHttp).use { client ->
            client.get<ByteArray>("$emojiMixURL/$date/u$mainCode/$fileName").also { bytes ->
                file.writeBytes(bytes)
            }
        }
    }.onFailure { DrawMeme.logger.error(it) }.getOrNull()
}

val supportedEmojis by lazy {
    runBlocking(DrawMeme.coroutineContext) {
        val emo = mutableMapOf<Int, Long>()
        val returnStr: String = HttpClient(OkHttp).use {
            it.get("https://tikolu.net/emojimix/emojis.js?v=2")
        }
        val regex = Regex("""\[\[(.+)], "(\d+)"]""")
        val finds = regex.findAll(returnStr)

        finds.forEach { result ->
            result.groupValues[1].split(",").forEach {
                emo[it.toInt()] = result.groupValues[2].toLong()
            }
        }
        emo
    }
}