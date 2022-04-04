package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.usedBy

private val paint = Paint()
private val pornColor = Color.makeRGB(255, 145, 0)

private const val phHeight = 170
private const val widthPlus = 12

val phFont = Fonts["MiSans-Bold", 88F] usedBy "PornHub生成器"
fun pornHub(left: String, right: String): Surface {
    val leftText = TextLine.make(left, phFont)
    val leftPorn = Surface.makeRasterN32Premul(leftText.width.toInt() + (widthPlus shl 1), phHeight)

    leftPorn.canvas.apply {
        drawTextLine(
            leftText,
            (leftPorn.width - leftText.width) / 2 + 5,
            ((leftPorn.height shr 1) + (leftText.height / 4)),
            paint.apply { color = Color.WHITE }
        )
    }

    val rightText = TextLine.make(right, phFont)
    val rightPorn = Surface.makeRasterN32Premul(
        rightText.width.toInt() + (widthPlus shl 1) + 20,
        rightText.height.toInt()
    )

    rightPorn.canvas.apply {
        val rRect = RRect.makeXYWH(
            ((rightPorn.width - rightText.width) / 2) - widthPlus,
            0F,
            rightText.width + widthPlus,
            rightText.height - 1,
            19.5F
        )
        drawRRect(
            rRect, paint.apply { color = pornColor }
        )
        // clear(Color.makeARGB(255, 255,144,0))
        // drawCircle(100F, 100F, 50F, Paint().apply { color = Color.BLUE })
        drawTextLine(
            rightText,
            ((rightPorn.width - rightText.width - widthPlus.toFloat()) / 2),
            ((rightPorn.height shr 1) + (rightText.height / 4) + 2),
            paint.apply { color = Color.BLACK }
        )
    }

    return Surface.makeRasterN32Premul(leftPorn.width + rightPorn.width, phHeight).apply {
        canvas.apply {
            clear(Color.BLACK)
            leftPorn.draw(this, 0, 0, null)
            rightPorn.draw(
                this,
                leftPorn.width - (widthPlus shr 1),
                (((phHeight - rightPorn.height) shr 1) - 2),
                null
            )
            //drawImage(
            //    rightPorn.makeImageSnapshot(),
            //    leftPorn.width.toFloat() - (widthPlus shr 1),
            //    (((phHeight - rightPorn.height) shr 1) - 2).toFloat()
            //)
        }

    }
}