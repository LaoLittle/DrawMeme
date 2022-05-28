@file:JvmName("Zero")

package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.usedBy
import kotlin.math.min

val x0font = Fonts["MiSans-Regular"] usedBy "0%生成器"
fun zero(image: Image, num: Int): Image {
    val w21 = (image.width shr 1).toFloat()
    val h21 = (image.height shr 1).toFloat()
    val radius = min(w21, h21) * .24f

    val text = TextLine.make("$num%", x0font.makeWithSize(radius * .6f))
    return Surface.makeRaster(image.imageInfo).apply {
        val paint = Paint().apply {
            color = Color.WHITE
        }
        canvas.apply {
            clear(Color.BLACK)
            drawImage(image, 0F, 0F, paint.apply {
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
    }.makeImageSnapshot()
}