package org.laolittle.plugin.draw.custom

import kotlinx.coroutines.async
import org.jetbrains.skia.*
import org.laolittle.plugin.bytes
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.draw.drawImageRectLinear
import org.laolittle.plugin.gif.GifEncoder
import org.laolittle.plugin.gif.GifSetting
import java.io.File

private val paintDefault = Paint()
class CustomMeme(
    val name: String,
    val input: Codec,
) {
    private val surface = Surface.makeRaster(input.imageInfo)

    val isGif = input.frameCount > 1
    private val frames = kotlin.run {
        if (!isGif) return@run arrayOf(input.readPixels())

        Array(input.frameCount) {
            Bitmap().apply {
                allocPixels(input.imageInfo)
                input.readPixels(this, it)
            }
        }
    }

    private val _actions = ArrayList<MutableList<Canvas.(Array<out Image>) -> Unit>>(input.frameCount)
    val actions: List<List<Canvas.(Array<out Image>) -> Unit>> get() = _actions

    fun add(frame: Int, block: Canvas.(Array<out Image>) -> Unit) {
        _actions[frame].add(block)
    }

    suspend fun makeImage(vararg images: Image): ByteArray {
        if (!isGif) {
            surface.canvas.clear(0)
            surface.writePixels(frames[0], 0, 0)
            actions.first().forEach {
                surface.canvas.it(images)
            }
            return surface.makeImageSnapshot().bytes
        }

        val (collector, writer) = GifEncoder.new(
            GifSetting(
                surface.width,
                surface.height,
                100,
                false,
                GifSetting.Repeat.Finite(input.repetitionCount.toShort())
            )
        )

        val bytes = DrawMeme.async {
            writer.writeToBytes()
        }

        var current = 0
        actions.forEachIndexed { index, each ->
            surface.canvas.clear(0)
            surface.writePixels(frames[index], 0, 0)
            each.forEach {
                surface.canvas.it(images)
            }
            val info = input.getFrameInfo(index)

            current += info.duration
            collector.addFrame(surface.makeImageSnapshot().bytes, index, info.duration / 1000.0)
        }

        return bytes.await()
    }

    companion object {
        fun fromFile(file: File): CustomMeme {
            /*fun String.withIndexOf(string: String, startIndex: Int = 0, ignoreCase: Boolean = false, block: (Int) -> Unit): Boolean {
                val index = indexOf(string,startIndex, ignoreCase)

                return index == 0
            }*/

            val text = ArrayList<String>(3)

            val reg = Regex("""//.*""")

            file.readLines().forEach {
                if (it.isNotBlank()) {
                    val foo = it.replace(reg, "")
                    if (foo.isNotBlank()) text.add(foo.replace('，', ',').replace('：', ':'))
                }
            }

            var name: String? = null
            var input: Codec? = null
            val avatarVal = hashMapOf<String, Int>()
            for (i in 0..2) {
                val line = text[i]
                when {
                    null == name && line.startsWith("meme:") -> {
                        val foo = line.replace(" ", "")
                        name = foo.slice(5..foo.lastIndex)
                    }

                    null == input && line.startsWith("input:") -> {
                        val foo = line.indexOf('{')
                        val bar = line.indexOf('}')
                        val path = line.slice(foo + 1 until bar)

                        input = Codec.makeFromData(Data.makeFromFileName(file.absoluteFile.parentFile.resolve(path).absolutePath))
                    }

                    avatarVal.isEmpty() && line.startsWith("avatars:") -> {
                        val foo = line
                            .replace(" ", "")


                        foo.slice(8..foo.lastIndex)
                            .split(',')
                            .forEachIndexed { index, s ->
                            if (!s.startsWith('@')) throw IllegalArgumentException("")
                            avatarVal[s.slice(1..s.lastIndex)] = index
                        }
                    }
                }
            }

            requireNotNull(name)
            requireNotNull(input)

            val customMeme = CustomMeme(name, input)

            fun compile(input: String): Canvas.(Array<out Image>) -> Unit {
                val split = input.split(' ', limit = 3)

                return when (split.first()) {
                    "draw" -> {
                        val index = avatarVal[split[1]] ?: throw IllegalArgumentException("Compiler error: No such argument: ${split[1]}")

                        val shape = split[2]

                        val foo = shape.indexOf('{')
                        val bar = shape.indexOf('}')
                        val cons = shape.slice(foo + 1 until bar).split(',')

                        when {
                            shape.startsWith("Rect", true) -> {
                                val rect =
                                    Rect(cons[0].toFloat(), cons[1].toFloat(), cons[2].toFloat(), cons[3].toFloat());

                                {
                                    val img = it[index]

                                    drawImageRectLinear(img, rect)
                                }
                            }

                            shape.startsWith("Circle", true) -> {
                                val x = cons[0].toFloat()
                                val y = cons[1].toFloat()
                                val radius = cons[2].toFloat();

                                {
                                    val img = it[index]

                                    // drawCircle(x, y, radius, paintTransparent) // 扫清障碍
                                    clipRRect(RRect.makeLTRB(x - radius, y - radius, x + radius, y + radius, radius), true)
                                    drawImageRectLinear(
                                        img,
                                        Rect(x - radius, y - radius, x + radius, y + radius),
                                        paintDefault
                                    )
                                }
                            }

                            else -> {
                                throw IllegalArgumentException(input)
                            }
                        }
                    }

                    else -> {
                        throw IllegalArgumentException(input)
                    }
                }
            }


            var currentFrame = -1

            for (i in 3..text.lastIndex) {
                val line = text[i]
                if (line.startsWith("frame")) {
                    currentFrame += 1
                    continue
                }

                customMeme.add(currentFrame, compile(line))
            }

            return customMeme
        }
    }

    init {
        repeat(input.frameCount) {
            _actions.add(mutableListOf())
        }
    }
}