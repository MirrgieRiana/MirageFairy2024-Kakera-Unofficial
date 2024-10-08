package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tool.contents.FairyAxeConfiguration
import miragefairy2024.mod.tool.contents.FairyBattleAxeConfiguration
import miragefairy2024.mod.tool.contents.FairyKnifeConfiguration
import miragefairy2024.mod.tool.contents.FairyPickaxeConfiguration
import miragefairy2024.mod.tool.contents.FairyScytheConfiguration
import miragefairy2024.mod.tool.contents.FairyShootingStaffConfiguration
import miragefairy2024.mod.tool.contents.FairyShovelConfiguration
import miragefairy2024.mod.tool.contents.FairySwordConfiguration
import miragefairy2024.mod.tool.contents.ScytheItem
import miragefairy2024.mod.tool.contents.ShootingStaffItem
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import net.minecraft.data.client.Models
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags

context(ModContext)
fun initToolCardModule() {

    ToolConfiguration.AREA_MINING_TRANSLATION.enJa()
    ToolConfiguration.MINE_ALL_TRANSLATION.enJa()
    ToolConfiguration.CUT_ALL_TRANSLATION.enJa()
    ToolConfiguration.SILK_TOUCH_TRANSLATION.enJa()
    ToolConfiguration.FORTUNE_TRANSLATION.enJa()
    ToolConfiguration.SELF_MENDING_TRANSLATION.enJa()
    ToolConfiguration.OBTAIN_FAIRY.enJa()

    ScytheItem.DESCRIPTION_TRANSLATION.enJa()
    ShootingStaffItem.NOT_ENOUGH_EXPERIENCE_TRANSLATION.enJa()
    ShootingStaffItem.DESCRIPTION_TRANSLATION.enJa()

    ToolCards.entries.forEach {
        it.init()
    }

}

