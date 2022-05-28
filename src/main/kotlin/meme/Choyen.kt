package org.laolittle.plugin.draw.meme

import org.jetbrains.skia.*
import org.laolittle.plugin.Fonts
import org.laolittle.plugin.usedBy

private val topFont = Fonts["Noto Sans SC-BOLD"] usedBy "5k兆顶部文字"
private val bottomFont = Fonts["Noto Serif SC-BOLD"] usedBy "5k兆底部文字"
fun choyen(top: String, bottom: String): Image {
    val topText = TextLine.make(top, topFont)
    val bottomText = TextLine.make(bottom, bottomFont)
    val width = maxOf(topText.width + 70, bottomText.width + 250).toInt()

    return Surface.makeRasterN32Premul(width, 290).apply {
        canvas.apply {
            skew(-0.45F, 0F)

            val topX = 70F
            val topY = 100F
            val paintTop = Paint().apply {
                mode = PaintMode.STROKE
                strokeCap = PaintStrokeCap.ROUND
                strokeJoin = PaintStrokeJoin.ROUND
            }
            // 黒色
            drawTextLine(topText, topX + 4, topY + 4, paintTop.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 22F
            })
            // 銀色
            drawTextLine(topText, topX + 4, topY + 4, paintTop.apply {
                shader = Shader.makeLinearGradient(
                    0F, 24F, 0F, 122F, intArrayOf(
                        Color.makeRGB(0, 15, 36),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(200, 200, 200),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(25, 20, 31),
                        Color.makeRGB(240, 240, 240),
                        Color.makeRGB(166, 175, 194),
                        Color.makeRGB(50, 50, 50)
                    ), floatArrayOf(0.0F, 0.10F, 0.18F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                )
                strokeWidth = 20F
            })
            // 黒色
            drawTextLine(topText, topX, topY, paintTop.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 16F
            })
            // 金色
            drawTextLine(topText, topX, topY, paintTop.apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(253, 241, 0),
                        Color.makeRGB(245, 253, 187),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(253, 219, 9),
                        Color.makeRGB(127, 53, 0),
                        Color.makeRGB(243, 196, 11),
                    ), floatArrayOf(0.0F, 0.25F, 0.4F, 0.75F, 0.9F, 1F)
                )
                strokeWidth = 10F
            })
            // 黒色
            drawTextLine(topText, topX + 2, topY - 3, paintTop.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 6F
            })
            // 白色
            drawTextLine(topText, topX, topY - 3, paintTop.apply {
                shader = null
                color = Color.makeRGB(255, 255, 255)
                strokeWidth = 6F
            })
            // 赤色
            drawTextLine(topText, topX, topY - 3, paintTop.apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(255, 100, 0),
                        Color.makeRGB(123, 0, 0),
                        Color.makeRGB(240, 0, 0),
                        Color.makeRGB(5, 0, 0),
                    ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                )
                strokeWidth = 4F
            })
            // 赤色
            drawTextLine(topText, topX, topY - 3, paintTop.setStroke(false).apply {
                shader = Shader.makeLinearGradient(
                    0F, 20F, 0F, 100F, intArrayOf(
                        Color.makeRGB(230, 0, 0),
                        Color.makeRGB(123, 0, 0),
                        Color.makeRGB(240, 0, 0),
                        Color.makeRGB(5, 0, 0),
                    ), floatArrayOf(0.0F, 0.5F, 0.51F, 1F)
                )
            })


            val bottomX = 250F
            val bottomY = 230F
            val paint = Paint().apply {
                mode = PaintMode.STROKE
                strokeCap = PaintStrokeCap.ROUND
                strokeJoin = PaintStrokeJoin.ROUND
            }

            // 黒色
            drawTextLine(bottomText, bottomX + 5, bottomY + 2, paint.apply {
                shader = null
                color = Color.makeRGB(0, 0, 0)
                strokeWidth = 22F
            })
            // 銀色
            drawTextLine(bottomText, bottomX + 5, bottomY + 2, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, bottomY - 80, 0F, bottomY + 18, intArrayOf(
                        Color.makeRGB(0, 15, 36),
                        Color.makeRGB(250, 250, 250),
                        Color.makeRGB(150, 150, 150),
                        Color.makeRGB(55, 58, 59),
                        Color.makeRGB(25, 20, 31),
                        Color.makeRGB(240, 240, 240),
                        Color.makeRGB(166, 175, 194),
                        Color.makeRGB(50, 50, 50)
                    ), floatArrayOf(0.0F, 0.25F, 0.5F, 0.75F, 0.85F, 0.91F, 0.95F, 1F)
                )
                strokeWidth = 19F
            })
            // 黒色
            drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                shader = null
                color = Color.makeRGB(16, 25, 58)
                strokeWidth = 17F
            })
            // 白色
            drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                shader = null
                color = Color.makeRGB(221, 221, 221)
                strokeWidth = 8F
            })
            // 紺色
            drawTextLine(bottomText, bottomX, bottomY, paint.apply {
                shader = Shader.makeLinearGradient(
                    0F, bottomY - 80, 0F, bottomY, intArrayOf(
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(16, 25, 58),
                        Color.makeRGB(16, 25, 58),
                    ), floatArrayOf(0.0F, 0.03F, 0.08F, 0.2F, 1F)
                )
                strokeWidth = 7F
            })
            // 銀色
            drawTextLine(bottomText, bottomX, bottomY - 3, paint.setStroke(false).apply {
                shader = Shader.makeLinearGradient(
                    0F, bottomY - 80, 0F, bottomY, intArrayOf(
                        Color.makeRGB(245, 246, 248),
                        Color.makeRGB(255, 255, 255),
                        Color.makeRGB(195, 213, 220),
                        Color.makeRGB(160, 190, 201),
                        Color.makeRGB(160, 190, 201),
                        Color.makeRGB(196, 215, 222),
                        Color.makeRGB(255, 255, 255)
                    ), floatArrayOf(0.0F, 0.15F, 0.35F, 0.5F, 0.51F, 0.52F, 1F)
                )
                strokeWidth = 19F
            })
        }
    }.makeImageSnapshot()
}