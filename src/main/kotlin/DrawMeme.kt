package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.EventPriority
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.draw.Emoji.EmojiUtil.fullEmojiRegex
import org.laolittle.plugin.draw.Emoji.EmojiUtil.toEmoji
import org.laolittle.plugin.draw.meme.*
import org.laolittle.plugin.draw.meme.Message
import org.laolittle.plugin.sendImage
import org.laolittle.plugin.toExternalResource
import org.laolittle.plugin.usedBy
import kotlin.math.min
import org.jetbrains.skia.Image as SkImage

object DrawMeme : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.draw.DrawMeme",
        name = "DrawMeme",
        version = "1.0.8",
    ) {
        author("LaoLittle")

        dependsOn("org.laolittle.plugin.SkikoMirai", ">=1.0.3", false)
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        val k5topFont = Fonts["Noto Sans SC-BOLD"] usedBy "5k兆顶部文字"
        val k5botFont = Fonts["Noto Serif SC-BOLD"] usedBy "5k兆底部文字"
        val x0font = Fonts["MiSans-Regular"] usedBy "0%生成器"
        val patReg = Regex("""^摸+([我爆头])?""")

        globalEventChannel().subscribeGroupMessages(
            priority = EventPriority.NORMAL
        ) {
            startsWith("#ph") { str ->
                val processed = message.firstIsInstanceOrNull<At>()?.let {
                    subject[it.target]?.nameCardOrNick?.let { card -> str.replace("@${it.target}", card) }
                } ?: str

                val words = processed.splitSpace() ?: return@startsWith

                pornHub(words[0], words[1]).makeImageSnapshot().toExternalResource().use { res ->
                    subject.sendImage(res)
                }
            }

            // finding(Regex("[\uD83D\uDE00-\uD83D\uDD67]\\+[\uD83D\uDE00-\uD83D\uDD67]")) {}
            startsWith("#bw") { str ->
                val msg = str.replace("[图片]", "").replace("[动画表情]", "").split("--")

                val content = msg.first()
                val filter = msg.getOrElse(1) { "" }

                val image = getOrWaitImage() ?: return@startsWith

                val bytes = HttpClient(OkHttp).use { client ->
                    client.get<ByteArray>(image.queryUrl())
                }

                blackWhite(content.trim(), bytes, filter).toExternalResource().use {
                    subject.sendImage(it)
                }
            }

            /**
             * [5000choyen](https://github.com/yurafuca/5000choyen)
             * kotlin ver made by @cssxsh
             * @author yurafuca
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

                val words = processed.splitSpace() ?: return@Five

                val topText = TextLine.make(words[0], k5topFont)
                val bottomText = TextLine.make(words[1], k5botFont)
                val width = maxOf(topText.width + 70, bottomText.width + 250).toInt()

                Surface.makeRasterN32Premul(width, 290).apply {
                    canvas.apply {
                        skew(-0.45F, 0F)

                        val topX = 70F
                        val topY = 100F
                        val paintTop = Paint().apply {
                            mode = PaintMode.STROKE
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
                            mode = PaintMode.STROKE
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

                    makeImageSnapshot().toExternalResource().use { subject.sendImage(it) }
                }
            }

            // 零溢事件
            finding(Regex("""#(\d{1,3})""")) { r ->
                if (message
                        .firstIsInstance<PlainText>()
                        .content
                        .trim()
                        .replace("#", "")
                        .contains(Regex("""\D"""))
                ) return@finding

                val real = r.groupValues[1].toInt()

                if (real > 100) return@finding
                val image = getOrWaitImage() ?: return@finding

                val skikoImage = HttpClient(OkHttp).use { client ->
                    SkImage.makeFromEncoded(client.get<ByteArray>(image.queryUrl()))
                }
                val w21 = (skikoImage.width shr 1).toFloat()
                val h21 = (skikoImage.height shr 1).toFloat()
                val radius = min(w21, h21) * .24f

                val text = TextLine.make("$real%", x0font.makeWithSize(radius * .6f))
                Surface.makeRaster(skikoImage.imageInfo).apply {
                    val paint = Paint().apply {
                        color = Color.WHITE
                    }
                    canvas.apply {
                        clear(Color.BLACK)
                        drawImage(skikoImage, 0F, 0F, paint.apply {
                            alpha = 155
                        })
                        drawCircle(w21, h21, radius, paint.apply {
                            alpha = 255
                            mode = PaintMode.STROKE
                            strokeWidth = radius * .19f
                            maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, radius * .2f)
                        })
                        drawTextLine(text, w21 - text.width / 2, h21 + text.height / 4, paint.apply {
                            mode = PaintMode.FILL
                            maskFilter = null
                        })
                    }

                    makeImageSnapshot().toExternalResource().use { subject.sendImage(it) }
                }
            }

            finding(patReg) { result ->
                val foo = result.groupValues[1]
                var delay = 0.05

                var image: SkImage? = null
                when (foo) {
                    "我" -> {
                        val bytes = httpClient.get<ByteArray>(sender.avatarUrl)
                        image = SkImage.makeFromEncoded(bytes)
                    }

                    "爆" -> delay = 0.02
                }

                for (single in message) {
                    if (null != image) break
                    when (single) {
                        is Image -> httpClient.get<ByteArray>(single.queryUrl()).apply {
                            image = SkImage.makeFromEncoded(this)
                        }

                        is At -> subject[single.target]?.let {
                            httpClient.get<ByteArray>(it.avatarUrl).apply {
                                image = SkImage.makeFromEncoded(this)
                            }
                        }
                    }
                }

                if (null == image) {
                    val name = message.content.replace(patReg, "")

                    subject.findUserOrNull(name)?.let {
                        httpClient.get<ByteArray>(it.avatarUrl).apply {
                            image = SkImage.makeFromEncoded(this)
                        }
                    } ?: kotlin.run {
                        subject.sendMessage("我不知道你要摸谁")
                        return@finding
                    }
                }

                patpat(image!!, delay).bytes.toExternalResource("GIF").use { subject.sendImage(it) }
            }

            startsWith("#ctl") {
                val forward = nextMessageOrNull(30_000) {
                    message.contains(ForwardMessage)
                }?.firstIsInstanceOrNull<ForwardMessage>() ?: return@startsWith

                var hito = mutableSetOf<Long>()
                val nick = forward.title.contains("群聊")

                val image = MessageImage()
                forward.nodeList.forEach { node ->
                    val messages = arrayListOf<Message>()
                    if (!hito.add(node.senderId)) {
                        // subject.sendMessage("人数过多")
                    }
                    node.messageChain.forEach {
                        when (it) {
                            is PlainText -> messages.add(
                                Message.Plain(
                                    ParagraphBuilder(
                                        paraStyle,
                                        GlobalParagraphMgr.fc
                                    ).apply {
                                        addText(it.content.replace("\\n", "\n"))
                                    }.build().layout(850f)
                                )
                            )
                            is Image -> messages.add(Message.Image(SkImage.makeFromEncoded(httpClient.get(it.queryUrl()))))
                        }
                    }

                    val imageNode = MessageImageNode(
                        if (nick) node.senderName else null,
                        "http://q1.qlogo.cn/g?b=qq&nk=${node.senderId}&s=640",
                        messages
                    )
                    image.add(imageNode)
                }

                subject.sendImage(image.makeImage())
            }

            finding(Regex("^#erode ?(\\d)? ?(\\d)?")) {
                val image = getOrWaitImage() ?: return@finding

                val rx = it.groupValues[1].toFloatOrNull() ?: 5f

                val ry = it.groupValues[2].toFloatOrNull() ?: 0f

                val sk = httpClient.get<ByteArray>(image.queryUrl())

                erode(sk, rx, ry).toExternalResource().use { ex ->
                    subject.sendImage(ex)
                }
            }

            finding(Regex("""^($fullEmojiRegex).*($fullEmojiRegex)$""")) {
                val first = it.groupValues[1].toEmoji()
                val second = it.groupValues[2].toEmoji()

                val file = getEmojiMix(first, second) ?: getEmojiMix(second, first) ?: return@finding
                file.toExternalResource("png").use { e -> subject.sendImage(e) }
            }
        }
    }
}

