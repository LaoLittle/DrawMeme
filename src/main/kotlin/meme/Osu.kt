package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.draw.makeFromResource
import org.laolittle.plugin.usedBy

fun osu(text: String = "osu!"): Image {
    val paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
    }
    val image = Image.makeFromResource("/Osu/logo.png")
    var osuText = TextLine.make(text, Fonts["Aller-Bold", 112.5F] usedBy "OSU图标生成")
    var yPos = 137.5F + osuText.height / 2
    if (osuText.width > 250) {
        yPos = 210 - (osuText.width - 255) / 20
        osuText = TextLine.make(text, Fonts["Aller-Bold", 250F / osuText.width * 112.5F] usedBy "OSU图标生成")
    }
    return Surface.makeRasterN32Premul(350, 350).apply {
        canvas.apply {
            drawImage(image, 0F, 0F)
            drawTextLine(osuText, 175F - osuText.width / 2, yPos, paint)
        }
    }.makeImageSnapshot()
}