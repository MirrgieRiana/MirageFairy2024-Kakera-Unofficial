package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.aqua
import miragefairy2024.util.en
import miragefairy2024.util.formatted
import miragefairy2024.util.ja
import miragefairy2024.util.text
import net.minecraft.item.Item
import net.minecraft.text.Text
import net.minecraft.util.Formatting

val itemPoemListTable = mutableMapOf<Item, PoemList>()

fun initPoemModule() {
    MirageFairy2024.onClientInit {
        it.registerItemTooltipCallback { stack, lines ->
            val poemList = itemPoemListTable[stack.item] ?: return@registerItemTooltipCallback
            val texts = mutableListOf<Text>()

            poemList.poems.filter { it.type == PoemType.POEM }.forEach {
                texts += text { translate("${stack.item.translationKey}.${it.key}").formatted(it.type.color) }
            }

            if (poemList.tier != null) {
                texts += text { "Tier ${poemList.tier}"().aqua }
            }

            poemList.poems.filter { it.type == PoemType.DESCRIPTION }.forEach {
                texts += text { translate("${stack.item.translationKey}.${it.key}").formatted(it.type.color) }
            }

            texts.forEachIndexed { index, it ->
                lines.add(1 + index, it)
            }
        }
    }
}


// Poem

enum class PoemType(val color: Formatting) {
    POEM(Formatting.DARK_AQUA),
    DESCRIPTION(Formatting.YELLOW),
}

class Poem(val type: PoemType, val key: String, val en: String, val ja: String)


// PoemList

class PoemList(val tier: Int?, val poems: List<Poem>)

fun PoemList(tier: Int?) = PoemList(tier, listOf())
fun PoemList.poem(key: String, en: String, ja: String) = PoemList(this.tier, this.poems + Poem(PoemType.POEM, key, en, ja))
fun PoemList.poem(en: String, ja: String) = PoemList(this.tier, this.poems + Poem(PoemType.POEM, "poem", en, ja))
fun PoemList.description(key: String, en: String, ja: String) = PoemList(this.tier, this.poems + Poem(PoemType.DESCRIPTION, key, en, ja))
fun PoemList.description(en: String, ja: String) = PoemList(this.tier, this.poems + Poem(PoemType.DESCRIPTION, "description", en, ja))


// Util

fun Item.registerPoem(poemList: PoemList) {
    require(this !in itemPoemListTable)
    itemPoemListTable[this] = poemList
}

fun Item.registerPoemGeneration(poemList: PoemList) {
    poemList.poems.forEach {
        en { "${this.translationKey}.${it.key}" to it.en }
        ja { "${this.translationKey}.${it.key}" to it.ja }
    }
}
