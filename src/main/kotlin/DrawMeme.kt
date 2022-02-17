package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.draw.Emoji.EmojiUtil.toEmoji
import org.laolittle.plugin.toExternalResource
import java.io.InputStream
import net.mamoe.mirai.message.data.Image as MiraiImage

object DrawMeme : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.draw.DrawMeme",
        name = "DrawMeme",
        version = "1.0.2",
    ) {
        author("LaoLittle")

        dependsOn(
            PluginDependency("org.laolittle.plugin.SkikoMirai", ">=1.0.1", true)
        )
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        globalEventChannel().subscribeGroupMessages(
            priority = EventPriority.NORMAL
        ) {
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
                        is TimeoutCancellationException -> {
                            subject.sendMessage(PlainText("超时未发送").plus(message.quote()))
                            return@startsWith
                        }
                        else -> throw e
                    }
                }).firstIsInstanceOrNull<MiraiImage>()
                    ?: return@startsWith

                val paint = Paint().apply {
                    isAntiAlias = true
                }

                val skikoImage = HttpClient(OkHttp).use { client ->
                    client.get<InputStream>(image.queryUrl()).use { input ->
                        Image.makeFromEncoded(input.readBytes())
                    }
                }

                val h = skikoImage.height
                val w = skikoImage.width
                val foo = h / 6
                val bar = foo / 1.4f
                val fontSize = if (bar.toInt() * content.length > w) ((w * 0.8f) / content.length) else bar
                val text = TextLine.make(content, Fonts["MiSans-Bold.ttf", fontSize])

                Surface.makeRasterN32Premul(skikoImage.width, h + (foo * 1.4f).toInt()).apply {
                    canvas.apply {
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
                            ((width - text.width) / 2),
                            h + ((foo + text.height) / 2),
                            paint.apply { color = Color.WHITE })
                    }

                    toExternalResource().use { res -> subject.sendImage(res) }
                }
            }

            /**
             * [5000choyen](https://github.com/yurafuca/5000choyen)
             * @author cssxsh
             */
            finding(Regex("#5(?:000|k)兆[\\s　]*(.+)")) Five@{ result ->
                /*val processed = message.firstIsInstanceOrNull<At>()?.let {
                    subject[it.target]?.nameCardOrNick?.let { card -> result.groupValues[1].replace("@${it.target}", card) }
                } ?: result.groupValues[1]*/

                val processed = result.groupValues[1].let { str ->
                    message.firstIsInstanceOrNull<At>()?.let {
                        subject[it.target]?.nameCardOrNick?.let { card ->
                            str.replace(
                                "@${it.target}",
                                card
                            )
                        }
                    } ?: str
                }

                val words = processed.split() ?: return@Five

                val topText = TextLine.make(words[0], Fonts["Noto Sans SC", FontStyle.BOLD])
                val bottomText = TextLine.make(words[1], Fonts["Noto Serif SC", FontStyle.BOLD])
                val width = maxOf(topText.width + 70, bottomText.width + 250).toInt() + 10

                Surface.makeRasterN32Premul(width, 290).apply {
                    canvas.apply {
                        skew(-0.45F, 0F)

                        val topX = 70F
                        val topY = 100F
                        val paintTop = Paint().apply {
                            isAntiAlias = true
                            setStroke(true)
                            strokeCap = PaintStrokeCap.ROUND
                            strokeJoin = PaintStrokeJoin.ROUND
                        }
                        // 黒色
                        drawTextLine(topText, topX + 4, topY + 4, paintTop.apply {
                            shader = null
                            color = Color.makeRGB(0, 0, 0)
                            strokeWidth = 22F
                        })
                        // 銀色
                        drawTextLine(topText, topX + 4, topY + 4, paintTop.apply {
                            shader = Shader.makeLinearGradient(
                                0F, 24F, 0F, 122F, intArrayOf(
                                    Color.makeRGB(0, 15, 36),
                                    Color.makeRGB(255, 255, 255),
                                    Color.makeRGB(55, 58, 59),
                                    Color.makeRGB(55, 58, 59),
                                    Color.makeRGB(200, 200, 200),
                                    Color.makeRGB(55, 58, 59),
                                    Color.makeRGB(25, 20, 31),
                                    Color.makeRGB(240, 240, 240),
                                    Color.makeRGB(166, 175, 194),
                                    Color.makeRGB(50, 50, 50)
                                ), floatArrayOf(0.0F, 0.10F, 0.18F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                            )
                            strokeWidth = 20F
                        })
                        // 黒色
                        drawTextLine(topText, topX, topY, paintTop.apply {
                            shader = null
                            color = Color.makeRGB(0, 0, 0)
                            strokeWidth = 16F
                        })
                        // 金色
                        drawTextLine(topText, topX, topY, paintTop.apply {
                            shader = Shader.makeLinearGradient(
                                0F, 20F, 0F, 100F, intArrayOf(
                                    Color.makeRGB(253, 241, 0),
                                    Color.makeRGB(245, 253, 187),
                                    Color.makeRGB(255, 255, 255),
                                    Color.makeRGB(253, 219, 9),
                                    Color.makeRGB(127, 53, 0),
                                    Color.makeRGB(243, 196, 11),
                                ), floatArrayOf(0.0F, 0.25F, 0.4F, 0.75F, 0.9F, 1F)
                            )
                            strokeWidth = 10F
                        })
                        // 黒色
                        drawTextLine(topText, topX + 2, topY - 3, paintTop.apply {
                            shader = null
                            color = Color.makeRGB(0, 0, 0)
                            strokeWidth = 6F
                        })
                        // 白色
                        drawTextLine(topText, topX, topY - 3, paintTop.apply {
                            shader = null
                            color = Color.makeRGB(255, 255, 255)
                            strokeWidth = 6F
                        })
                        // 赤色
                        drawTextLine(topText, topX, topY - 3, paintTop.apply {
                            shader = Shader.makeLinearGradient(
                                0F, 20F, 0F, 100F, intArrayOf(
                                    Color.makeRGB(255, 100, 0),
                                    Color.makeRGB(123, 0, 0),
                                    Color.makeRGB(240, 0, 0),
                                    Color.makeRGB(5, 0, 0),
                                ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                            )
                            strokeWidth = 4F
                        })
                        // 赤色
                        drawTextLine(topText, topX, topY - 3, paintTop.setStroke(false).apply {
                            shader = Shader.makeLinearGradient(
                                0F, 20F, 0F, 100F, intArrayOf(
                                    Color.makeRGB(230, 0, 0),
                                    Color.makeRGB(123, 0, 0),
                                    Color.makeRGB(240, 0, 0),
                                    Color.makeRGB(5, 0, 0),
                                ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                            )
                        })


                        val bottomX = 250F
                        val bottomY = 230F
                        val paint = Paint().apply {
                            isAntiAlias = true
                            setStroke(true)
                            strokeCap = PaintStrokeCap.ROUND
                            strokeJoin = PaintStrokeJoin.ROUND
                        }

                        // 黒色
                        drawTextLine(bottomText, bottomX + 5, bottomY + 2, paint.apply {
                            shader = null
                            color = Color.makeRGB(0, 0, 0)
                            strokeWidth = 22F
                        })
                        // 銀色
                        drawTextLine(bottomText, bottomX + 5, bottomY + 2, paint.apply {
                            shader = Shader.makeLinearGradient(
                                0F, bottomY - 80, 0F, bottomY + 18, intArrayOf(
                                    Color.makeRGB(0, 15, 36),
                                    Color.makeRGB(250, 250, 250),
                                    Color.makeRGB(150, 150, 150),
                                    Color.makeRGB(55, 58, 59),
                                    Color.makeRGB(25, 20, 31),
                                    Color.makeRGB(240, 240, 240),
                                    Color.makeRGB(166, 175, 194),
                                    Color.makeRGB(50, 50, 50)
                                ), floatArrayOf(0.0F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                            )
                            strokeWidth = 19F
                        })
                        // 黒色
                        drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                            shader = null
                            color = Color.makeRGB(16, 25, 58)
                            strokeWidth = 17F
                        })
                        // 白色
                        drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                            shader = null
                            color = Color.makeRGB(221, 221, 221)
                            strokeWidth = 8F
                        })
                        // 紺色
                        drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                            shader = Shader.makeLinearGradient(
                                0F, bottomY - 80, 0F, bottomY, intArrayOf(
                                    Color.makeRGB(16, 25, 58),
                                    Color.makeRGB(255, 255, 255),
                                    Color.makeRGB(16, 25, 58),
                                    Color.makeRGB(16, 25, 58),
                                    Color.makeRGB(16, 25, 58),
                                ), floatArrayOf(0.0F, 0.03F, 0.08F, 0.2F, 1F)
                            )
                            strokeWidth = 7F
                        })
                        // 銀色
                        drawTextLine(bottomText, bottomX, bottomY - 3, paint.setStroke(false).apply {
                            shader = Shader.makeLinearGradient(
                                0F, bottomY - 80, 0F, bottomY, intArrayOf(
                                    Color.makeRGB(245, 246, 248),
                                    Color.makeRGB(255, 255, 255),
                                    Color.makeRGB(195, 213, 220),
                                    Color.makeRGB(160, 190, 201),
                                    Color.makeRGB(160, 190, 201),
                                    Color.makeRGB(196, 215, 222),
                                    Color.makeRGB(255, 255, 255)
                                ), floatArrayOf(0.0F, 0.15F, 0.35F, 0.5F, 0.51F, 0.52F, 1F)
                            )
                            strokeWidth = 19F
                        })
                    }

                    toExternalResource().use { subject.sendImage(it) }
                }
            }

            finding(Regex("""^([\ud83c\udd00-\ud83e\udfff]).*([\ud83c\udd00-\ud83e\udfff])""")) {
                val emojiMix = "https://www.gstatic.com/android/keyboard/emojikitchen"
                val getEmoji: suspend (Emoji, Emoji) -> ByteArray? = Here@{ main: Emoji, aux: Emoji ->
                    val mainCode = main.code.toString(16)
                    val auxCode = aux.code.toString(16)
                    val date = Emoji.supportedEmojis[main.code] ?: return@Here null

                    val fileName = "u${mainCode}_u${auxCode}.png"
                    val file = emojiMixFolder
                        .resolve(fileName)
                    val giaFile = emojiMixFolder.resolve("u${auxCode}_u${mainCode}.png")

                    return@Here kotlin.runCatching {
                        if (file.isFile) file.readBytes()
                        else if (giaFile.isFile) giaFile.readBytes()
                        else HttpClient(OkHttp).use { client ->
                            client.get<ByteArray>("$emojiMix/$date/u$mainCode/$fileName").also { bytes ->
                                file.writeBytes(bytes)
                            }
                        }
                    }.getOrNull()
                }

                val first = it.groupValues[1].toEmoji()
                val second = it.groupValues[2].toEmoji()

                launch {
                    val bytes = kotlin.runCatching {
                        getEmoji(first, second) ?: getEmoji(second,first)
                    }.getOrNull() ?: return@launch

                    bytes.toExternalResource("png").use { e -> subject.sendImage(e) }
                }
            }
        }
    }
}