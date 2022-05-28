package org.laolittle.plugin.draw

import kotlinx.coroutines.runBlocking
import okhttp3.internal.toHexString
import org.jetbrains.skia.*
import org.laolittle.plugin.bytes
import org.laolittle.plugin.draw.custom.CustomMeme
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.random.Random
import kotlin.system.measureTimeMillis
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
                    this.maskFilter
                })

                /*drawImage(image, 0f, 0f, Paint().apply {

                    //maskFilter = MaskFilter.makeBlur(FilterBlurMode.INNER, 0f, true)
                    imageFilter = ImageFilter.makeErode(5f, 0f, null, null)
                })*/
            }
        }.makeImageSnapshot().encodeToData()?.let {
            Path("outblur.png").writeBytes(it.bytes)
        }
    }

    @Test
    fun bit() {
        val a = 0xffffffff

        println(a.toInt().toString(2))
        println(a.toInt().toHexString())
    }

    @Test
    fun bba() {
        val image = Image.makeFromEncoded(Path("atri.jpg").readBytes())

        fun random(): Float {
            return Random.nextDouble(-15.0,15.0).toFloat()
        }

        Surface.makeRaster(image.imageInfo).apply {

            canvas.apply {
                drawImage(image, 0f, 0f)
                drawImage(image, random(), random(), Paint().apply {
                    alpha = 100
                    colorFilter = ColorFilter.makeMatrix(ColorMatrix(
                        1F,0F,0.2F,0.3F,0F,
                        0F,0F,0F,0F,0F,
                        0F,0F,0F,0F,0F,
                        0F,0F,0F,1F,0F,
                    ))
                })
            }
        }.makeImageSnapshot().encodeToData()?.let {
            Path("outfi.png").writeBytes(it.bytes)
        }
    }

    @Test
    fun blue() {
        val image = Image.makeFromEncoded(Path("atri.jpg").readBytes())

        Surface.makeRaster(image.imageInfo).apply {

            canvas.apply {
                drawImage(image, 0f, 0f, Paint().apply {
                    colorFilter = ColorFilter.makeMatrix(ColorMatrix(
                      //R  G  B  A  ?
                        1F,0F,0F,0F,0F,       // R
                        0F,1F,0F,0F,0F,       // G
                        0.5F,0.6F,1F,.3F,0F, // B
                        0F,0F,0F,1F,0F,       // A
                    ))
                })
            }
        }.makeImageSnapshot().encodeToData()?.let {
            Path("outblue.png").writeBytes(it.bytes)
        }
    }

    @Test
    fun pic() {
        val image = Image.makeFromEncoded(Path("atri.jpg").readBytes())
        val recorder = PictureRecorder().apply {
            beginRecording(image.imageInfo.bounds.toRect())
        }

        recorder.recordingCanvas?.apply {
            drawCircle(0f,0f,100f, Paint())
            drawImage(image, 0f,0f)

        }

        val pic = recorder.finishRecordingAsPicture()
        pic.serializeToData().bytes.also(Path("pic.bin")::writeBytes)

        Surface.makeRaster(image.imageInfo).apply {
            canvas.drawImage(image,0f,0f)
            canvas.drawPicture(pic)
           // pic.playback(canvas)
        }.makeImageSnapshot().bytes.also(Path("outpic.png")::writeBytes)
    }

    @Test
    fun customMeme() {
        measureTimeMillis {
            val image = Image.makeFromEncoded(Path("atri.jpg").readBytes())
            val a = CustomMeme.fromFile(File("testmeme.txt"))
            a.isGif
            a.input
            a.actions
            a.name
            runBlocking {
                a.makeImage(image).also(Path("custom.png")::writeBytes)
            }
        }
    }

    @Test
    fun path() {
        Path(".idea").listDirectoryEntries().forEach {
            println(it)
        }
    }
    /*@Test
    fun arrayList() {
        val a = arrayListOf<Float>(12f)

        a.add(22, 2f)

        println(a)
    }*/
}