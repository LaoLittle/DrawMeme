package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Rect

fun Canvas.bar(block: Canvas.() -> Unit) {
    save()
    block()
    restore()
}

fun Rect.copy(
    left: Float = this.left,
    top: Float = this.top,
    right: Float = this.right,
    bottom: Float = this.bottom
) = Rect(left, top, right, bottom)

fun Bitmap.mosaic(size: Int) {
    val w = width
    val h = height

    for (y in 0 until h step size) for (x in 0 until w step size) {
        val color = getColor(x,y)
        erase(color, IRect.makeXYWH(x,y,size,size))
    }
}