package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.Canvas
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