package org.laolittle.plugin.draw.extension

import net.mamoe.mirai.contact.*
import kotlin.math.max
import kotlin.math.min


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