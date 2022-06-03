package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.draw.makeFromResource
import org.laolittle.plugin.usedBy

private val paintText = Paint().apply {
    color = Color.WHITE
}

private val osuImage = Image.makeFromResource("/Osu/logo.png")

private val font = Fonts["Aller-Bold", 112.5F] usedBy "OSU图标生成"

fun osu(text: String = "osu!"): Image {
    val osuText: TextLine
    val yPos: Float
    val textWidth = font.measureTextWidth(text, paintText)
    if (textWidth <= 250) {
        osuText = TextLine.make(text, font)
        yPos = 137.5F + osuText.height / 2
    }else {
        yPos = 210 - (textWidth - 255) / 20
        osuText = TextLine.make(text, font.makeWithSize(250F / textWidth * 112.5F))
    }

    return Surface.makeRasterN32Premul(350, 350).apply {
        canvas.apply {
            drawImage(osuImage, 0F, 0F)
            drawTextLine(osuText, 175F - osuText.width / 2, yPos, paintText)
        }
    }.makeImageSnapshot()
}