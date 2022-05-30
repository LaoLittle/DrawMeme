package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.IRect
import org.jetbrains.skia.Image
import org.laolittle.plugin.draw.math.Noise
import org.laolittle.plugin.draw.math.bilinearInterpolate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

fun marble(image: Image, marble: MarbleFilter = MarbleFilter()): Bitmap {
    //val surface = Surface.makeRaster(image.imageInfo)
    val bitmap = Bitmap.makeFromImage(image)

    val dst = Bitmap().apply {
        allocPixels(image.imageInfo)
    }

    val h = bitmap.height
    val w = bitmap.width

    val h1 = h - 1
    val w1 = w - 1

    val out = FloatArray(2)

    for (y in 0 until h) for (x in 0 until w) {
        marble.transformInverse(x, y, out)
        val srcX = floor(out[0]).toInt()
        val srcY = floor(out[1]).toInt()

        val xWeight = out[0] - srcX
        val yWeight = out[1] - srcY

        val nw: Int
        val ne: Int
        val sw: Int
        val se: Int

        if (srcX in 0 until w1 && srcY in 0 until h1) {
            nw = bitmap.getColor(srcX, srcY)
            ne = bitmap.getColor(srcX + 1, srcY)
            sw = bitmap.getColor(srcX, srcY + 1)
            se = bitmap.getColor(srcX + 1, srcY + 1)
        } else {
            nw = bitmap.pixel(srcX, srcY, w, h)
            ne = bitmap.pixel(srcX + 1, srcY, w, h)
            sw = bitmap.pixel(srcX, srcY + 1, w, h)
            se = bitmap.pixel(srcX + 1, srcY + 1, w, h)
        }

        val pixel = bilinearInterpolate(xWeight, yWeight, nw, ne, sw, se)

        dst.erase(pixel, IRect.makeXYWH(x, y, 1, 1))
    }

    return dst
}

private fun Bitmap.pixel(x: Int, y: Int, w: Int, h: Int): Int {
    val clampX = x.coerceAtLeast(0).coerceAtMost(w - 1)
    val clampY = y.coerceAtLeast(0).coerceAtMost(h - 1)

    return getColor(clampX, clampY)
}

private const val TWO_PI = PI * 2

class MarbleFilter(
    private val xScale: Float = 4f,
    private val yScale: Float = 4f,
    private val turbulence: Float = 1f
) {
    private val sinTable = FloatArray(256)
    private val cosTable = FloatArray(256)

    private fun displacementMap(x: Int, y: Int): Int {
        return clamp((127 * (1 + Noise.noise2(x / xScale, y / xScale))).toInt())
    }

    fun transformInverse(x: Int, y: Int, out: FloatArray) {
        val displacement = displacementMap(x, y)
        out[0] = x + sinTable[displacement]
        out[1] = y + cosTable[displacement]
    }

    init {
        repeat(256) {
            val angle = (TWO_PI * it / 256f) * turbulence
            sinTable[it] = (-yScale * sin(angle)).toFloat()
            cosTable[it] = (yScale * cos(angle)).toFloat()
        }
    }

    companion object {
        fun clamp(value: Int) = value.coerceAtLeast(0).coerceAtMost(255)
    }
}