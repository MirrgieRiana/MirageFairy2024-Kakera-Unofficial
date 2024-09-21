package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.en
import miragefairy2024.util.enJa
import miragefairy2024.util.ja
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerDamageTypeTagGeneration
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.with
import net.minecraft.data.client.Models
import net.minecraft.entity.damage.DamageType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.DamageTypeTags

object MagicDamageTypeCard {
    val identifier = MirageFairy2024.identifier("magic")
    val registryKey = RegistryKeys.DAMAGE_TYPE with identifier
    val damageType = DamageType(identifier.toTranslationKey(), 0.1F)
}

context(ModContext)
fun initToolModule() {
    ToolCard.entries.forEach {
        it.init()
    }

    FairyToolSettings.AREA_MINING_TRANSLATION.enJa()
    FairyToolSettings.MINE_ALL_TRANSLATION.enJa()
    FairyToolSettings.CUT_ALL_TRANSLATION.enJa()
    FairyToolSettings.SILK_TOUCH_TRANSLATION.enJa()
    FairyToolSettings.SELF_MENDING_TRANSLATION.enJa()
    FairyToolSettings.OBTAIN_FAIRY.enJa()

    MagicDamageTypeCard.let { card ->
        registerDynamicGeneration(card.registryKey) {
            card.damageType
        }
        en { card.identifier.toTranslationKey("death.attack") to "%1\$s was killed by magic" }
        ja { card.identifier.toTranslationKey("death.attack") to "%1\$sは魔法で殺された" }
        en { card.identifier.toTranslationKey("death.attack", "player") to "%1\$s was killed by magic whilst trying to escape %2\$s" }
        ja { card.identifier.toTranslationKey("death.attack", "player") to "%1\$sは%2\$sとの戦闘中に魔法で殺された" }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.IS_PROJECTILE }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.BYPASSES_ARMOR }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.WITCH_RESISTANT_TO }
        card.identifier.registerDamageTypeTagGeneration { DamageTypeTags.AVOIDS_GUARDIAN_THORNS }
    }

    initToolMaterialModule()
}


interface ToolSettings {
    fun createItem(): Item
    context(ModContext)
    fun init(card: ToolCard) = Unit

    fun addPoems(poemList: PoemList) = poemList
}

class ToolCard(
    path: String,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    private val tier: Int,
    private val toolSettings: ToolSettings,
    private val initializer: context(ModContext)ToolCard.() -> Unit = {},
) {
    val identifier = MirageFairy2024.identifier(path)
    val item = toolSettings.createItem()

    context(ModContext)
    fun init() {
        item.register(Registries.ITEM, identifier)

        item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        item.registerModelGeneration(Models.HANDHELD)

        item.enJa(enName, jaName)

        val poemList = PoemList(tier).poem(enPoem, jaPoem).let { toolSettings.addPoems(it) }
        item.registerPoem(poemList)
        item.registerPoemGeneration(poemList)

        toolSettings.init(this)
        initializer(this@ModContext, this)
    }

    @Suppress("unused")
    companion object {
        val entries = mutableListOf<ToolCard>()
        private fun ToolCard.register() = this.also { entries.add(this) }

        val FAIRY_CRYSTAL_PICKAXE = ToolCard(
            "fairy_crystal_pickaxe", "Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし",
            "A brain frozen in crystal", "闇を打ち砕く、透き通る心。",
            2, createPickaxe(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.FAIRY_CRYSTAL.item)
                input('R', Items.STICK)
            } on MaterialCard.FAIRY_CRYSTAL.item
        }.register()
        val FAIRY_CRYSTAL_SWORD = ToolCard(
            "fairy_crystal_sword", "Fairy Crystal Sword", "フェアリークリスタルの剣",
            "Nutrients for the soul", "妖精はこれをおやつにするという",
            2, createSword(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("#")
                pattern("#")
                pattern("R")
                input('#', MaterialCard.FAIRY_CRYSTAL.item)
                input('R', Items.STICK)
            } on MaterialCard.FAIRY_CRYSTAL.item
        }.register()
        val FAIRY_CRYSTAL_BATTLE_AXE = ToolCard(
            "fairy_crystal_battle_axe", "Fairy Crystal Battle Axe", "フェアリークリスタルの戦斧",
            "The embodiment of fighting spirit", "妖精の本能を呼び覚ませ。",
            2, createBattleAxe(ToolMaterialCard.FAIRY_CRYSTAL, 6.5F, -3.0F).selfMending(10).obtainFairy(9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern("#R#")
                pattern(" R ")
                input('#', MaterialCard.FAIRY_CRYSTAL.item)
                input('R', Items.STICK)
            } on MaterialCard.FAIRY_CRYSTAL.item
        }.register()
        val MIRAGIUM_PICKAXE = ToolCard(
            "miragium_pickaxe", "Miragium Pickaxe", "ミラジウムのつるはし",
            "More durable than gold", "妖精の肉体労働",
            3, createPickaxe(ToolMaterialCard.MIRAGIUM).selfMending(20).mineAll(),
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
            3, createAxe(ToolMaterialCard.MIRAGIUM, 5.0F, -3.0F).selfMending(20).cutAll(),
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
            2, createPickaxe(ToolMaterialCard.MIRANAGITE).silkTouch(),
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
            2, createPickaxe(ToolMaterialCard.XARPITE).mineAll(),
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
            2, createAxe(ToolMaterialCard.XARPITE, 6.0F, -3.1F).cutAll(),
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
            4, createPickaxe(ToolMaterialCard.CHAOS_STONE).also { it.effectiveBlockTags += BlockTags.SHOVEL_MINEABLE }.areaMining(),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.CHAOS_STONE.item)
                input('R', Items.STICK)
            } on MaterialCard.CHAOS_STONE.item
        }.register()
        val PHANTOM_PICKAXE = ToolCard(
            "phantom_pickaxe", "Phantom Pickaxe", "幻想のつるはし",
            "\"Creation\" is the true power.", "人間が手にした唯一の幻想。",
            4, createPickaxe(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern(" R ")
                pattern(" R ")
                input('#', MaterialCard.PHANTOM_DROP.item)
                input('R', Items.STICK)
            } on MaterialCard.PHANTOM_DROP.item
        }.register()
        val PHANTOM_SHOVEL = ToolCard(
            "phantom_shovel", "Phantom Shovel", "幻想のシャベル",
            "The sound of the world's end echoed", "破壊された世界の音――",
            4, createShovel(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("#")
                pattern("R")
                pattern("R")
                input('#', MaterialCard.PHANTOM_DROP.item)
                input('R', Items.STICK)
            } on MaterialCard.PHANTOM_DROP.item
        }.register()
        val PHANTOM_SWORD = ToolCard(
            "phantom_sword", "Phantom Sword", "幻想の剣",
            "Pray. For rebirth.", "闇を切り裂く、再生の光。",
            4, createSword(ToolMaterialCard.PHANTOM_DROP).selfMending(20).obtainFairy(9.0 * 9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("#")
                pattern("#")
                pattern("R")
                input('#', MaterialCard.PHANTOM_DROP.item)
                input('R', Items.STICK)
            } on MaterialCard.PHANTOM_DROP.item
        }.register()
    }
}
