package org.laolittle.plugin.draw

import net.mamoe.mirai.console.plugin.description.PluginDependency
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.*
import org.laolittle.plugin.toExternalResource

object DrawMeme : KotlinPlugin(
    JvmPluginDescription(
        id = "org.laolittle.plugin.draw.DrawMeme",
        name = "DrawMeme",
        version = "1.0",
    ) {
        author("LaoLittle")

        dependsOn(
            PluginDependency("org.laolittle.plugin.SkikoMirai", ">=1.0", true)
        )
    }
) {
    override fun onEnable() {
        logger.info { "Plugin loaded" }
        globalEventChannel().subscribeGroupMessages {
            startsWith("#ph") {
                val words = it.split(Regex("[\\s　]+")).toMutableList()

                if (words.isEmpty()) return@startsWith
                if (words.size == 1) {
                    words.apply {
                        val sentence = words[0]
                        clear()
                        val left = if (sentence.isNotEmpty())
                            (sentence.length shr 1) else 1
                        add(sentence.substring(0, left))
                        if (sentence.length == 1)
                            add(" ")
                        else
                            add(sentence.substring(left, sentence.length))
                    }
                }

                val phHeight = 170
                val widthPlus = 12

                val leftText = TextLine.make(words[0], MiSansBold88)
                val leftPorn = Surface.makeRasterN32Premul(leftText.width.toInt() + (widthPlus shl 1), phHeight)

                leftPorn.canvas.apply {
                    clear(Color.makeARGB(255, 0, 0, 0))
                    drawTextLine(
                        leftText,
                        (leftPorn.width - leftText.width) / 2 + 5,
                        ((leftPorn.height shr 1) + (leftText.height / 4)),
                        Paint().apply { color = Color.makeARGB(255, 255, 255, 255) }
                    )
                }

                val rightText = TextLine.make(words[1], MiSansBold88)
                val rightPorn = Surface.makeRasterN32Premul(
                    rightText.width.toInt() + (widthPlus shl 1) + 20,
                    rightText.height.toInt()
                )

                rightPorn.canvas.apply {
                    val rRect = RRect.makeComplexXYWH(
                        ((rightPorn.width - rightText.width) / 2) - widthPlus,
                        0F,
                        rightText.width + widthPlus,
                        rightText.height - 1,
                        floatArrayOf(19.5F)
                    )
                    drawRRect(
                        rRect, Paint().apply { color = Color.makeARGB(255, 255, 145, 0) }
                    )
                    // clear(Color.makeARGB(255, 255,144,0))
                    // drawCircle(100F, 100F, 50F, Paint().apply { color = Color.BLUE })
                    drawTextLine(
                        rightText,
                        ((rightPorn.width - rightText.width - widthPlus.toFloat()) / 2),
                        ((rightPorn.height shr 1) + (rightText.height / 4) + 2),
                        Paint().apply { color = Color.makeARGB(255, 0, 0, 0) }
                    )
                }

                Surface.makeRasterN32Premul(leftPorn.width + rightPorn.width, phHeight).apply {
                    canvas.apply {
                        clear(Color.makeARGB(255, 0, 0, 0))
                        drawImage(leftPorn.makeImageSnapshot(), 0F, 0F)
                        drawImage(
                            rightPorn.makeImageSnapshot(),
                            leftPorn.width.toFloat() - (widthPlus shr 1),
                            (((phHeight - rightPorn.height) shr 1) - 2).toFloat()
                        )
                    }
                    toExternalResource().use { res ->
                        subject.sendImage(res)
                    }
                }
            }
        }
    }
}