package org.laolittle.plugin.draw

import kotlin.reflect.full.functions

abstract class AbstractMeme(
    override val name: String,

): Meme {
    open val prefix = "#"

    @Target(AnnotationTarget.FUNCTION)
    protected annotation class MemeHandler(
        val option: MemeHandlerOption = MemeHandlerOption.Name,
        val pat: String = ""
    )


    fun call(vararg arg: Any) {

    }
}

enum class MemeHandlerOption {
    Name,
    Regex,
}