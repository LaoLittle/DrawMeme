package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.draw.makeFromResource
import org.laolittle.plugin.getBytes
import org.laolittle.plugin.gif.GifImage
import org.laolittle.plugin.gif.GifSetting
import org.laolittle.plugin.gif.buildGifImage

private const val width = 320
private const val height = 320
suspend fun patpat(image: Image, delay: Double = .05): GifImage {
    return buildGifImage(GifSetting(width, height, 100, true, GifSetting.Repeat.Infinite)) {
        addFrame(pat(Rect(40f ,40f, 300f, 300f), Point(0f, 0f), image, 0).getBytes(), delay)
        addFrame(pat(Rect(40f ,70f, 300f, 300f), Point(0f, 0f), image, 1).getBytes(), delay)
        addFrame(pat(Rect(33f ,105f, 300f, 300f), Point(0f, 0f), image, 2).getBytes(), delay)
        addFrame(pat(Rect(37f ,90f, 300f, 300f), Point(0f, 0f), image, 3).getBytes(), delay)
        addFrame(pat(Rect(40f ,65f, 300f, 300f), Point(0f, 0f), image, 4).getBytes(), delay)
    }
}

private val whitePaint = Paint().apply { color = Color.WHITE }
private val srcInPaint = Paint().apply { blendMode = BlendMode.SRC_IN }
private val hands = Array(5) { Image.makeFromResource("/PatPat/img$it.png") }

private const val imgW = width.toFloat()
private const val imgH = height.toFloat()
 fun pat(imgDst: Rect, handPoint: Point, image: Image, no: Int): Image {
    val hand = hands[no]

    return Surface.makeRasterN32Premul(width, height).apply {

        canvas.apply {
            bar {
                val radius = (width shr 1).toFloat()
                translate(imgDst.left, imgDst.top)
                scale(imgDst.width / width, imgDst.height / height)
                drawCircle(imgW * .5f, imgH * .5f, radius, whitePaint)
                drawImageRect(
                    image,
                    Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
                    Rect.makeWH(imgW, imgH),
                    FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
                    srcInPaint,
                    true
                )
            }

            drawImageRect(
                hand,
                Rect.makeWH(hand.width.toFloat(), hand.height.toFloat()),
                Rect(handPoint.x, handPoint.y, handPoint.x + width, handPoint.y + height),
                SamplingMode.CATMULL_ROM,
                null,
                true
            )
            //drawImageRect(hand, Rect(handPoint.x, handPoint.y, handPoint.x + width, handPoint.y + height))
            //makeImageSnapshot().getBytes().also { File("out$no.png").writeBytes(it) }
        }
    }.makeImageSnapshot()
}