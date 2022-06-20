package org.laolittle.plugin.draw

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
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import net.mamoe.mirai.utils.info
import org.laolittle.plugin.draw.custom.initCustomMemes
import org.laolittle.plugin.draw.extension.Emoji.EmojiUtil.fullEmojiRegex
import org.laolittle.plugin.draw.extension.Emoji.EmojiUtil.toEmoji
import org.laolittle.plugin.draw.extension.findUserOrNull
import org.laolittle.plugin.draw.meme.*
import org.laolittle.plugin.sendImage
import org.laolittle.plugin.toExternalResource
import org.jetbrains.skia.Image as SkImage

object DrawMeme : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.draw.DrawMeme",
        name = "DrawMeme",
        version = "1.2.3",
    ) {
        author("LaoLittle")

        dependsOn("org.laolittle.plugin.SkikoMirai", ">=1.0.3", false)
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }

        initCustomMemes()

        val patReg = Regex("""^摸+([我爆头])?""")
        val choReg = Regex("#5(?:000|k)兆 *(.+)")
        val zeroReg = Regex("""#(\d{1,3})""")
        val erodeReg = Regex("""^#erode *(\d*) *(\d*)""")
        val emojiReg = Regex("""^($fullEmojiRegex) *($fullEmojiRegex)$""")
        val osuReg = Regex("""^#osu *(.*)""")

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
                val msg = str.replace("[图片]", "").replace("[动画表情]", "")

                val image = getOrWaitImage() ?: return@startsWith

                val sp = msg.split("--")
                val content = sp.first()
                val filter = sp.getOrElse(1) { "" }
                blackWhite(content.trim(), image, filter).toExternalResource().use {
                    subject.sendImage(it)
                }
            }

            /**
             * [5000choyen](https://github.com/yurafuca/5000choyen)
             * kotlin ver made by @cssxsh
             * @author yurafuca
             */
            finding(choReg) Five@{ result ->
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

                val (top, bottom) = words
                subject.sendImage(choyen(top, bottom))
            }

            // 零溢事件
            finding(zeroReg) { r ->
                if (message.firstIsInstance<PlainText>()
                        .content
                        .trim()
                        .replace("#", "")
                        .contains(Regex("""\D"""))
                ) return@finding // avoid #\dxxx

                val real = r.groupValues[1].toInt()

                if (real > 100) return@finding
                val image = getOrWaitImage() ?: return@finding
                val skikoImage = SkImage.makeFromEncoded(image)

                subject.sendImage(zero(skikoImage, real))
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
                    } ?: return@finding
                }

                patpat(image!!, delay).bytes.toExternalResource("GIF").use { subject.sendImage(it) }
            }

            /*startsWith("#ctl") {
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
            }*/

            finding(erodeReg) {
                val image = getOrWaitImage() ?: return@finding

                val rx = it.groupValues[1].toFloatOrNull() ?: 5f

                val ry = it.groupValues[2].toFloatOrNull() ?: 0f

                erode(image, rx, ry).toExternalResource().use { ex ->
                    subject.sendImage(ex)
                }
            }

            startsWith("#flash") {
                val image = getOrWaitImage() ?: return@startsWith

                subject.sendImage(flashImage(image))
            }

            startsWith("#marble") {
                val skImage = SkImage.makeFromEncoded(getOrWaitImage() ?: return@startsWith)

                val s = it.split(' ')
                fun getFloatOrNull(index: Int): Float? {
                    return s.getOrNull(index)?.toFloatOrNull()
                }

                val foo = skImage.width * .1f
                val x = getFloatOrNull(0) ?: foo
                val y = getFloatOrNull(1) ?: (foo * .1f)
                val i = getFloatOrNull(2) ?: 1f

                subject.sendImage(
                    marble(
                        skImage,
                        MarbleFilter(x, y, i)
                    ).use { bitmap -> SkImage.makeFromBitmap(bitmap) })
            }

            finding(emojiReg) {
                val first = it.groupValues[1].toEmoji()
                val second = it.groupValues[2].toEmoji()

                val file = getEmojiMix(first, second) ?: getEmojiMix(second, first) ?: return@finding
                file.toExternalResource("png").use { e -> subject.sendImage(e) }
            }

            finding(osuReg) {
                val content = it.groupValues[1]
                subject.sendImage(osu(content.ifBlank { "osu!" }))
            }
        }
    }
}