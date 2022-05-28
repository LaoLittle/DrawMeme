package org.laolittle.plugin.draw

import kotlinx.coroutines.CoroutineExceptionHandler
import net.mamoe.mirai.event.globalEventChannel

val drawMemeEventChannel = DrawMeme.globalEventChannel(CoroutineExceptionHandler { context, e ->

})