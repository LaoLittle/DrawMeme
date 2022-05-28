package org.laolittle.plugin.draw

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Codec

operator fun Codec.get(frame: Int) {
    val bitmap = Bitmap()
    bitmap.allocPixels(imageInfo)
    readPixels(bitmap, frame)
}