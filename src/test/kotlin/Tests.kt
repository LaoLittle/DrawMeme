package org.laolittle.plugin.draw

import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import kotlin.io.path.Path
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.test.Test

class Tests {
    @Test
    fun blur() {
        val image = Image.makeFromEncoded(Path("atri.jpg").readBytes())

        Surface.makeRaster(image.imageInfo).apply {
            canvas.apply {
                drawImage(image, 0f, 0f, Paint().apply {
                    //this.maskFilter = MaskFilter.makeBlur(FilterBlurMode.INNER, 0f, true)
                    //imageFilter = ImageFilter.makeBlur(0f, 10f, FilterTileMode.MIRROR)
                    //imageFilter = ImageFilter.makeDilate(5f, 0f, null, null)
                    //imageFilter = ImageFilter.makeErode(5f, 0f, null, null)

                })

                drawImage(image, 0f, 0f, Paint().apply {
                    alpha = 125
                    //maskFilter = MaskFilter.makeBlur(FilterBlurMode.INNER, 0f, true)
                    imageFilter = ImageFilter.makeErode(5f, 0f, null, null)
                })
            }
        }.makeImageSnapshot().encodeToData()?.let {
            Path("outblur.png").writeBytes(it.bytes)
        }
    }

    /*@Test
    fun arrayList() {
        val a = arrayListOf<Float>(12f)

        a.add(22, 2f)

        println(a)
    }*/
}