package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.usedBy

private val bwFont = Fonts["MiSans-Bold"] usedBy "黑白图片"
private val paint = Paint().apply {
    color = Color.WHITE
    colorFilter = ColorFilter.makeMatrix(
        ColorMatrix(
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 0F, 0F,
            0.33F, 0.38F, 0.29F, 1F, 0F,
        )
    )
}

fun blackWhite(text: String, image: Image): Surface {
    val h = image.height
    val w = image.width
    val blank = text.isBlank()
    val foo = h / 6

    return Surface.makeRasterN32Premul(w, h + if (blank) 0 else (foo * 1.4f).toInt()).apply {
        canvas.apply {
            clear(Color.BLACK)
            drawImage(image, 0F, 0F, paint)

            if (!blank) {
                val bar = foo / 1.4f
                val fontSize = if (bar.toInt() * text.length > w) ((w * 0.8f) / text.length) else bar
                val textLine = TextLine.make(text, bwFont.makeWithSize(fontSize))
                drawTextLine(
                    textLine,
                    ((width - textLine.width) / 2),
                    h + ((foo + textLine.height) / 2),
                    paint
                )
            }
        }
    }
}