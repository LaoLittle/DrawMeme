package org.laolittle.plugin.draw.meme

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.contact.nameCardOrNick
import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.draw.extension.GlobalParagraphMgr
import org.laolittle.plugin.draw.httpClient
import org.laolittle.plugin.draw.makeFromImage
import org.laolittle.plugin.usedBy
import org.jetbrains.skia.Image as SkImage

private val msgFont = Fonts["MiSans-Regular", 35f] usedBy "消息图片"

val paraStyle = ParagraphStyle().apply {
    textStyle = TextStyle().apply {
        fontSize = 55f
        color = Color.WHITE
        typeface = msgFont.typefaceOrDefault
    }
}

private const val AVATAR_SIZE = 120f
private const val PADDING = 30f

class MessageImageNode(
    val nick: String? = null,
    val avatarUrl: String,
    val messages: List<Message>,
    avatar: SkImage? = null,
    private val side: Side = Side.Left
) {
    constructor(user: User, message: List<Message>) : this(
        if (user is Friend) null else user.nameCardOrNick,
        user.avatarUrl,
        message
    )

    private val _avatarJob: Job = DrawMeme.launch(DrawMeme.coroutineContext) {
        initAvatar()
    }

    private var _avatar: SkImage? = avatar
    private val avatar: SkImage
            by lazy {
                if (null == _avatar)
                // second try
                    runBlocking(DrawMeme.coroutineContext) {
                        _avatarJob.join()
                        initAvatar()
                    }

                _avatar!!
            }

    val height: Float by lazy {
        var h = 0f
        val first = messages.first()
        if (messages.size == 1 && first is Message.Image) {
            return@lazy first.image.height.toFloat() + if (null == nick) 0 else 40
        }

        messages.forEach {
            when (it) {
                is Message.Image -> {
                    h += it.image.height + 10
                }

                is Message.Plain -> {
                    h += it.content.height
                }
            }
        }

        h
    }

    val heightBox = height - if (null == nick) 0 else 40

    val width by lazy {
        var hMax = 0f
        val first = messages.first()
        if (messages.size == 1 && first is Message.Image) {
            hMax = first.image.width.toFloat()
        } else {
            messages.forEach {
                hMax = maxOf(hMax, it.width)
            }
        }

        hMax + 50 + AVATAR_SIZE + 30
    }

    val widthBox = width - 50 - AVATAR_SIZE - 30

    fun drawTo(canvas: Canvas) {
        canvas.apply {
            save()
            translate(50f, 0f)

            val halfSize = AVATAR_SIZE / 2
            drawCircle(halfSize, halfSize, halfSize, Paint())
            drawImageRect(
                avatar,
                Rect.makeFromImage(avatar),
                Rect(0f, 0f, AVATAR_SIZE, AVATAR_SIZE),
                FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                Paint().apply { blendMode = BlendMode.SRC_ATOP },
                true
            )

            translate(AVATAR_SIZE, 0f)
            translate(30f, 0f)

            if (null != nick) {
                val nickText = TextLine.make(nick, msgFont)

                drawTextLine(nickText, 5f, 25f, Paint().apply { color = Color.MAGENTA })
                translate(0f, 40f)
            }

            val first = messages.first()
            if (messages.size == 1 && first is Message.Image) {
                var h = first.image.height.toFloat()
                val w = (if (first.image.width > 700) {
                    h = first.image.height * (700f / first.image.height)
                    700f
                } else first.image.width).toFloat()

                drawImageRect(first.image, Rect.makeFromImage(first.image), Rect(0f, 0f, w, h))
            } else {
                val rad = 20f

                drawCircle(0f, AVATAR_SIZE / 5, rad, Paint())
                drawCircle(-rad / 2 + 2, 5f, 20f, Paint().apply {
                    color = 0
                    blendMode = BlendMode.SRC
                })

                drawRRect(RRect.makeLTRB(0f, 0f, widthBox + PADDING * 2, heightBox + PADDING * 2, 33f), Paint())

                translate(PADDING, PADDING - 10)
                messages.forEach { m ->
                    when (m) {
                        is Message.Image -> {
                            translate(0f, 5f)
                            var h = m.image.height.toFloat()
                            val w = (if (m.image.width > 700) {
                                h = m.image.height * (700f / m.image.height)
                                700f
                            } else m.image.width).toFloat()

                            drawImageRect(m.image, Rect.makeFromImage(m.image), Rect(0f, 0f, w, h))
                            translate(0f, 5f)
                        }

                        is Message.Plain -> {
                            m.content.paint(this, 0f, -10f)
                        }
                    }
                }
            }

            restore()
        }
    }

    private suspend fun initAvatar() {
        if (null == _avatar) {
            val bytes = httpClient.get(avatarUrl).body<ByteArray>()
            _avatar = SkImage.makeFromEncoded(bytes)
        }
    }

    init {
        require(messages.isNotEmpty())
        ParagraphBuilder(paraStyle, GlobalParagraphMgr.fc).apply {
            addText("瓦瓦瓦瓦")
        }.build().layout(850f)
    }

    enum class Side {
        Left,
        Right
    }
}

class MessageImage : MutableList<MessageImageNode> by mutableListOf() {
    fun makeImage(): SkImage {
        var height = 0f

        forEach {
            height += it.height + 100
        }

        return Surface.makeRasterN32Premul(1170, height.toInt()).apply {
            canvas.apply {
                translate(0f, 40f)
                forEach {
                    it.drawTo(this)
                    translate(0f, it.height + 100)
                }
            }
        }.makeImageSnapshot()
    }
}

sealed interface Message {
    val width: Float

    class Plain(val content: Paragraph) : Message {
        override val width: Float
            get() = content.longestLine
    }

    class Image(val image: SkImage) : Message {
        override val width: Float
            get() = image.width.toFloat()
    }
}