class ToolCard(
    path: String,
    private val tier: Int?,
    private val name: EnJa,
    private val poem: EnJa?,
    private val configuration: ToolConfiguration,
    private val initializer: context(ModContext)ToolCard.() -> Unit = {},
) {
    val identifier = MirageFairy2024.identifier(path)
    val item = configuration.createItem()

    context(ModContext)
    fun init() {
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerModelGeneration(Models.HANDHELD)

        item.enJa(name)

        val poemList = PoemList(tier).let { if (poem != null) it.poem(poem) else it }.let { configuration.appendPoems(it) }
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        configuration.init(this)
        initializer(this@ModContext, this)
    }
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
object ToolCards {
    val entries = mutableListOf<ToolCard>()
    private operator fun ToolCard.not() = this.also { entries.add(this) }

    val IRON_SCYTHE = !ToolCard(
        "iron_scythe", null, EnJa("Iron Scythe", "鉄の大鎌"),
        null,
        FairyScytheConfiguration(ToolMaterialCard.IRON, 1),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern(" ##")
            pattern("# R")
            pattern("  R")
            input('#', Items.IRON_INGOT)
            input('R', Items.STICK)
        } on Items.IRON_INGOT
    } // TODO texture
    val FAIRY_CRYSTAL_PICKAXE = !ToolCard(
        "fairy_crystal_pickaxe", 2, EnJa("Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし"),
        EnJa("A brain frozen in crystal", "闇を打ち砕く、透き通る心。"),
        FairyPickaxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.FAIRY_CRYSTAL.item)
            input('R', Items.STICK)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
    val FAIRY_CRYSTAL_SCYTHE = !ToolCard(
        "fairy_crystal_scythe", 2, EnJa("Fairy Crystal Scythe", "フェアリークリスタルの大鎌"),
        EnJa("What color is fairy blood?", "妖精を刈り取るための道具。"),
        FairyScytheConfiguration(ToolMaterialCard.FAIRY_CRYSTAL, 2).selfMending(10).obtainFairy(9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern(" ##")
            pattern("# R")
            pattern("  R")
            input('#', MaterialCard.FAIRY_CRYSTAL.item)
            input('R', MaterialCard.MIRAGE_STEM.item)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
    val FAIRY_CRYSTAL_SWORD = !ToolCard(
        "fairy_crystal_sword", 2, EnJa("Fairy Crystal Sword", "フェアリークリスタルの剣"),
        EnJa("Nutrients for the soul", "妖精はこれをおやつにするという"),
        FairySwordConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("#")
            pattern("R")
            input('#', MaterialCard.FAIRY_CRYSTAL.item)
            input('R', Items.STICK)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
    val FAIRY_CRYSTAL_BATTLE_AXE = !ToolCard(
        "fairy_crystal_battle_axe", 2, EnJa("Fairy Crystal Battle Axe", "フェアリークリスタルの戦斧"),
        EnJa("The embodiment of fighting spirit", "妖精の本能を呼び覚ませ。"),
        FairyBattleAxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL, 6.5F, -3.0F).selfMending(10).obtainFairy(9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern("#R#")
            pattern(" R ")
            input('#', MaterialCard.FAIRY_CRYSTAL.item)
            input('R', Items.STICK)
        } on MaterialCard.FAIRY_CRYSTAL.item
    }
    val MIRAGIUM_PICKAXE = !ToolCard(
        "miragium_pickaxe", 3, EnJa("Miragium Pickaxe", "ミラジウムのつるはし"),
        EnJa("More durable than gold", "妖精の肉体労働"),
        FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIUM).selfMending(20).mineAll(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.MIRAGIUM_INGOT.item)
            input('R', Items.STICK)
        } on MaterialCard.MIRAGIUM_INGOT.item
    }
    val MIRAGIUM_AXE = !ToolCard(
        "miragium_axe", 3, EnJa("Miragium Axe", "ミラジウムの斧"),
        EnJa("Crack! Squish!", "バキッ！ぐにっ"),
        FairyAxeConfiguration(ToolMaterialCard.MIRAGIUM, 5.0F, -3.0F).selfMending(20).cutAll(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("##")
            pattern("#R")
            pattern(" R")
            input('#', MaterialCard.MIRAGIUM_INGOT.item)
            input('R', Items.STICK)
        } on MaterialCard.MIRAGIUM_INGOT.item
    }
    val MIRANAGITE_KNIFE = !ToolCard(
        "miranagite_knife", 2, EnJa("Miranagite Knife", "蒼天石のナイフ"),
        EnJa("Gardener's tool invented by Miranagi", "大自然を駆ける探究者のナイフ。"),
        FairyKnifeConfiguration(ToolMaterialCard.MIRANAGITE).silkTouch(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("R")
            input('#', MaterialCard.MIRANAGITE.item)
            input('R', Items.STICK)
        } on MaterialCard.MIRANAGITE.item
    }
    val MIRANAGITE_PICKAXE = !ToolCard(
        "miranagite_pickaxe", 2, EnJa("Miranagite Pickaxe", "蒼天石のつるはし"),
        EnJa("Promotes ore recrystallization", "凝集する秩序、蒼穹彩煌が如く。"),
        FairyPickaxeConfiguration(ToolMaterialCard.MIRANAGITE).silkTouch(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.MIRANAGITE.item)
            input('R', Items.STICK)
        } on MaterialCard.MIRANAGITE.item
    }
    val MIRANAGITE_SCYTHE = !ToolCard(
        "miranagite_scythe", 2, EnJa("Miranagite Scythe", "蒼天石の大鎌"),
        EnJa("Releases the souls of weeds", "宙を切り裂く創世の刃、草魂を蒼天へ導く。"),
        FairyScytheConfiguration(ToolMaterialCard.MIRANAGITE, 3).silkTouch(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern(" ##")
            pattern("# R")
            pattern("  R")
            input('#', MaterialCard.MIRANAGITE.item)
            input('R', MaterialCard.MIRAGE_STEM.item)
        } on MaterialCard.MIRANAGITE.item
    }
    val MIRANAGI_STAFF_0 = !ToolCard(
        "miranagi_staff_0", 2, EnJa("Miranagite Staff", "蒼天石のスタッフ"),
        EnJa("Inflating anti-entropy force", "膨張する秩序の力。"),
        FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 7F, 12F).silkTouch(),
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
        "miranagi_staff", 3, EnJa("Staff of Miranagi", "みらなぎの杖"),
        EnJa("Risk of vacuum decay due to anti-entropy", "創世の神光は混沌をも翻す。"),
        FairyShootingStaffConfiguration(ToolMaterialCard.MIRANAGITE, 10F, 16F).silkTouch(),
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
        "xarpite_pickaxe", 2, EnJa("Xarpite Pickaxe", "紅天石のつるはし"),
        EnJa("Shears space using astral induction", "鉱石の魂を貪る血塗られた有機質。"),
        FairyPickaxeConfiguration(ToolMaterialCard.XARPITE).mineAll(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.XARPITE.item)
            input('R', Items.STICK)
        } on MaterialCard.XARPITE.item
    }
    val XARPITE_AXE = !ToolCard(
        "xarpite_axe", 2, EnJa("Xarpite Axe", "紅天石の斧"),
        EnJa("Strip the log from the space", "空間にこびりついた丸太の除去に。"),
        FairyAxeConfiguration(ToolMaterialCard.XARPITE, 6.0F, -3.1F).cutAll(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("##")
            pattern("#R")
            pattern(" R")
            input('#', MaterialCard.XARPITE.item)
            input('R', Items.STICK)
        } on MaterialCard.XARPITE.item
    }
    val DIAMOND_SCYTHE = !ToolCard(
        "diamond_scythe", null, EnJa("Diamond Scythe", "ダイヤモンドの大鎌"),
        null,
        FairyScytheConfiguration(ToolMaterialCard.DIAMOND, 3),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern(" ##")
            pattern("# R")
            pattern("  R")
            input('#', Items.DIAMOND)
            input('R', Items.STICK)
        } on Items.DIAMOND
    } // TODO texture
    val CHAOS_STONE_PICKAXE = !ToolCard(
        "chaos_stone_pickaxe", 4, EnJa("Chaos Stone Pickaxe", "混沌のつるはし"),
        EnJa("Is this made of metal? Or clay?", "時空結晶の交点に、古代の産業が芽吹く。"),
        FairyPickaxeConfiguration(ToolMaterialCard.CHAOS_STONE).also { it.effectiveBlockTags += BlockTags.SHOVEL_MINEABLE }.areaMining(),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.CHAOS_STONE.item)
            input('R', Items.STICK)
        } on MaterialCard.CHAOS_STONE.item
    }
    val PHANTOM_PICKAXE = !ToolCard(
        "phantom_pickaxe", 4, EnJa("Phantom Pickaxe", "幻想のつるはし"),
        EnJa("\"Creation\" is the true power.", "人間が手にした唯一の幻想。"),
        FairyPickaxeConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("###")
            pattern(" R ")
            pattern(" R ")
            input('#', MaterialCard.PHANTOM_DROP.item)
            input('R', Items.STICK)
        } on MaterialCard.PHANTOM_DROP.item
    }
    val PHANTOM_SHOVEL = !ToolCard(
        "phantom_shovel", 4, EnJa("Phantom Shovel", "幻想のシャベル"),
        EnJa("The sound of the world's end echoed", "破壊された世界の音――"),
        FairyShovelConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("R")
            pattern("R")
            input('#', MaterialCard.PHANTOM_DROP.item)
            input('R', Items.STICK)
        } on MaterialCard.PHANTOM_DROP.item
    }
    val PHANTOM_SWORD = !ToolCard(
        "phantom_sword", 4, EnJa("Phantom Sword", "幻想の剣"),
        EnJa("Pray. For rebirth.", "闇を切り裂く、再生の光。"),
        FairySwordConfiguration(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
    ) {
        registerShapedRecipeGeneration(item) {
            pattern("#")
            pattern("#")
            pattern("R")
            input('#', MaterialCard.PHANTOM_DROP.item)
            input('R', Items.STICK)
        } on MaterialCard.PHANTOM_DROP.item
    }
}
