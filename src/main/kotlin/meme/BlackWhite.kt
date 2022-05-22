package org.laolittle.plugin.draw.meme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.bytes
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.gif.GifEncoder
import org.laolittle.plugin.gif.GifSetting
import org.laolittle.plugin.usedBy
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readLines

private val bwFont = Fonts["MiSans-Bold"] usedBy "黑白图片"

private val bwData = DrawMeme.dataFolderPath.resolve("bw").also { it.createDirectories() }
internal val customFilter by lazy {
    val customFiles = bwData.listDirectoryEntries().filter { it.isRegularFile() }

    val customs = HashMap<String, ColorFilter>(customFiles.size)

    customFiles.forEach m@{
        val lines = it.readLines()
        val head = (lines.firstOrNull() ?: return@m).replace(" ", "")

        if (head.slice(0..3) != "meme") return@m
        val type = head.slice(5 until head.length)

        val name = lines.getOrNull(1) ?: return@m

        val filter = when (type) {
            "martix" -> {
                val matrix = lines.slice(2 until lines.size)
                    .joinToString(",")
                    .replace(Regex(",{2,}"), ",")
                    .replace(" ", "")
                    .replace("\n", "")

                val arr = FloatArray(20)

                println(matrix)

                run {
                    matrix.split(",").forEachIndexed { i, str ->
                        val num = str.toFloatOrNull() ?: run {
                            DrawMeme.logger.error("Parsing filter $name failed: Unknown number $str")
                            return@m
                        }
                        println(num)

                        arr[i] = num

                        println(i)
                        if (i == 19) return@run
                    }
                }

                ColorFilter.makeMatrix(ColorMatrix(*arr))
            }
            else -> {
                DrawMeme.logger.error("Unknown Type: $type at $it")
                return@m
            }
        }

        customs[name] = filter
    }

    customs
}

private val paintWhite = Paint().apply {
    color = Color.WHITE
}

private val paintDefault = Paint().apply {
    colorFilter = ColorFilter.makeMatrix(
        ColorMatrix(
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 1F, 0F,
        )
    )
}

private val blackPaint = Paint().apply { color = Color.BLACK }
suspend fun blackWhite(text: String, image: ByteArray, _filter: String): ByteArray {
    val codec = Codec.makeFromData(Data.makeFromBytes(image))

    val filter = customFilter[_filter]

    val h = codec.height
    val w = codec.width
    val blank = text.isBlank()
    val foo = h / 6

    val surface = Surface.makeRaster(ImageInfo(codec.colorInfo, w, h + if (blank) 0 else (foo * 1.4f).toInt()))

    val bwDraw = fun Surface.(bitmap: Bitmap) {
        canvas.apply {
            clear(Color.TRANSPARENT)
            drawImage(
                Image.makeFromBitmap(bitmap),
                0f,
                0f,
                if (null == filter) paintDefault else paintWhite.apply { colorFilter = filter })

            if (!blank) {
                val bar = foo / 1.4f
                val fontSize = if (bar.toInt() * text.length > w) ((w * 0.8f) / text.length) else bar
                val textLine = TextLine.make(text, bwFont.makeWithSize(fontSize))
                drawRect(Rect(0f, h.toFloat(), w.toFloat(), height.toFloat()), blackPaint)
                drawTextLine(
                    textLine,
                    ((width - textLine.width) / 2),
                    h + ((foo + textLine.height) / 2),
                    paintWhite
                )
            }
        }
    }

    codec.use {
        return if (codec.encodedImageFormat == EncodedImageFormat.GIF) {
            val bitmaps = Array(codec.frameCount) {
                //DrawMeme.async {   // multi-thread will make jvm crash
                Bitmap().apply {
                    allocPixels(codec.imageInfo)
                    codec.readPixels(this, it)
                }
                //}
            }

            val (collector, writer) = GifEncoder.new(
                GifSetting(
                    surface.width,
                    surface.height,
                    100,
                    false,
                    GifSetting.Repeat.Infinite
                )
            )

            val result = DrawMeme.async(Dispatchers.IO) {
                writer.writeToBytes()
            }

            var current = 0
            repeat(codec.frameCount) {
                surface.bwDraw(bitmaps[it])

                current += codec.getFrameInfo(it).duration
                collector.addFrame(surface.makeImageSnapshot().bytes, it, current / 1000.0)
            }

            collector.close()

            result.await()
        } else {
            val bitmap = codec.readPixels()
            surface.bwDraw(bitmap)

            surface.makeImageSnapshot().bytes
        }
    }
}