package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tool.contents.ScytheItem
import miragefairy2024.mod.tool.contents.ShootingStaffItem
import miragefairy2024.mod.tool.contents.createAxe
import miragefairy2024.mod.tool.contents.createBattleAxe
import miragefairy2024.mod.tool.contents.createKnife
import miragefairy2024.mod.tool.contents.createPickaxe
import miragefairy2024.mod.tool.contents.createScythe
import miragefairy2024.mod.tool.contents.createShootingStaff
import miragefairy2024.mod.tool.contents.createShovel
import miragefairy2024.mod.tool.contents.createSword
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
    FairyToolCard.entries.forEach {
        it.init()
    }

    FairyToolSettings.AREA_MINING_TRANSLATION.enJa()
    FairyToolSettings.MINE_ALL_TRANSLATION.enJa()
    FairyToolSettings.CUT_ALL_TRANSLATION.enJa()
    FairyToolSettings.SILK_TOUCH_TRANSLATION.enJa()
    FairyToolSettings.FORTUNE_TRANSLATION.enJa()
    FairyToolSettings.SELF_MENDING_TRANSLATION.enJa()
    FairyToolSettings.OBTAIN_FAIRY.enJa()

    ScytheItem.DESCRIPTION_TRANSLATION.enJa()
    ShootingStaffItem.NOT_ENOUGH_EXPERIENCE_TRANSLATION.enJa()
    ShootingStaffItem.DESCRIPTION_TRANSLATION.enJa()

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
    fun init(card: FairyToolCard) = Unit

    fun addPoems(poemList: PoemList) = poemList
}

class FairyToolCard(
    path: String,
    private val enName: String,
    private val jaName: String,
    private val enPoem: String,
    private val jaPoem: String,
    private val tier: Int,
    private val toolSettings: ToolSettings,
    private val initializer: context(ModContext)FairyToolCard.() -> Unit = {},
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
        val entries = mutableListOf<FairyToolCard>()
        private operator fun FairyToolCard.not() = this.also { entries.add(this) }

        val IRON_SCYTHE = !FairyToolCard(
            "iron_scythe", "Iron Scythe", "鉄の大鎌",
            "For cutting grass and harvesting crops.", "草や農作物を刈り取るための道具。",
            2, createScythe(ToolMaterialCard.IRON, 1),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" ##")
                pattern("# R")
                pattern("  R")
                input('#', Items.IRON_INGOT)
                input('R', MaterialCard.MIRAGE_STEM.item)
            } on Items.IRON_INGOT
        }
        val FAIRY_CRYSTAL_PICKAXE = !FairyToolCard(
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
        }
        val FAIRY_CRYSTAL_SCYTHE = !FairyToolCard(
            "fairy_crystal_scythe", "Fairy Crystal Scythe", "フェアリークリスタルの大鎌",
            "What color is fairy blood?", "妖精を刈り取るための道具。",
            2, createScythe(ToolMaterialCard.FAIRY_CRYSTAL, 2).selfMending(10).obtainFairy(9.0),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" ##")
                pattern("# R")
                pattern("  R")
                input('#', MaterialCard.FAIRY_CRYSTAL.item)
                input('R', MaterialCard.MIRAGE_STEM.item)
            } on MaterialCard.FAIRY_CRYSTAL.item
        }
        val FAIRY_CRYSTAL_SWORD = !FairyToolCard(
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
        }
        val FAIRY_CRYSTAL_BATTLE_AXE = !FairyToolCard(
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
        }
        val MIRAGIUM_PICKAXE = !FairyToolCard(
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
        }
        val MIRAGIUM_AXE = !FairyToolCard(
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
        }
        val MIRANAGITE_KNIFE = !FairyToolCard(
            "miranagite_knife", "Miranagite Knife", "蒼天石のナイフ",
            "Gardener's tool invented by Miranagi", "大自然を駆ける探究者のナイフ。",
            2, createKnife(ToolMaterialCard.MIRANAGITE).silkTouch(),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("#")
                pattern("R")
                input('#', MaterialCard.MIRANAGITE.item)
                input('R', Items.STICK)
            } on MaterialCard.MIRANAGITE.item
        }
        val MIRANAGITE_PICKAXE = !FairyToolCard(
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
        }
        val MIRANAGITE_SCYTHE = !FairyToolCard(
            "miranagite_scythe", "Miranagite Scythe", "蒼天石の大鎌",
            "Releases the souls of weeds", "宙を切り裂く創世の刃、草魂を蒼天へ導く。",
            2, createScythe(ToolMaterialCard.MIRANAGITE, 3).silkTouch(),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" ##")
                pattern("# R")
                pattern("  R")
                input('#', MaterialCard.MIRANAGITE.item)
                input('R', MaterialCard.MIRAGE_STEM.item)
            } on MaterialCard.MIRANAGITE.item
        }
        val MIRANAGI_STAFF_0 = !FairyToolCard(
            "miranagi_staff_0", "Miranagite Staff", "蒼天石のスタッフ",
            "Inflating anti-entropy force", "膨張する秩序の力。",
            2, createShootingStaff(ToolMaterialCard.MIRANAGITE, 7F, 12F).silkTouch(),
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
        val MIRANAGI_STAFF = !FairyToolCard(
            "miranagi_staff", "Staff of Miranagi", "みらなぎの杖",
            "Risk of vacuum decay due to anti-entropy", "創世の神光は混沌をも翻す。",
            3, createShootingStaff(ToolMaterialCard.MIRANAGITE, 10F, 16F).silkTouch(),
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
        val XARPITE_PICKAXE = !FairyToolCard(
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
        }
        val XARPITE_AXE = !FairyToolCard(
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
        }
        val DIAMOND_SCYTHE = !FairyToolCard(
            "diamond_scythe", "Diamond Scythe", "ダイヤモンドの大鎌",
            "A highly durable scythe made of diamond.", "ダイヤモンドを加工した高耐久の大鎌。",
            3, createScythe(ToolMaterialCard.DIAMOND, 3),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" ##")
                pattern("# R")
                pattern("  R")
                input('#', Items.DIAMOND)
                input('R', MaterialCard.MIRAGE_STEM.item)
            } on Items.DIAMOND
        }
        val CHAOS_STONE_PICKAXE = !FairyToolCard(
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
        }
        val PHANTOM_PICKAXE = !FairyToolCard(
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
        }
        val PHANTOM_SHOVEL = !FairyToolCard(
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
        }
        val PHANTOM_SWORD = !FairyToolCard(
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
        }
    }
}
