package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.TimeoutCancellationException
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.*
import java.nio.file.Path
import kotlin.io.path.readBytes
import org.jetbrains.skia.Image as SkImage

internal val httpClient = HttpClient(OkHttp)
internal val logger by DrawMeme::logger

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
    DrawMeme::class.java.getResourceAsStream(name)?.readBytes() ?: throw IllegalStateException("无法找到资源文件: $name")
)

fun Rect.Companion.makeFromImage(image: SkImage) = Rect(0f, 0f, image.width.toFloat(), image.height.toFloat())

fun Path.toExternalResource(formatName: String? = null) = readBytes().toExternalResource(formatName)

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