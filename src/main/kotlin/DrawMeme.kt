package org.laolittle.plugin.draw

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstance
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.toExternalResource
import java.net.URL
import net.mamoe.mirai.message.data.Image as MiraiImage

object DrawMeme : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.draw.DrawMeme",
        name = "DrawMeme",
        version = "1.0.1",
    ) {
        author("LaoLittle")

        dependsOn(
            PluginDependency("org.laolittle.plugin.SkikoMirai", ">=1.0", true)
        )
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeGroupMessages {
            startsWith("#ph") { str ->
                val processed = message.firstIsInstanceOrNull<At>()?.let {
                    subject[it.target]?.nameCardOrNick?.let { card -> str.replace("@${it.target}", card) }
                } ?: str

                val words = processed.split() ?: return@startsWith

                val phHeight = 170
                val widthPlus = 12

                val leftText = TextLine.make(words[0], MiSansBold88)
                val leftPorn = Surface.makeRasterN32Premul(leftText.width.toInt() + (widthPlus shl 1), phHeight)

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                leftPorn.canvas.apply {
                    clear(Color.makeARGB(255, 0, 0, 0))
                    drawTextLine(
                        leftText,
                        (leftPorn.width - leftText.width) / 2 + 5,
                        ((leftPorn.height shr 1) + (leftText.height / 4)),
                        paint.apply { color = Color.makeARGB(255, 255, 255, 255) }
                    )
                }

                val rightText = TextLine.make(words[1], MiSansBold88)
                val rightPorn = Surface.makeRasterN32Premul(
                    rightText.width.toInt() + (widthPlus shl 1) + 20,
                    rightText.height.toInt()
                )

                rightPorn.canvas.apply {
                    val rRect = RRect.makeComplexXYWH(
                        ((rightPorn.width - rightText.width) / 2) - widthPlus,
                        0F,
                        rightText.width + widthPlus,
                        rightText.height - 1,
                        floatArrayOf(19.5F)
                    )
                    drawRRect(
                        rRect, paint.apply { color = Color.makeARGB(255, 255, 145, 0) }
                    )
                    // clear(Color.makeARGB(255, 255,144,0))
                    // drawCircle(100F, 100F, 50F, Paint().apply { color = Color.BLUE })
                    drawTextLine(
                        rightText,
                        ((rightPorn.width - rightText.width - widthPlus.toFloat()) / 2),
                        ((rightPorn.height shr 1) + (rightText.height / 4) + 2),
                        paint.apply { color = Color.makeARGB(255, 0, 0, 0) }
                    )
                }

                Surface.makeRasterN32Premul(leftPorn.width + rightPorn.width, phHeight).apply {
                    canvas.apply {
                        clear(Color.makeARGB(255, 0, 0, 0))
                        drawImage(leftPorn.makeImageSnapshot(), 0F, 0F)
                        drawImage(
                            rightPorn.makeImageSnapshot(),
                            leftPorn.width.toFloat() - (widthPlus shr 1),
                            (((phHeight - rightPorn.height) shr 1) - 2).toFloat()
                        )
                    }
                    toExternalResource().use { res ->
                        subject.sendImage(res)
                    }
                }
            }

            // finding(Regex("[\uD83D\uDE00-\uD83D\uDD67]\\+[\uD83D\uDE00-\uD83D\uDD67]")) {}

            startsWith("#bw") { str ->
                val content = str.replace("[图片]", "").replace("[动画表情]", "")

                if (content.isBlank()) {
                    subject.sendMessage("发送#bw 文字 图片来生成")
                    return@startsWith
                }

                val image = (message.takeIf { m -> m.contains(MiraiImage) } ?: runCatching {
                    subject.sendMessage("请在30s内发送图片")
                    nextMessage(30_000) { event -> event.message.contains(MiraiImage) }
                }.getOrElse { e ->
                    when (e) {
                        is TimeoutCancellationException -> return@startsWith
                        else -> throw e
                    }
                }).runCatching {
                    firstIsInstance<MiraiImage>()
                }.onFailure { subject.sendMessage(PlainText("超时未发送").plus(message.quote())) }.getOrNull()
                    ?: return@startsWith

                val skikoImage = withContext(Dispatchers.IO) {
                    URL(image.queryUrl()).openStream().use { input ->
                        requireNotNull(input)
                        Image.makeFromEncoded(input.readBytes())
                    }
                }

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                val h = skikoImage.height
                val w = skikoImage.width
                val foo = h / 6
                val bar = foo / (1.4f)
                val fontSize = if (bar.toInt() * content.length > w) ((w * 0.8f) / content.length) else bar
                val text = TextLine.make(content, Fonts["MiSans-Bold.ttf", fontSize])

                val surface = Surface.makeRasterN32Premul(skikoImage.width, h + (foo * 1.4f).toInt())
                skikoImage.imageInfo

                surface.canvas.apply {
                    clear(Color.BLACK)
                    drawImage(skikoImage, 0F, 0F, paint.apply {
                        colorFilter = ColorFilter.makeMatrix(
                            ColorMatrix(
                                0.33F, 0.38F, 0.29F, 0F, 0F,
                                0.33F, 0.38F, 0.29F, 0F, 0F,
                                0.33F, 0.38F, 0.29F, 0F, 0F,
                                0.33F, 0.38F, 0.29F, 1F, 0F,
                            )
                        )
                    })

                    drawTextLine(text,
                        ((surface.width - text.width) / 2),
                        h + ((foo + text.height) / 2),
                        paint.apply { color = Color.WHITE })
                }

                surface.toExternalResource().use { res -> subject.sendImage(res) }

            }

            /*(startsWith("5000兆") or startsWith("5k兆")) Five@{ str ->
                val processed = message.firstIsInstanceOrNull<At>()?.let {
                    subject[it.target]?.nameCardOrNick?.let { card -> str.replace("@${it.target}", card) }
                } ?: str

                val words = processed.split() ?: return@Five

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                val topText = TextLine.make("Text", Fonts["", 88F])
            }*/
        }
    }
}