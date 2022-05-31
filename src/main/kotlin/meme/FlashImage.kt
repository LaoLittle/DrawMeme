package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import kotlin.math.min

fun flashImage(image: ByteArray): Image {
    val input = Image.makeFromEncoded(image)
    val b = Bitmap.makeFromImage(input)
    b.mosaic(30)

    val w = input.width
    val h = input.height

    val foo = min(w, h) * .30f
    val bar = foo * .5f
    return Surface.makeRasterN32Premul(w,h).apply {
        writePixels(b, 0, 0)
        canvas.apply {
            drawPaint(Paint().apply {
                alpha = 160
            })

            translate(w * .5f - bar, h * .5f - bar)

            drawPath(Path().apply {
                val x1 = foo *.65f

                val p1 = Point(bar*.85f, bar*.85f)
                val p4 = Point(bar*1.15f, bar*1.15f)
                val p2 = Point(p4.x,p1.y)
                val p3 = Point(p1.x,p4.y)
                moveTo(x1, 0f)
                lineTo(foo * .2f, p3.y)
                lineTo(p3)
                moveTo(foo - x1, foo)
                lineTo(foo * .8f, p1.y)
                lineTo(p2)
                moveTo(p2)
                lineTo(p1)
                lineTo(p3)
                moveTo(p3)
                lineTo(p4)
                lineTo(p2)
            }, Paint().apply {
                color = Color.WHITE
                alpha = 160
                pathEffect = PathEffect.makeCorner(foo / 10)
            })
        }
    }.makeImageSnapshot()
}