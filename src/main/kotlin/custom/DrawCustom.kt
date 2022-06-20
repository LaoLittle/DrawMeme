package org.laolittle.plugin.draw.custom

import io.ktor.client.request.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import org.jetbrains.skia.Image
import org.laolittle.plugin.draw.DrawMeme
import org.laolittle.plugin.draw.drawMemeEventChannel
import org.laolittle.plugin.draw.httpClient
import java.io.File

internal val customMemeFolder = DrawMeme.dataFolder.resolve("custom").also(File::mkdirs)

internal val customMemes = mutableListOf<CustomMeme>()

internal fun initCustomMemes() {
    customMemeFolder.listFiles()?.forEach {
        if (it.isFile) customMemes.add(CustomMeme.fromFile(it))
    }

    drawMemeEventChannel.subscribeGroupMessages {
        customMemes.forEach { meme ->
            startsWith("#${meme.name}") {
                val avatars = arrayListOf<Deferred<Image>>()
                message.forEach { m ->
                    if (m is At) {
                        avatars.add(DrawMeme.async {
                            val id = m.target
                            Image.makeFromEncoded(httpClient.get("https://q1.qlogo.cn/g?b=qq&nk=$id&s=640"))
                        })
                    }
                }

                meme.makeImage(*avatars.awaitAll().toTypedArray())
                    .toExternalResource(if (meme.isGif) "GIF" else null).use {
                        subject.sendImage(it)
                    }

            }
        }
    }
}