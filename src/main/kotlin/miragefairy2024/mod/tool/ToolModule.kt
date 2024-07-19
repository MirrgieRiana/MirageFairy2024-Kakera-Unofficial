package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.ToolMaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.enJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.data.client.Models
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

fun initToolModule() = ModEvents.onInitialize {
    ToolCard.entries.forEach {
        it.init()
    }

    FairyMiningToolType.CUT_ALL_TRANSLATION.enJa()
}


interface ToolType<I : Item> {
    fun createItem(): I
    fun init(card: ToolCard<I>) = Unit
    fun addPoems(poemList: PoemList) = poemList
}

class ToolCard<I : Item>(
    path: String,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    private val tier: Int,
    private val type: ToolType<I>,
    private val initializer: ToolCard<I>.() -> Unit = {},
) {
    val identifier = Identifier(MirageFairy2024.modId, path)
    val item = type.createItem()

    fun init() {
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerItemModelGeneration(Models.HANDHELD)

        item.enJa(enName, jaName)

        val poemList = PoemList(tier).poem(enPoem, jaPoem).let { type.addPoems(it) }
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        type.init(this)
        initializer(this)
    }

    @Suppress("unused")
    companion object {
        val entries = mutableListOf<ToolCard<*>>()
        private fun <I : Item> ToolCard<I>.register() = this.also { entries.add(this) }

        val FAIRY_CRYSTAL_PICKAXE = ToolCard(
            "fairy_crystal_pickaxe", "Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし",
            "A brain frozen in crystal", "闇を打ち砕く透明な心。",
            2, FairyMiningToolType.pickaxe(ToolMaterialCard.FAIRY_CRYSTAL), // TODO 特殊能力
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.FAIRY_CRYSTAL.item)
                input('R', Items.STICK)
            } on MaterialCard.FAIRY_CRYSTAL.item
        }.register()
        val MIRAGIUM_PICKAXE = ToolCard(
            "miragium_pickaxe", "Miragium Pickaxe", "ミラジウムのつるはし",
            "More durable than gold", "妖精の肉体労働",
            3, FairyMiningToolType.pickaxe(ToolMaterialCard.MIRAGIUM), // TODO 特殊能力
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.MIRAGIUM_INGOT.item)
                input('R', Items.STICK)
            } on MaterialCard.MIRAGIUM_INGOT.item
        }.register()
        val MIRAGIUM_AXE = ToolCard(
            "miragium_axe", "Miragium Axe", "ミラジウムの斧",
            "Crack! Squish!", "バキッ！ぐにっ",
            3, FairyMiningToolType.axe(ToolMaterialCard.MIRAGIUM, 5.0F, -3.0F).cutAll(),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("##")
                pattern("#R")
                pattern(" R")
                input('#', MaterialCard.MIRAGIUM_INGOT.item)
                input('R', Items.STICK)
            } on MaterialCard.MIRAGIUM_INGOT.item
        }.register()
        val MIRANAGITE_PICKAXE = ToolCard(
            "miranagite_pickaxe", "Miranagite Pickaxe", "蒼天石のつるはし",
            "Promotes ore recrystallization", "凝集する秩序、蒼穹彩煌が如く。",
            2, FairyMiningToolType.pickaxe(ToolMaterialCard.MIRANAGITE), // TODO 特殊能力
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.MIRANAGITE.item)
                input('R', Items.STICK)
            } on MaterialCard.MIRANAGITE.item
        }.register()
        val XARPITE_PICKAXE = ToolCard(
            "xarpite_pickaxe", "Xarpite Pickaxe", "紅天石のつるはし",
            "Shears space using astral induction", "鉱石の魂を貪る血塗られた有機質。",
            2, FairyMiningToolType.pickaxe(ToolMaterialCard.XARPITE), // TODO 特殊能力
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.XARPITE.item)
                input('R', Items.STICK)
            } on MaterialCard.XARPITE.item
        }.register()
        val XARPITE_AXE = ToolCard(
            "xarpite_axe", "Xarpite Axe", "紅天石の斧",
            "Strip the log from the space", "空間にこびりついた丸太の除去に。",
            2, FairyMiningToolType.axe(ToolMaterialCard.XARPITE, 6.0F, -3.1F).cutAll(),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("##")
                pattern("#R")
                pattern(" R")
                input('#', MaterialCard.XARPITE.item)
                input('R', Items.STICK)
            } on MaterialCard.XARPITE.item
        }.register()
        val CHAOS_STONE_PICKAXE = ToolCard(
            "chaos_stone_pickaxe", "Chaos Stone Pickaxe", "混沌のつるはし",
            "Is this made of metal? Or clay?", "時空結晶の交点に、古代の産業が芽吹く。",
            4, FairyMiningToolType.pickaxe(ToolMaterialCard.CHAOS_STONE), // TODO 特殊能力
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.CHAOS_STONE.item)
                input('R', Items.STICK)
            } on MaterialCard.CHAOS_STONE.item
        }.register()
    }
}
