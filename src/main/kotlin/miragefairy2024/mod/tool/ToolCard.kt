package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tool.items.FairyAxeConfiguration
import miragefairy2024.mod.tool.items.FairyBattleAxeConfiguration
import miragefairy2024.mod.tool.items.FairyHoeConfiguration
import miragefairy2024.mod.tool.items.FairyKnifeConfiguration
import miragefairy2024.mod.tool.items.FairyPickaxeConfiguration
import miragefairy2024.mod.tool.items.FairyScytheConfiguration
import miragefairy2024.mod.tool.items.FairyShootingStaffConfiguration
import miragefairy2024.mod.tool.items.FairyShovelConfiguration
import miragefairy2024.mod.tool.items.FairySwordConfiguration
import miragefairy2024.mod.tool.items.ScytheItem
import miragefairy2024.mod.tool.items.ShootingStaffItem
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.data.client.Models
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags

context(ModContext)
fun initToolCard() {

    ScytheItem.DESCRIPTION_TRANSLATION.enJa()
    ShootingStaffItem.NOT_ENOUGH_EXPERIENCE_TRANSLATION.enJa()
    ShootingStaffItem.DESCRIPTION_TRANSLATION.enJa()

    ToolCard.entries.forEach {
        it.init()
    }

}

class ToolCard(
    path: String,
    private val name: EnJa,
    private val poemList: PoemList,
    private val configuration: ToolConfiguration,
    private val initializer: context(ModContext)ToolCard.() -> Unit = {},
) {
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    companion object {
        val entries = mutableListOf<ToolCard>()
        private operator fun ToolCard.not() = this.also { entries.add(this) }

        val IRON_SCYTHE = !ToolCard(
            "iron_scythe", EnJa("Iron Scythe", "鉄の大鎌"),
            PoemList(null),
            FairyScytheConfiguration(ToolMaterialCard.IRON, 1),
        ) { registerScytheRecipeGeneration(item, Items.IRON_INGOT) }
        val DIAMOND_SCYTHE = !ToolCard(
            "diamond_scythe", EnJa("Diamond Scythe", "ダイヤモンドの大鎌"),
            PoemList(null),
            FairyScytheConfiguration(ToolMaterialCard.DIAMOND, 3),
        ) { registerScytheRecipeGeneration(item, Items.DIAMOND) }

        val MAGNETITE_PICKAXE = !ToolCard(
            "magnetite_pickaxe", EnJa("Magnetite Pickaxe", "磁鉄鉱のつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.MAGNETITE).collection(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MAGNETITE.item) }
        val MAGNETITE_AXE = !ToolCard(
            "magnetite_axe", EnJa("Magnetite Axe", "磁鉄鉱の斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.MAGNETITE, 6.5F, -3.2F).collection(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MAGNETITE.item) }
        val MAGNETITE_SHOVEL = !ToolCard(
            "magnetite_shovel", EnJa("Magnetite Shovel", "磁鉄鉱のシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.MAGNETITE).collection(),
        ) { registerShovelRecipeGeneration(item, MaterialCard.MAGNETITE.item) }
        val MAGNETITE_HOE = !ToolCard(
            "magnetite_hoe", EnJa("Magnetite Hoe", "磁鉄鉱のクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.MAGNETITE, -1, -2.0F).collection(),
        ) { registerHoeRecipeGeneration(item, MaterialCard.MAGNETITE.item) }
        val MAGNETITE_SWORD = !ToolCard(
            "magnetite_sword", EnJa("Magnetite Sword", "磁鉄鉱の剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.MAGNETITE).collection(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.MAGNETITE.item) }
        val COPPER_PICKAXE = !ToolCard(
            "copper_pickaxe", EnJa("Copper Pickaxe", "銅のつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.COPPER),
        ) { registerPickaxeRecipeGeneration(item, Items.COPPER_INGOT) }
        val COPPER_AXE = !ToolCard(
            "copper_axe", EnJa("Copper Axe", "銅の斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.COPPER, 6.5F, -3.2F),
        ) { registerAxeRecipeGeneration(item, Items.COPPER_INGOT) }
        val COPPER_SHOVEL = !ToolCard(
            "copper_shovel", EnJa("Copper Shovel", "銅のシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.COPPER),
        ) { registerShovelRecipeGeneration(item, Items.COPPER_INGOT) }
        val COPPER_HOE = !ToolCard(
            "copper_hoe", EnJa("Copper Hoe", "銅のクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.COPPER, -2, -1.5F),
        ) { registerHoeRecipeGeneration(item, Items.COPPER_INGOT) }
        val COPPER_SWORD = !ToolCard(
            "copper_sword", EnJa("Copper Sword", "銅の剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.COPPER),
        ) { registerSwordRecipeGeneration(item, Items.COPPER_INGOT) }
        val FLUORITE_PICKAXE = !ToolCard(
            "fluorite_pickaxe", EnJa("Fluorite Pickaxe", "蛍石のつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.FLUORITE).enchantment(Enchantments.FORTUNE, 1),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.FLUORITE.item) }
        val FLUORITE_AXE = !ToolCard(
            "fluorite_axe", EnJa("Fluorite Axe", "蛍石の斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.FLUORITE, 6.5F, -3.2F).enchantment(Enchantments.FORTUNE, 1),
        ) { registerAxeRecipeGeneration(item, MaterialCard.FLUORITE.item) }
        val FLUORITE_SHOVEL = !ToolCard(
            "fluorite_shovel", EnJa("Fluorite Shovel", "蛍石のシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.FLUORITE).enchantment(Enchantments.FORTUNE, 1),
        ) { registerShovelRecipeGeneration(item, MaterialCard.FLUORITE.item) }
        val FLUORITE_HOE = !ToolCard(
            "fluorite_hoe", EnJa("Fluorite Hoe", "蛍石のクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.FLUORITE, -1, -2.0F).enchantment(Enchantments.FORTUNE, 1),
        ) { registerHoeRecipeGeneration(item, MaterialCard.FLUORITE.item) }
        val FLUORITE_SWORD = !ToolCard(
            "fluorite_sword", EnJa("Fluorite Sword", "蛍石の剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.FLUORITE).enchantment(Enchantments.LOOTING, 1),
        ) { registerSwordRecipeGeneration(item, MaterialCard.FLUORITE.item) }
        val EMERALD_PICKAXE = !ToolCard(
            "emerald_pickaxe", EnJa("Emerald Pickaxe", "エメラルドのつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.EMERALD).enchantment(Enchantments.FORTUNE, 2),
        ) { registerPickaxeRecipeGeneration(item, Items.EMERALD) }
        val EMERALD_AXE = !ToolCard(
            "emerald_axe", EnJa("Emerald Axe", "エメラルドの斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.EMERALD, 5.5F, -3.0F).enchantment(Enchantments.FORTUNE, 2),
        ) { registerAxeRecipeGeneration(item, Items.EMERALD) }
        val EMERALD_SHOVEL = !ToolCard(
            "emerald_shovel", EnJa("Emerald Shovel", "エメラルドのシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.EMERALD).enchantment(Enchantments.FORTUNE, 2),
        ) { registerShovelRecipeGeneration(item, Items.EMERALD) }
        val EMERALD_HOE = !ToolCard(
            "emerald_hoe", EnJa("Emerald Hoe", "エメラルドのクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.EMERALD, -3, 0.0F).enchantment(Enchantments.FORTUNE, 2),
        ) { registerHoeRecipeGeneration(item, Items.EMERALD) }
        val EMERALD_SWORD = !ToolCard(
            "emerald_sword", EnJa("Emerald Sword", "エメラルドの剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.EMERALD).enchantment(Enchantments.LOOTING, 2),
        ) { registerSwordRecipeGeneration(item, Items.EMERALD) }

        val FAIRY_CRYSTAL_PICKAXE = !ToolCard(
            "fairy_crystal_pickaxe", EnJa("Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし"),
            PoemList(2).poem(EnJa("A brain frozen in crystal", "闇を打ち砕く、透き通る心。")),
            FairyPickaxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_SCYTHE = !ToolCard(
            "fairy_crystal_scythe", EnJa("Fairy Crystal Scythe", "フェアリークリスタルの大鎌"),
            PoemList(2).poem(EnJa("What color is fairy blood?", "妖精を刈り取るための道具。")),
            FairyScytheConfiguration(ToolMaterialCard.FAIRY_CRYSTAL, 2).selfMending(10).obtainFairy(9.0),
        ) { registerScytheRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_SWORD = !ToolCard(
            "fairy_crystal_sword", EnJa("Fairy Crystal Sword", "フェアリークリスタルの剣"),
            PoemList(2).poem(EnJa("Nutrients for the soul", "妖精はこれをおやつにするという")),
            FairySwordConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
        ) { registerSwordRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_BATTLE_AXE = !ToolCard(
            "fairy_crystal_battle_axe", EnJa("Fairy Crystal Battle Axe", "フェアリークリスタルの戦斧"),
            PoemList(2).poem(EnJa("The embodiment of fighting spirit", "妖精の本能を呼び覚ませ。")),
            FairyBattleAxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL, 6.5F, -3.0F).selfMending(10).obtainFairy(9.0),
        ) { registerBattleAxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val MIRAGIUM_PICKAXE = !ToolCard(
            "miragium_pickaxe", EnJa("Miragium Pickaxe", "ミラジウムのつるはし"),
            PoemList(3).poem(EnJa("More durable than gold", "妖精の肉体労働")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIUM).selfMending(20).mineAll(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.item) }
        val MIRAGIUM_AXE = !ToolCard(
            "miragium_axe", EnJa("Miragium Axe", "ミラジウムの斧"),
            PoemList(3).poem(EnJa("Crack! Squish!", "バキッ！ぐにっ")),
            FairyAxeConfiguration(ToolMaterialCard.MIRAGIUM, 5.0F, -3.0F).selfMending(20).cutAll(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.item) }
        val MIRANAGITE_KNIFE = !ToolCard(
            "miranagite_knife", EnJa("Miranagite Knife", "蒼天石のナイフ"),
            PoemList(2).poem(EnJa("Gardener's tool invented by Miranagi", "大自然を駆ける探究者のナイフ。")),
            FairyKnifeConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
        ) { registerKnifeRecipeGeneration(item, MaterialCard.MIRANAGITE.item) }
        val MIRANAGITE_PICKAXE = !ToolCard(
            "miranagite_pickaxe", EnJa("Miranagite Pickaxe", "蒼天石のつるはし"),
            PoemList(2).poem(EnJa("Promotes ore recrystallization", "凝集する秩序、蒼穹彩煌が如く。")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRANAGITE.item) }
        val MIRANAGITE_SCYTHE = !ToolCard(
            "miranagite_scythe", EnJa("Miranagite Scythe", "蒼天石の大鎌"),
            PoemList(2).poem(EnJa("Releases the souls of weeds", "宙を切り裂く創世の刃、草魂を蒼天へ導く。")),
            FairyScytheConfiguration(ToolMaterialCard.MIRANAGITE, 3).enchantment(Enchantments.SILK_TOUCH),
        ) { registerScytheRecipeGeneration(item, MaterialCard.MIRANAGITE.item) }
        val MIRANAGI_STAFF_0 = !ToolCard(
            "miranagi_staff_0", EnJa("Miranagite Staff", "蒼天石のスタッフ"),
            PoemList(2).poem(EnJa("Inflating anti-entropy force", "膨張する秩序の力。")),
            FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 5F, 12F).enchantment(Enchantments.SILK_TOUCH),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" IG")
                pattern(" RI")
                pattern("I  ")
                input('R', MaterialCard.MIRANAGITE_ROD.item)
                input('G', Items.GLASS)
                input('I', Items.COPPER_INGOT)
            } on MaterialCard.MIRANAGITE.item
        }
        val MIRANAGI_STAFF = !ToolCard(
            "miranagi_staff", EnJa("Staff of Miranagi", "みらなぎの杖"),
            PoemList(3).poem(EnJa("Risk of vacuum decay due to anti-entropy", "創世の神光は混沌をも翻す。")),
            FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 7F, 16F).enchantment(Enchantments.SILK_TOUCH),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" IG")
                pattern(" #I")
                pattern("N  ")
                input('#', MIRANAGI_STAFF_0.item)
                input('G', Items.DIAMOND)
                input('I', Items.IRON_INGOT)
                input('N', Items.IRON_NUGGET)
            } on MaterialCard.MIRANAGITE.item
        }
        val XARPITE_PICKAXE = !ToolCard(
            "xarpite_pickaxe", EnJa("Xarpite Pickaxe", "紅天石のつるはし"),
            PoemList(2).poem(EnJa("Shears space using astral induction", "鉱石の魂を貪る血塗られた有機質。")),
            FairyPickaxeConfiguration(ToolMaterialCard.XARPITE).mineAll(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.XARPITE.item) }
        val XARPITE_AXE = !ToolCard(
            "xarpite_axe", EnJa("Xarpite Axe", "紅天石の斧"),
            PoemList(2).poem(EnJa("Strip the log from the space", "空間にこびりついた丸太の除去に。")),
            FairyAxeConfiguration(ToolMaterialCard.XARPITE, 6.0F, -3.1F).cutAll(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.XARPITE.item) }
        val CHAOS_STONE_PICKAXE = !ToolCard(
            "chaos_stone_pickaxe", EnJa("Chaos Stone Pickaxe", "混沌のつるはし"),
            PoemList(4).poem(EnJa("Is this made of metal? Or clay?", "時空結晶の交点に、古代の産業が芽吹く。")),
            FairyPickaxeConfiguration(ToolMaterialCard.CHAOS_STONE).also { it.effectiveBlockTags += BlockTags.SHOVEL_MINEABLE }.areaMining(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.CHAOS_STONE.item) }
        val PHANTOM_PICKAXE = !ToolCard(
            "phantom_pickaxe", EnJa("Phantom Pickaxe", "幻想のつるはし"),
            PoemList(4).poem(EnJa("\"Creation\" is the true power.", "人間が手にした唯一の幻想。")),
            FairyPickaxeConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.PHANTOM_DROP.item) }
        val PHANTOM_SHOVEL = !ToolCard(
            "phantom_shovel", EnJa("Phantom Shovel", "幻想のシャベル"),
            PoemList(4).poem(EnJa("The sound of the world's end echoed", "破壊された世界の音――")),
            FairyShovelConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerShovelRecipeGeneration(item, MaterialCard.PHANTOM_DROP.item) }
        val PHANTOM_SWORD = !ToolCard(
            "phantom_sword", EnJa("Phantom Sword", "幻想の剣"),
            PoemList(4).poem(EnJa("Pray. For rebirth.", "闇を切り裂く、再生の光。")),
            FairySwordConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) { registerSwordRecipeGeneration(item, MaterialCard.PHANTOM_DROP.item) }
        val HAIMEVISKA_ROSIN_PICKAXE = !ToolCard(
            "haimeviska_rosin_pickaxe", EnJa("Rosin Pickaxe", "涙のつるはし"),
            PoemList(2).poem(EnJa("A pickaxe looking for companions", "きみも一緒だよ――")),
            FairyPickaxeConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining().collection().enchantment(Enchantments.UNBREAKING, 2),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.item) }
        val HAIMEVISKA_ROSIN_SHOVEL = !ToolCard(
            "haimeviska_rosin_shovel", EnJa("Rosin Shovel", "涙のシャベル"),
            PoemList(2).poem(EnJa("The oppressed Viska spirit's desire", "傷つけられた樹の声")),
            FairyShovelConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining().collection().enchantment(Enchantments.UNBREAKING, 2),
        ) { registerShovelRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.item) }
        val HAIMEVISKA_ROSIN_SWORD = !ToolCard(
            "haimeviska_rosin_sword", EnJa("Rosin Sword", "涙の剣"),
            PoemList(2).poem(EnJa("The story of the lonely tree", "涙の中に消えた友――")),
            FairySwordConfiguration(ToolMaterialCard.HAIMEVISKA_ROSIN).areaMining().collection().enchantment(Enchantments.UNBREAKING, 2),
        ) { registerSwordRecipeGeneration(item, MaterialCard.HAIMEVISKA_ROSIN.item) }
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = configuration.createItem()

    context(ModContext)
    fun init() {
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerModelGeneration(Models.HANDHELD)

        item.enJa(name)

        val poemList2 = configuration.appendPoems(poemList)
        item.registerPoem(poemList2)
        item.registerPoemGeneration(poemList2)

        configuration.init(this)
        initializer(this@ModContext, this)
    }
}

context(ModContext)
private fun registerPickaxeRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("###")
    pattern(" R ")
    pattern(" R ")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerAxeRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("##")
    pattern("#R")
    pattern(" R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerShovelRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("R")
    pattern("R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerHoeRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("##")
    pattern(" R")
    pattern(" R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerKnifeRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerScytheRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern(" ##")
    pattern("# R")
    pattern("  R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerSwordRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("#")
    pattern("#")
    pattern("R")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem

context(ModContext)
private fun registerBattleAxeRecipeGeneration(item: Item, inputItem: Item) = registerShapedRecipeGeneration(item) {
    pattern("###")
    pattern("#R#")
    pattern(" R ")
    input('#', inputItem)
    input('R', Items.STICK)
} on inputItem
