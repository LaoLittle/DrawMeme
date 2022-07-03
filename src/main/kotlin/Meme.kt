package org.laolittle.plugin.draw

interface Meme {
    val name: String

}

data class MemeException(
    override val message: String?,
    override val cause: Throwable? = null,
): Exception()