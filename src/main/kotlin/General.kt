package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.*
import java.nio.file.Path
import kotlin.io.path.readBytes
import org.jetbrains.skia.Image as SkImage

internal val httpClient = HttpClient(OkHttp)
internal val logger by DrawMeme::logger

val json = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

internal fun String.splitSpace(): List<String>? {
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

internal fun SkImage.Companion.makeFromResource(name: String) = makeFromEncoded(
    DrawMeme::class.java.getResourceAsStream(name)?.readBytes()
        ?: throw IllegalStateException("无法找到资源文件: $name")
)

fun Rect.Companion.makeFromImage(image: SkImage) = Rect(0f, 0f, image.width.toFloat(), image.height.toFloat())

fun Path.toExternalResource(formatName: String? = null) = readBytes().toExternalResource(formatName)

internal suspend fun MessageEvent.getOrWaitImage(): ByteArray? {
    message.forEach { m ->
        when (m) {
            is Image -> return httpClient.get(m.queryUrl())
            is At -> return httpClient.get(m.avatarUrl)
            is QuoteReply -> m.source.originalMessage.firstIsInstanceOrNull<Image>()?.let { img ->
                return httpClient.get(img.queryUrl())
            }
        }
    }

    return kotlin.run {
        subject.sendMessage("请在30s内发送图片")
        nextMessageOrNull(30_000) { event -> event.message.contains(Image) }?.let { m ->
            httpClient.get(m.firstIsInstance<Image>().queryUrl())
        }
    } ?: kotlin.run {
        subject.sendMessage(PlainText("超时未发送").plus(message.quote()))
        null
    }
}

val At.avatarUrl: String get() = "https://q1.qlogo.cn/g?b=qq&nk=$target&s=640"

fun Bitmap.asImage() = SkImage.makeFromBitmap(this)

private val linearMipmap = FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST)
fun Canvas.drawImageRectLinear(
    image: org.jetbrains.skia.Image,
    src: Rect,
    dst: Rect,
    paint: Paint?,
    strict: Boolean
) = drawImageRect(image, src, dst, linearMipmap, paint, strict)

fun Canvas.drawImageRectLinear(image: SkImage, dst: Rect, paint: Paint?) =
    drawImageRectLinear(
        image,
        Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
        dst,
        paint,
        true
    )

fun Canvas.drawImageRectLinear(image: SkImage, dst: Rect) = drawImageRectLinear(image, dst, null)

suspend fun Image.getBytes(): ByteArray = httpClient.get(queryUrl())