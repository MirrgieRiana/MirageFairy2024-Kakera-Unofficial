package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.util.aqua
import miragefairy2024.util.en
import miragefairy2024.util.formatted
import miragefairy2024.util.ja
import miragefairy2024.util.text
import net.minecraft.item.Item
import net.minecraft.text.Text
import net.minecraft.util.Formatting

val itemPoemListTable = mutableMapOf<Item, PoemList>()

fun initPoemModule() = ModEvents.onInitialize {
    MirageFairy2024.onClientInit {
        it.registerItemTooltipCallback { stack, lines ->
            val poemList = itemPoemListTable[stack.item] ?: return@registerItemTooltipCallback
            val texts = mutableListOf<Text>()

            poemList.poems.filter { it.type == PoemType.POEM }.forEach {
                texts += it.getText(stack.item)
            }

            if (poemList.tier != null) {
                texts += text { "Tier ${poemList.tier}"().aqua }
            }

            poemList.poems.filter { it.type == PoemType.DESCRIPTION }.forEach {
                texts += it.getText(stack.item)
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

interface Poem {
    val type: PoemType
    fun getText(item: Item): Text
    fun init(item: Item) = Unit
}

class InternalPoem(override val type: PoemType, private val key: String, private val en: String, private val ja: String) : Poem {
    override fun getText(item: Item) = text { translate("${item.translationKey}.$key").formatted(type.color) }
    override fun init(item: Item) {
        en { "${item.translationKey}.$key" to en }
        ja { "${item.translationKey}.$key" to ja }
    }
}


// PoemList

class PoemList(val tier: Int?, val poems: List<Poem>)

fun PoemList(tier: Int?) = PoemList(tier, listOf())
operator fun PoemList.plus(poem: Poem) = PoemList(this.tier, this.poems + poem)
fun PoemList.poem(key: String, en: String, ja: String) = this + InternalPoem(PoemType.POEM, key, en, ja)
fun PoemList.poem(en: String, ja: String) = this + InternalPoem(PoemType.POEM, "poem", en, ja)
fun PoemList.description(key: String, en: String, ja: String) = this + InternalPoem(PoemType.DESCRIPTION, key, en, ja)
fun PoemList.description(en: String, ja: String) = this + InternalPoem(PoemType.DESCRIPTION, "description", en, ja)


// Util

fun Item.registerPoem(poemList: PoemList) {
    require(this !in itemPoemListTable)
    itemPoemListTable[this] = poemList
}

fun Item.registerPoemGeneration(poemList: PoemList) {
    poemList.poems.forEach {
        it.init(this)
    }
}
