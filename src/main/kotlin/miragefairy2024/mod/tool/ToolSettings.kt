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
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

abstract class ToolSettings {
    companion object {
        private val identifier = MirageFairy2024.identifier("fairy_mining_tool")
        val AREA_MINING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.area_mining" }, "Area mining", "範囲採掘")
        val MINE_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SILK_TOUCH_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.silk_touch" }, "Silk Touch", "シルクタッチ")
        val FORTUNE_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.fortune" }, "Fortune", "幸運")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${identifier.toTranslationKey()}.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
        val OBTAIN_FAIRY = Translation({ "item.${identifier.toTranslationKey()}.obtain_fairy_when_mined" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")
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
    var silkTouch = false
    var fortune: Int? = null
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

    fun appendPoems(poemList: PoemList) = descriptions.fold(poemList) { it, description -> it.text(PoemType.DESCRIPTION, description) }

}

abstract class FairyMiningToolSettings : ToolSettings() {
    var attackDamage = 0F
    var attackSpeed = 0F
}


fun ToolSettings.areaMining() = this.also {
    it.areaMining = true
    it.descriptions += text { ToolSettings.AREA_MINING_TRANSLATION() }
}

fun ToolSettings.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += text { ToolSettings.MINE_ALL_TRANSLATION() }
}

fun ToolSettings.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += text { ToolSettings.CUT_ALL_TRANSLATION() }
}

fun ToolSettings.silkTouch() = this.also {
    it.silkTouch = true
    it.descriptions += text { ToolSettings.SILK_TOUCH_TRANSLATION() }
}

fun ToolSettings.fortune(fortune: Int) = this.also {
    check(fortune >= 1)
    it.fortune = fortune
    it.descriptions += text { ToolSettings.FORTUNE_TRANSLATION() + if (fortune >= 2) " "() + fortune.toRomanText() else ""() }
}

fun ToolSettings.selfMending(selfMending: Int) = this.also {
    it.selfMending = selfMending
    it.descriptions += text { ToolSettings.SELF_MENDING_TRANSLATION() }
}

fun ToolSettings.obtainFairy(appearanceRateBonus: Double) = this.also {
    it.obtainFairy = appearanceRateBonus
    it.descriptions += text { ToolSettings.OBTAIN_FAIRY() }
}
