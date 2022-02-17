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
    }
}