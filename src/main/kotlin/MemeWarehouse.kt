package org.laolittle.plugin.draw

import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.full.functions

object MemeWarehouse {
    private val memeDat: ArrayList<MemeDat> = arrayListOf()

    fun register(meme: Meme) {
        when (meme) {
            is AbstractMeme -> {

            }

            else -> {

            }
        }
    }

    private data class MemeDat(
        val meme: Meme,
        val handlers: ArrayList<KFunction<Any>> = arrayListOf()
    )
}

