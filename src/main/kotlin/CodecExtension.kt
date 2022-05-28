package org.laolittle.plugin.draw

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec
import org.jetbrains.skia.Image

operator fun Codec.get(frame: Int) {
    val bitmap = Bitmap()
    bitmap.allocPixels(imageInfo)
    readPixels(bitmap, frame)
}

fun Bitmap.asImage() = Image.makeFromBitmap(this)