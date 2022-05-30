package org.laolittle.plugin.draw.math

fun bilinearInterpolate(x: Float, y: Float, nw: Int, ne: Int, sw: Int, se: Int): Int {
    var m0: Float
    var m1: Float
    val a0 = nw shr 24 and 0xff
    val r0 = nw shr 16 and 0xff
    val g0 = nw shr 8 and 0xff
    val b0 = nw and 0xff
    val a1 = ne shr 24 and 0xff
    val r1 = ne shr 16 and 0xff
    val g1 = ne shr 8 and 0xff
    val b1 = ne and 0xff
    val a2 = sw shr 24 and 0xff
    val r2 = sw shr 16 and 0xff
    val g2 = sw shr 8 and 0xff
    val b2 = sw and 0xff
    val a3 = se shr 24 and 0xff
    val r3 = se shr 16 and 0xff
    val g3 = se shr 8 and 0xff
    val b3 = se and 0xff
    val cx = 1.0f - x
    val cy = 1.0f - y
    m0 = cx * a0 + x * a1
    m1 = cx * a2 + x * a3
    val a = (cy * m0 + y * m1).toInt()
    m0 = cx * r0 + x * r1
    m1 = cx * r2 + x * r3
    val r = (cy * m0 + y * m1).toInt()
    m0 = cx * g0 + x * g1
    m1 = cx * g2 + x * g3
    val g = (cy * m0 + y * m1).toInt()
    m0 = cx * b0 + x * b1
    m1 = cx * b2 + x * b3
    val b = (cy * m0 + y * m1).toInt()
    return a shl 24 or (r shl 16) or (g shl 8) or b
}