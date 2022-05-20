package org.laolittle.plugin.draw

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import net.mamoe.mirai.message.nextMessage
import net.mamoe.mirai.utils.info
import org.jetbrains.skia.Rect
import java.io.File
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.skia.Image as SkImage

internal val httpClient = HttpClient(OkHttp)
internal val logger by DrawMeme::logger

internal fun String.splitSpace(): List<String>? {
    val words =
        when {
            this.length == 1 -> listOf(this, " ")
            contains("\n") -> split(Regex("\n+"))
            else -> trim().split(Regex("[\\s　]+"))
        }.toMutableList()

    if (words.isEmpty()) return null
    if (words.size == 1) {
        words.apply {
            val sentence = words[0].also { if (it.isBlank()) return null }
            clear()
            val left = sentence.length shr 1
            add(sentence.substring(0, left))
            if (sentence.length == 1)
                add(" ")
            else
                add(sentence.substring(left, sentence.length))
        }
    }
    return words
}

internal val emojiMixFolder by lazy {
    DrawMeme.dataFolder.resolve("emojimix")
        .also(File::mkdirs)
}

private const val emojiMixURL = "https://www.gstatic.com/android/keyboard/emojikitchen"
internal suspend fun getEmojiMix(main: Emoji, aux: Emoji): File? {
    val mainCode = main.code.toString(16)
    val auxCode = aux.code.toString(16)
    val date = supportedEmojis[main.code] ?: return null

    val fileName = "u${mainCode}_u${auxCode}.png"
    val file = emojiMixFolder
        .resolve(fileName)
    val giaFile = emojiMixFolder.resolve("u${auxCode}_u${mainCode}.png")

    return runCatching {
        if (file.isFile) file
        else if (giaFile.isFile) giaFile
        else {
            httpClient.get<ByteArray>("$emojiMixURL/$date/u$mainCode/$fileName").also { bytes ->
                file.writeBytes(bytes)
            }
            file
        }

    }.onFailure { logger.error(it) }.getOrNull()
}

private val supportedEmojis by lazy {
    runBlocking(DrawMeme.coroutineContext) {

        logger.info { "开始获取支持的Emoji列表" }

        val emo = mutableMapOf<Int, Long>()
        val returnStr: String = httpClient.get("https://tikolu.net/emojimix/emojis.js")

        val regex = Regex("""\[\[(.+)], "(\d+)"""")
        val finds = regex.findAll(returnStr)

        finds.forEach { result ->
            result.groupValues[1].split(",").forEach {
                emo[it.replace(" ", "").toInt()] = result.groupValues[2].toLong()
            }
        }
        emo
    }
}

internal fun SkImage.Companion.makeFromResource(name: String) = makeFromEncoded(DrawMeme::class.java.getResourceAsStream(name)?.readBytes() ?: throw IllegalStateException("无法找到资源文件: $name"))

fun Rect.Companion.makeFromImage(image: SkImage) = Rect(0f,0f,image.width.toFloat(), image.height.toFloat())

internal suspend fun MessageEvent.getOrWaitImage(): Image? {
    return (message.takeIf { m -> m.contains(Image) } ?: runCatching {
        subject.sendMessage("请在30s内发送图片")
        nextMessage(30_000) { event -> event.message.contains(Image) }
    }.getOrElse { e ->
        when (e) {
            is TimeoutCancellationException -> {
                subject.sendMessage(PlainText("超时未发送").plus(message.quote()))
                return null
            }
            else -> throw e
        }
    }).firstIsInstanceOrNull<Image>()
}

internal fun String.fuzzyMatchWith(target: String): Double {
    if (this == target) {
        return 1.0
    }
    var match = 0
    for (i in 0..(max(this.lastIndex, target.lastIndex))) {
        val t = target.getOrNull(match) ?: break
        if (t == this.getOrNull(i)) {
            match++
        }
    }

    val longerLength = max(this.length, target.length)
    val shorterLength = min(this.length, target.length)

    return match.toDouble() / (longerLength + (shorterLength - match))
}

/**
 * @return candidates
 */
internal fun Group.fuzzySearchMember(
    nameCardTarget: String,
    minRate: Double = 0.2, // 参与判断, 用于提示可能的解
    matchRate: Double = 0.6,// 最终选择的最少需要的匹配率, 减少歧义
    /**
     * 如果有多个值超过 [matchRate], 并相互差距小于等于 [disambiguationRate], 则认为有较大歧义风险, 返回可能的解的列表.
     */
    disambiguationRate: Double = 0.1,
): List<Pair<Member, Double>> {
    val candidates = (this.members + botAsMember)
        .asSequence()
        .associateWith { it.nameCardOrNick.fuzzyMatchWith(nameCardTarget) }
        .filter { it.value >= minRate }
        .toList()
        .sortedByDescending { it.second }

    val bestMatches = candidates.filter { it.second >= matchRate }

    return when {
        bestMatches.isEmpty() -> candidates
        bestMatches.size == 1 -> listOf(bestMatches.single().first to 1.0)
        else -> {
            if (bestMatches.first().second - bestMatches.last().second <= disambiguationRate) {
                // resolution ambiguity
                candidates
            } else {
                listOf(bestMatches.first().first to 1.0)
            }
        }
    }
}

/**
 * 通过消息获取联系人
 * 若[Contact]为[Group]，则可通过群员昵称获取联系人
 * 否则通过QQ号查找，查找失败返回``null``
 * @param msg 传入的消息[String]
 * @return User if only one is found null otherwise
 * */
fun Contact.findUserOrNull(msg: String): User? {
    val noneAt = msg.replace("@", "").replace(" ", "")
    if (noneAt.isBlank()) {
        return null
    }
    return if (noneAt.contains(Regex("""\D"""))) {
        when (this) {
            is Group -> this.findMemberOrNull(noneAt)
            else -> null
        }
    } else {
        val number = noneAt.toLong()
        when (this) {
            is Group -> this[number]
            else -> bot.getFriend(number) ?: bot.getStranger(number)
        }
    }
}

/**
 * 从一个群中模糊搜索昵称是[nameCard]的群员
 * @param nameCard 群员昵称
 * @return Member if only one exist or null otherwise
 * @author mamoe
 * */
private fun Group.findMemberOrNull(nameCard: String): Member? {
    this.members.singleOrNull { it.nameCardOrNick.contains(nameCard) }?.let { return it }
    this.members.singleOrNull { it.nameCardOrNick.contains(nameCard, ignoreCase = true) }?.let { return it }

    val candidates = this.fuzzySearchMember(nameCard)
    candidates.singleOrNull()?.let {
        if (it.second == 1.0) return it.first // single match
    }
    var maxPerMember: Member? = null
    if (candidates.isNotEmpty()) {
        var maxPer = 0.0
        candidates.forEach {
            if (it.second > maxPer) maxPerMember = it.first
            maxPer = it.second
        }
    }
    return maxPerMember
}