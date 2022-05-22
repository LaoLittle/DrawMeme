package org.laolittle.plugin.draw.meme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.skia.*
import org.laolittle.plugin.bytes
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.gif.GifEncoder
import org.laolittle.plugin.gif.GifSetting

suspend fun erode(image: ByteArray, rx: Float, ry: Float): ByteArray {
    val codec = Codec.makeFromData(Data.makeFromBytes(image))

    val surface = Surface.makeRaster(codec.imageInfo)

    val paint = Paint().apply { imageFilter = ImageFilter.makeErode(rx, ry, null, null) }
    fun Canvas.makeErode(bitmap: Bitmap) {
        clear(Color.TRANSPARENT)
        drawImage(Image.makeFromBitmap(bitmap), 0f, 0f, paint)
    }

    codec.use {
        return if (codec.encodedImageFormat == EncodedImageFormat.GIF) {
            val bitmaps = Array(codec.frameCount) {
                Bitmap().apply {
                    allocPixels(codec.imageInfo)
                    codec.readPixels(this, it)
                }
            }

            val (collector, writer) = GifEncoder.new(
                GifSetting(
                    surface.width,
                    surface.height,
                    100,
                    false,
                    GifSetting.Repeat.Finite(codec.repetitionCount.toShort())
                )
            )

            val result = DrawMeme.async(Dispatchers.IO) {
                writer.writeToBytes()
            }

            var current = 0
            repeat(codec.frameCount) {
                surface.canvas.makeErode(bitmaps[it])

                current += codec.getFrameInfo(it).duration
                collector.addFrame(surface.makeImageSnapshot().bytes, it, current / 1000.0)
            }

            collector.close()

            result.await()
        } else {
            val bitmap = codec.readPixels()

            surface.canvas.makeErode(bitmap)
            surface.makeImageSnapshot().bytes
        }
    }
}