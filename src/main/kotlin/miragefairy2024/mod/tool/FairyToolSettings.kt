package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.PoemType
import miragefairy2024.mod.text
import miragefairy2024.util.Translation
import miragefairy2024.util.invoke
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text

class FairyToolSettings(
    val creator: (FairyToolSettings) -> Item,
    val toolMaterialCard: ToolMaterialCard,
) : ToolSettings {
    companion object {
        val AREA_MINING_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.area_mining" }, "Area mining", "範囲採掘")
        val MINE_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.mine_all" }, "Mine the entire ore", "鉱石全体を採掘")
        val CUT_ALL_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.cut_all" }, "Cut down the entire tree", "木全体を伐採")
        val SILK_TOUCH_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.silk_touch" }, "Silk Touch", "シルクタッチ")
        val FORTUNE_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.fortune" }, "Fortune", "幸運")
        val SELF_MENDING_TRANSLATION = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.self_mending" }, "Self-mending while in the main hand", "メインハンドにある間、自己修繕")
        val OBTAIN_FAIRY = Translation({ "item.${MirageFairy2024.MOD_ID}.fairy_mining_tool.obtain_fairy_when_mined" }, "Obtain a fairy when mined or killed", "採掘・撃破時に妖精を入手")
    }

    val tags = mutableListOf<TagKey<Item>>()
    var attackDamage = 0F
    var attackSpeed = 0F
    var miningSpeedMultiplierOverride: Float? = null
    val superEffectiveBlocks = mutableListOf<Block>()
    val effectiveBlocks = mutableListOf<Block>()
    val effectiveBlockTags = mutableListOf<TagKey<Block>>()
    var areaMining = false
    var mineAll = false
    var cutAll = false
    var silkTouch = false
    var fortune: Int? = null
    var selfMending: Int? = null
    val descriptions = mutableListOf<Text>()
    var basePower = 0F // TODO -> FairyShootingStaffSettings
    var baseMaxDistance = 0F // TODO -> FairyShootingStaffSettings
    var obtainFairy: Double? = null

    override fun createItem() = creator(this)

    context(ModContext)
    override fun init(card: ToolCard) {
        tags.forEach {
            card.item.registerItemTagGeneration { it }
        }
        card.item.registerItemTagGeneration { toolMaterialCard.tag }
    }

    override fun addPoems(poemList: PoemList) = descriptions.fold(poemList) { it, description -> it.text(PoemType.DESCRIPTION, description) }

}


fun createSword(toolMaterialCard: ToolMaterialCard) = FairyToolSettings({ FairySwordItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = 3.0F
    it.attackSpeed = -2.4F
    it.miningSpeedMultiplierOverride = 1.5F
    it.tags += ItemTags.SWORDS
    it.superEffectiveBlocks += Blocks.COBWEB
    it.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
}

fun createShovel(toolMaterialCard: ToolMaterialCard) = FairyToolSettings({ FairyShovelItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = 1.5F
    it.attackSpeed = -3.0F
    it.tags += ItemTags.SHOVELS
    it.effectiveBlockTags += BlockTags.SHOVEL_MINEABLE
}

fun createPickaxe(toolMaterialCard: ToolMaterialCard) = FairyToolSettings({ FairyPickaxeItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = 1F
    it.attackSpeed = -2.8F
    it.tags += ItemTags.PICKAXES
    it.tags += ItemTags.CLUSTER_MAX_HARVESTABLES
    it.effectiveBlockTags += BlockTags.PICKAXE_MINEABLE
}

/**
 * @param attackDamage wood: 6.0, stone: 7.0, gold: 6.0, iron: 6.0, diamond: 5.0, netherite: 5.0
 * @param attackSpeed wood: -3.2, stone: -3.2, gold: -3.0, iron: -3.1, diamond: -3.0, netherite: -3.0
 */
fun createAxe(toolMaterialCard: ToolMaterialCard, attackDamage: Float, attackSpeed: Float) = FairyToolSettings({ FairyAxeItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = attackDamage
    it.attackSpeed = attackSpeed
    it.tags += ItemTags.AXES
    it.effectiveBlockTags += BlockTags.AXE_MINEABLE
}

// Hoe
// @param attackDamage wood: 0.0, stone: -1.0, gold: 0.0, iron: -2.0, diamond: -3.0, netherite: -4.0
// @param attackSpeed wood: -3.0, stone: -2.0, gold: -3.0, iron: -1.0, diamond: 0.0, netherite: 0.0

fun createKnife(toolMaterialCard: ToolMaterialCard) = FairyToolSettings({ FairyKnifeItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = 2.0F
    it.attackSpeed = -2.4F
    it.tags += ItemTags.SWORDS
    it.superEffectiveBlocks += Blocks.COBWEB
    it.effectiveBlockTags += BlockTags.SWORD_EFFICIENT
}

fun createBattleAxe(toolMaterialCard: ToolMaterialCard, attackDamage: Float, attackSpeed: Float) = FairyToolSettings({ FairyBattleAxeItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.attackDamage = attackDamage
    it.attackSpeed = attackSpeed
    it.tags += ItemTags.AXES
    it.effectiveBlockTags += BlockTags.AXE_MINEABLE
}

fun createShootingStaff(toolMaterialCard: ToolMaterialCard, basePower: Float, baseMaxDistance: Float) = FairyToolSettings({ FairyShootingStaffItem(it, Item.Settings()) }, toolMaterialCard).also {
    it.basePower = basePower
    it.baseMaxDistance = baseMaxDistance
}


fun FairyToolSettings.areaMining() = this.also {
    it.areaMining = true
    it.descriptions += FairyToolSettings.AREA_MINING_TRANSLATION()
}

fun FairyToolSettings.mineAll() = this.also {
    it.mineAll = true
    it.descriptions += FairyToolSettings.MINE_ALL_TRANSLATION()
}

fun FairyToolSettings.cutAll() = this.also {
    it.cutAll = true
    it.descriptions += FairyToolSettings.CUT_ALL_TRANSLATION()
}

fun FairyToolSettings.silkTouch() = this.also {
    it.silkTouch = true
    it.descriptions += FairyToolSettings.SILK_TOUCH_TRANSLATION()
}

fun FairyToolSettings.fortune(fortune: Int) = this.also {
    check(fortune >= 1)
    it.fortune = fortune
    it.descriptions += text { FairyToolSettings.FORTUNE_TRANSLATION() + if (fortune >= 2) " "() + fortune.toRomanText() else ""() }
}

fun FairyToolSettings.selfMending(selfMending: Int) = this.also {
    it.selfMending = selfMending
    it.descriptions += FairyToolSettings.SELF_MENDING_TRANSLATION()
}

fun FairyToolSettings.obtainFairy(appearanceRateBonus: Double) = this.also {
    it.obtainFairy = appearanceRateBonus
    it.descriptions += FairyToolSettings.OBTAIN_FAIRY()
}
