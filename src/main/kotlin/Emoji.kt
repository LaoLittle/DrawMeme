package org.laolittle.plugin.draw

data class Emoji(val code: Int) {
    fun toSurrogates() = code.toChars()

    override fun toString() = String(toSurrogates())

    override operator fun equals(other: Any?): Boolean =
        when (other) {
            is Emoji -> this.code == other.code
            else -> false
        }

    override fun hashCode() = code

    companion object EmojiUtil {
        private const val MIN_LOW_SURROGATE = '\uDC00'

        private const val MIN_HIGH_SURROGATE = '\uD800'

        private const val MIN_SUPPLEMENTARY_CODE_POINT = 0x010000

        private const val MAX_CODE_POINT = 0X10FFFF

        fun String.toEmoji(): Emoji {
            require(length == 2) { "Expected length is 2, but length is $length" }
            val emojiCode =
                ((get(0).code -
                        (MIN_HIGH_SURROGATE.code -
                                (MIN_SUPPLEMENTARY_CODE_POINT ushr 10))) shl 10) +
                        (get(1).code - MIN_LOW_SURROGATE.code)
            return Emoji(emojiCode)
        }

        private fun Int.toChars(): CharArray =
            if (ushr(16) == 0) {
                charArrayOf(toChar())
            } else if ((ushr(16) < (MAX_CODE_POINT + 1) ushr 16)) {
                CharArray(2).apply {
                    set(0, this@toChars.highSurrogate)
                    set(1, this@toChars.lowSurrogate)
                }
            } else {
                throw IllegalArgumentException(String.format("Not a valid Unicode code point: 0x%X", this))
            }

        private val Int.highSurrogate
            get() = (ushr(10) + (MIN_HIGH_SURROGATE.code - (MIN_SUPPLEMENTARY_CODE_POINT ushr 10))).toChar()

        private val Int.lowSurrogate get() = (and(0x3ff) + MIN_LOW_SURROGATE.code).toChar()

        private const val MiscellaneousSymbolsAndPictographs = "[\\uD83C\\uDF00-\\uD83D\\uDDFF]"
        private const val SupplementalSymbolsAndPictographs = "[\\uD83E\\uDD00-\\uD83E\\uDDFF]"
        private const val Emoticons = "[\\uD83D\\uDE00-\\uD83D\\uDE4F]"
        private const val TransportAndMapSymbols = "[\\uD83D\\uDE80-\\uD83D\\uDEFF]"
        private const val MiscellaneousSymbols = "[\\u2600-\\u26FF]\\uFE0F?"
        private const val Dingbats = "[\\u2700-\\u27BF]\\uFE0F?"
        private const val EnclosedAlphanumerics = "\\u24C2\\uFE0F?"
        private const val RegionalIndicatorSymbol = "[\\uD83C\\uDDE6-\\uD83C\\uDDFF]{1,2}"
        private const val EnclosedAlphanumericSupplement =
            "[\\uD83C\\uDD70\\uD83C\\uDD71\\uD83C\\uDD7E\\uD83C\\uDD7F\\uD83C\\uDD8E\\uD83C\\uDD91-\\uD83C\\uDD9A]\\uFE0F?"
        private const val BasicLatin = "[\\u0023\\u002A\\u0030-\\u0039]\\uFE0F?\\u20E3"
        private const val Arrows = "[\\u2194-\\u2199\\u21A9-\\u21AA]\\uFE0F?"
        private const val MiscellaneousSymbolsAndArrows = "[\\u2B05-\\u2B07\\u2B1B\\u2B1C\\u2B50\\u2B55]\\uFE0F?"
        private const val SupplementalArrows = "[\\u2934\\u2935]\\uFE0F?"
        private const val CJKSymbolsAndPunctuation = "[\\u3030\\u303D]\\uFE0F?"
        private const val EnclosedCJKLettersAndMonths = "[\\u3297\\u3299]\\uFE0F?"
        private const val EnclosedIdeographicSupplement =
            "[\\uD83C\\uDE01\\uD83C\\uDE02\\uD83C\\uDE1A\\uD83C\\uDE2F\\uD83C\\uDE32-\\uD83C\\uDE3A\\uD83C\\uDE50\\uD83C\\uDE51]\\uFE0F?"
        private const val GeneralPunctuation = "[\\u203C\\u2049]\\uFE0F?"
        private const val GeometricShapes = "[\\u25AA\\u25AB\\u25B6\\u25C0\\u25FB-\\u25FE]\\uFE0F?"
        private const val LatinSupplement = "[\\u00A9\\u00AE]\\uFE0F?"
        private const val LetterlikeSymbols = "[\\u2122\\u2139]\\uFE0F?"
        private const val MahjongTiles = "\\uD83C\\uDC04\\uFE0F?"
        private const val PlayingCards = "\\uD83C\\uDCCF\\uFE0F?"
        private const val MiscellaneousTechnical =
            "[\\u231A\\u231B\\u2328\\u23CF\\u23E9-\\u23F3\\u23F8-\\u23FA]\\uFE0F?"
        val fullEmojiRegex = (
                MiscellaneousSymbolsAndPictographs + "|"
                        + SupplementalSymbolsAndPictographs + "|"
                        + Emoticons + "|"
                        + TransportAndMapSymbols + "|"
                        + MiscellaneousSymbols + "|"
                        + Dingbats + "|"
                        + EnclosedAlphanumerics + "|"
                        + RegionalIndicatorSymbol + "|"
                        + EnclosedAlphanumericSupplement + "|"
                        + BasicLatin + "|"
                        + Arrows + "|"
                        + MiscellaneousSymbolsAndArrows + "|"
                        + SupplementalArrows + "|"
                        + CJKSymbolsAndPunctuation + "|"
                        + EnclosedCJKLettersAndMonths + "|"
                        + EnclosedIdeographicSupplement + "|"
                        + GeneralPunctuation + "|"
                        + GeometricShapes + "|"
                        + LatinSupplement + "|"
                        + LetterlikeSymbols + "|"
                        + MahjongTiles + "|"
                        + PlayingCards + "|"
                        + MiscellaneousTechnical).toRegex()
    }
}