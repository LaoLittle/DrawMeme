package org.laolittle.plugin.draw.extension

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TypefaceFontProvider

object GlobalParagraphMgr {
    val provider = TypefaceFontProvider()

    val style = ParagraphStyle().apply {

    }

    val fc = FontCollection().apply {
        setDynamicFontManager(provider)
        setDefaultFontManager(FontMgr.default)
    }

    fun registerTypeface(typeface: Typeface?, alias: String? = null) = provider.registerTypeface(typeface, alias)
}