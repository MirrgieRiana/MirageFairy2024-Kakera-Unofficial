package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.plus
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import miragefairy2024.util.translate
import mirrg.kotlin.hydrogen.max
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

abstract class ToolConfiguration {
    companion object {
        private val identifier = MirageFairy2024.identifier("fairy_mining_tool")
        val AREA_MINING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.area_mining" }, "Area mining", "範囲採掘")
        val MINE_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
        val OBTAIN_FAIRY_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.obtain_fairy_when_mined" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")
    }

    abstract val toolMaterialCard: ToolMaterialCard
    val tags = mutableListOf<TagKey<Item>>()
    var miningSpeedMultiplierOverride: Float? = null
    val superEffectiveBlocks = mutableListOf<Block>()
    val effectiveBlocks = mutableListOf<Block>()
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var miningDamage = 1.0
    var areaMining = false
    var mineAll = false
    var cutAll = false
    val enchantments = mutableMapOf<Enchantment, Int>()
    var selfMending: Int? = null
    val descriptions = mutableListOf<Text>()
    var obtainFairy: Double? = null

    abstract fun createItem(): Item

    context(ModContext)
    fun init(card: ToolCard) {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    fun appendPoems(poemList: PoemList): PoemList {
        val texts = mutableListOf<Text>()

        texts += descriptions
        enchantments.forEach { (enchantment, level) ->
            texts += text { translate(enchantment.translationKey) + if (level >= 2 || enchantment.maxLevel >= 2) " "() + level.toRomanText() else ""() }
        }

        return texts.fold(poemList) { it, description -> it.text(PoemType.DESCRIPTION, description) }
    }

}

abstract class FairyMiningToolConfiguration : ToolConfiguration() {
    var attackDamage = 0F
    var attackSpeed = 0F
}


fun ToolConfiguration.areaMining() = this.also {
    it.areaMining = true
    it.descriptions += text { ToolConfiguration.AREA_MINING_TRANSLATION() }
}

fun ToolConfiguration.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += text { ToolConfiguration.MINE_ALL_TRANSLATION() }
}

fun ToolConfiguration.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += text { ToolConfiguration.CUT_ALL_TRANSLATION() }
}

fun ToolConfiguration.enchantment(enchantment: Enchantment, level: Int = 1) = this.also {
    check(level >= 1)
    it.enchantments[enchantment] = (it.enchantments[enchantment] ?: 0) max level
}

fun ToolConfiguration.selfMending(selfMending: Int) = this.also {
    it.selfMending = selfMending
    it.descriptions += text { ToolConfiguration.SELF_MENDING_TRANSLATION() }
}

fun ToolConfiguration.obtainFairy(appearanceRateBonus: Double) = this.also {
    it.obtainFairy = appearanceRateBonus
    it.descriptions += text { ToolConfiguration.OBTAIN_FAIRY_TRANSLATION() }
}
