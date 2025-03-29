package miragefairy2024.mod.tool

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.EnchantmentCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.mod.tool.effects.areaMining
import miragefairy2024.mod.tool.effects.collection
import miragefairy2024.mod.tool.effects.cutAll
import miragefairy2024.mod.tool.effects.enchantment
import miragefairy2024.mod.tool.effects.glint
import miragefairy2024.mod.tool.effects.mineAll
import miragefairy2024.mod.tool.effects.obtainFairy
import miragefairy2024.mod.tool.effects.selfMending
import miragefairy2024.mod.tool.effects.soulStreamContainable
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
            FairyScytheConfiguration(ToolMaterialCard.IRON),
        ) { registerScytheRecipeGeneration(item, Items.IRON_INGOT) }
        val DIAMOND_SCYTHE = !ToolCard(
            "diamond_scythe", EnJa("Diamond Scythe", "ダイヤモンドの大鎌"),
            PoemList(null),
            FairyScytheConfiguration(ToolMaterialCard.DIAMOND),
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
        val BISMUTH_PICKAXE = !ToolCard(
            "bismuth_pickaxe", EnJa("Bismuth Pickaxe", "ビスマスのつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.BISMUTH).enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.enchantment, 1),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.BISMUTH_INGOT.item) }
        val BISMUTH_AXE = !ToolCard(
            "bismuth_axe", EnJa("Bismuth Axe", "ビスマスの斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.BISMUTH, 6.5F, -3.2F).enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.enchantment, 1),
        ) { registerAxeRecipeGeneration(item, MaterialCard.BISMUTH_INGOT.item) }
        val BISMUTH_SHOVEL = !ToolCard(
            "bismuth_shovel", EnJa("Bismuth Shovel", "ビスマスのシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.BISMUTH).enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.enchantment, 1),
        ) { registerShovelRecipeGeneration(item, MaterialCard.BISMUTH_INGOT.item) }
        val BISMUTH_HOE = !ToolCard(
            "bismuth_hoe", EnJa("Bismuth Hoe", "ビスマスのクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.BISMUTH, -2, -1.5F).enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.enchantment, 1),
        ) { registerHoeRecipeGeneration(item, MaterialCard.BISMUTH_INGOT.item) }
        val BISMUTH_SWORD = !ToolCard(
            "bismuth_sword", EnJa("Bismuth Sword", "ビスマスの剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.BISMUTH).enchantment(Enchantments.FORTUNE, 1).enchantment(EnchantmentCard.FORTUNE_UP.enchantment, 1),
        ) { registerSwordRecipeGeneration(item, MaterialCard.BISMUTH_INGOT.item) }
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
        val AMETHYST_PICKAXE = !ToolCard(
            "amethyst_pickaxe", EnJa("Amethyst Pickaxe", "アメジストのつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.AMETHYST).enchantment(Enchantments.FORTUNE, 2),
        ) { registerPickaxeRecipeGeneration(item, Items.AMETHYST_SHARD) }
        val AMETHYST_AXE = !ToolCard(
            "amethyst_axe", EnJa("Amethyst Axe", "アメジストの斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.AMETHYST, 6.5F, -3.2F).enchantment(Enchantments.FORTUNE, 2),
        ) { registerAxeRecipeGeneration(item, Items.AMETHYST_SHARD) }
        val AMETHYST_SHOVEL = !ToolCard(
            "amethyst_shovel", EnJa("Amethyst Shovel", "アメジストのシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.AMETHYST).enchantment(Enchantments.FORTUNE, 2),
        ) { registerShovelRecipeGeneration(item, Items.AMETHYST_SHARD) }
        val AMETHYST_HOE = !ToolCard(
            "amethyst_hoe", EnJa("Amethyst Hoe", "アメジストのクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.AMETHYST, -1, -2.0F).enchantment(Enchantments.FORTUNE, 2),
        ) { registerHoeRecipeGeneration(item, Items.AMETHYST_SHARD) }
        val AMETHYST_SWORD = !ToolCard(
            "amethyst_sword", EnJa("Amethyst Sword", "アメジストの剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.AMETHYST).enchantment(Enchantments.LOOTING, 2),
        ) { registerSwordRecipeGeneration(item, Items.AMETHYST_SHARD) }
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
        val ECHO_SHARD_PICKAXE = !ToolCard(
            "echo_shard_pickaxe", EnJa("Echo Pickaxe", "残響のつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.ECHO_SHARD).enchantment(Enchantments.EFFICIENCY, 5),
        ) { registerPickaxeRecipeGeneration(item, Items.ECHO_SHARD) }
        val ECHO_SHARD_AXE = !ToolCard(
            "echo_shard_axe", EnJa("Echo Axe", "残響の斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.ECHO_SHARD, 5.0F, -3.0F).enchantment(Enchantments.EFFICIENCY, 5),
        ) { registerAxeRecipeGeneration(item, Items.ECHO_SHARD) }
        val ECHO_SHARD_SHOVEL = !ToolCard(
            "echo_shard_shovel", EnJa("Echo Shovel", "残響のシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.ECHO_SHARD).enchantment(Enchantments.EFFICIENCY, 5),
        ) { registerShovelRecipeGeneration(item, Items.ECHO_SHARD) }
        val ECHO_SHARD_HOE = !ToolCard(
            "echo_shard_hoe", EnJa("Echo Hoe", "残響のクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.ECHO_SHARD, -4, 0.0F).enchantment(Enchantments.EFFICIENCY, 5),
        ) { registerHoeRecipeGeneration(item, Items.ECHO_SHARD) }
        val ECHO_SHARD_SWORD = !ToolCard(
            "echo_shard_sword", EnJa("Echo Sword", "残響の剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.ECHO_SHARD).enchantment(Enchantments.SHARPNESS, 5),
        ) { registerSwordRecipeGeneration(item, Items.ECHO_SHARD) }
        val NETHER_STAR_PICKAXE = !ToolCard(
            "nether_star_pickaxe", EnJa("Nether Star Pickaxe", "ネザースターのつるはし"),
            PoemList(null),
            FairyPickaxeConfiguration(ToolMaterialCard.NETHER_STAR).enchantment(Enchantments.FORTUNE, 4).glint(),
        ) { registerPickaxeRecipeGeneration(item, Items.NETHER_STAR) }
        val NETHER_STAR_AXE = !ToolCard(
            "nether_star_axe", EnJa("Nether Star Axe", "ネザースターの斧"),
            PoemList(null),
            FairyAxeConfiguration(ToolMaterialCard.NETHER_STAR, 4.5F, -3.0F).enchantment(Enchantments.FORTUNE, 4).glint(),
        ) { registerAxeRecipeGeneration(item, Items.NETHER_STAR) }
        val NETHER_STAR_SHOVEL = !ToolCard(
            "nether_star_shovel", EnJa("Nether Star Shovel", "ネザースターのシャベル"),
            PoemList(null),
            FairyShovelConfiguration(ToolMaterialCard.NETHER_STAR).enchantment(Enchantments.FORTUNE, 4).glint(),
        ) { registerShovelRecipeGeneration(item, Items.NETHER_STAR) }
        val NETHER_STAR_HOE = !ToolCard(
            "nether_star_hoe", EnJa("Nether Star Hoe", "ネザースターのクワ"),
            PoemList(null),
            FairyHoeConfiguration(ToolMaterialCard.NETHER_STAR, -4, 0.0F).enchantment(Enchantments.FORTUNE, 4).glint(),
        ) { registerHoeRecipeGeneration(item, Items.NETHER_STAR) }
        val NETHER_STAR_SWORD = !ToolCard(
            "nether_star_sword", EnJa("Nether Star Sword", "ネザースターの剣"),
            PoemList(null),
            FairySwordConfiguration(ToolMaterialCard.NETHER_STAR).enchantment(Enchantments.LOOTING, 4).glint(),
        ) { registerSwordRecipeGeneration(item, Items.NETHER_STAR) }

        val FAIRY_CRYSTAL_PICKAXE = !ToolCard(
            "fairy_crystal_pickaxe", EnJa("Fairy Crystal Pickaxe", "フェアリークリスタルのつるはし"),
            PoemList(2).poem(EnJa("A brain frozen in crystal", "闇を打ち砕く、透き通る心。")),
            FairyPickaxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_SCYTHE = !ToolCard(
            "fairy_crystal_scythe", EnJa("Fairy Crystal Scythe", "フェアリークリスタルの大鎌"),
            PoemList(2).poem(EnJa("What color is fairy blood?", "妖精を刈り取るための道具。")),
            FairyScytheConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_SWORD = !ToolCard(
            "fairy_crystal_sword", EnJa("Fairy Crystal Sword", "フェアリークリスタルの剣"),
            PoemList(2).poem(EnJa("Nutrients for the soul", "妖精はこれをおやつにするという")),
            FairySwordConfiguration(ToolMaterialCard.FAIRY_CRYSTAL).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val FAIRY_CRYSTAL_BATTLE_AXE = !ToolCard(
            "fairy_crystal_battle_axe", EnJa("Fairy Crystal Battle Axe", "フェアリークリスタルの戦斧"),
            PoemList(2).poem(EnJa("The embodiment of fighting spirit", "妖精の本能を呼び覚ませ。")),
            FairyBattleAxeConfiguration(ToolMaterialCard.FAIRY_CRYSTAL, 6.5F, -3.0F).selfMending(10).obtainFairy(9.0).soulStreamContainable(),
        ) { registerBattleAxeRecipeGeneration(item, MaterialCard.FAIRY_CRYSTAL.item) }
        val MIRAGIUM_PICKAXE = !ToolCard(
            "miragium_pickaxe", EnJa("Miragium Pickaxe", "ミラジウムのつるはし"),
            PoemList(3).poem(EnJa("More durable than gold", "妖精の肉体労働")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIUM).selfMending(20).mineAll().soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.item) }
        val MIRAGIUM_AXE = !ToolCard(
            "miragium_axe", EnJa("Miragium Axe", "ミラジウムの斧"),
            PoemList(3).poem(EnJa("Crack! Squish!", "バキッ！ぐにっ")),
            FairyAxeConfiguration(ToolMaterialCard.MIRAGIUM, 5.0F, -3.0F).selfMending(20).cutAll().soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MIRAGIUM_INGOT.item) }
        val LILAGIUM_SCYTHE = !ToolCard(
            "lilagium_scythe", EnJa("Lilagium Scythe", "リラジウムの大鎌"),
            PoemList(3).poem(EnJa("Wish upon the grass", "葉っぱが吸い込まれてくる")),
            FairyScytheConfiguration(ToolMaterialCard.LILAGIUM, range = 2).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.LILAGIUM_INGOT.item) }
        val LUMINITE_PICKAXE = !ToolCard(
            "luminite_pickaxe", EnJa("Luminite Pickaxe", "ルミナイトのつるはし"),
            PoemList(4).poem(EnJa("Energetic soul extract", "精製された魂の残滓。")),
            FairyPickaxeConfiguration(ToolMaterialCard.LUMINITE).enchantment(Enchantments.EFFICIENCY, 4).enchantment(Enchantments.FORTUNE, 3),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.LUMINITE.item) }
        val LUMINITE_AXE = !ToolCard(
            "luminite_axe", EnJa("Luminite Axe", "ルミナイトの斧"),
            PoemList(4).poem(EnJa("Spiritual ectoplasm recycler", "失われた記憶の断片。")),
            FairyAxeConfiguration(ToolMaterialCard.LUMINITE, 5.0F, -3.0F).enchantment(Enchantments.EFFICIENCY, 4).enchantment(Enchantments.FORTUNE, 3),
        ) { registerAxeRecipeGeneration(item, MaterialCard.LUMINITE.item) }
        val LUMINITE_SWORD = !ToolCard(
            "luminite_sword", EnJa("Luminite Sword", "ルミナイトの剣"),
            PoemList(4).poem(EnJa("Bionic etheroluminescence illuminator", "光を生み出す力。")),
            FairySwordConfiguration(ToolMaterialCard.LUMINITE).enchantment(Enchantments.SHARPNESS, 4).enchantment(Enchantments.LOOTING, 3),
        ) { registerSwordRecipeGeneration(item, MaterialCard.LUMINITE.item) }
        val RESONITE_PICKAXE = !ToolCard(
            "resonite_pickaxe", EnJa("Resonance Pickaxe", "共鳴のつるはし"),
            PoemList(5).poem(EnJa("The compound of light and sound", "光と闇の純結晶。")),
            FairyPickaxeConfiguration(ToolMaterialCard.RESONITE).mineAll().enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val RESONITE_AXE = !ToolCard(
            "resonite_axe", EnJa("Resonance Axe", "共鳴の斧"),
            PoemList(5).poem(EnJa("Wavelength matched to tree height", "共振する樹の繊維。")),
            FairyAxeConfiguration(ToolMaterialCard.RESONITE, 4.0F, -3.0F).cutAll().enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val RESONITE_KNIFE = !ToolCard(
            "resonite_knife", EnJa("Resonite Knife", "共鳴のナイフ"),
            PoemList(5).poem(EnJa(en = "The ultrasonic vibration knife", "音波を超えた破壊の力。")),
            FairyKnifeConfiguration(ToolMaterialCard.RESONITE).areaMining().enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerKnifeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val RESONITE_SCYTHE = !ToolCard(
            "resonite_scythe", EnJa("Resonite Scythe", "共鳴の大鎌"),
            PoemList(5).poem(EnJa("Vacuum wave that cuts down grasses", "虚空を切り裂く碧の波。")),
            FairyScytheConfiguration(ToolMaterialCard.RESONITE).enchantment(Enchantments.SWEEPING, 3).enchantment(Enchantments.EFFICIENCY, 6).soulStreamContainable(),
        ) { registerScytheRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val RESONITE_SWORD = !ToolCard(
            "resonite_sword", EnJa("Resonite Sword", "共鳴の剣"),
            PoemList(5).poem(EnJa("The pulsating ectoplasm", "生命の脈動を砥ぎ澄ませ。")),
            FairySwordConfiguration(ToolMaterialCard.RESONITE).enchantment(Enchantments.SWEEPING, 3).enchantment(Enchantments.SHARPNESS, 6).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val RESONITE_BATTLE_AXE = !ToolCard(
            "resonite_battle_axe", EnJa("Resonite Battle Axe", "共鳴の戦斧"),
            PoemList(5).poem(EnJa("The mind synchronize with the body", "精神と肉体の調和。")),
            FairyBattleAxeConfiguration(ToolMaterialCard.RESONITE, 7.0F, -3.0F).cutAll().enchantment(Enchantments.SHARPNESS, 6).soulStreamContainable(),
        ) { registerBattleAxeRecipeGeneration(item, MaterialCard.RESONITE_INGOT.item) }
        val MIRAGIDIAN_PICKAXE = !ToolCard(
            "miragidian_pickaxe", EnJa("Miragidian Pickaxe", "ミラジディアンのつるはし"),
            PoemList(4).poem(EnJa("The reinforced will of the substance", "高速度鋼という名の誇り。")),
            FairyPickaxeConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable(),
        ) { registerPickaxeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.item) }
        val MIRAGIDIAN_AXE = !ToolCard(
            "miragidian_axe", EnJa("Miragidian Axe", "ミラジディアンの斧"),
            PoemList(4).poem(EnJa("The Ancient Future Relic", "古代の未来遺物。")),
            FairyAxeConfiguration(ToolMaterialCard.MIRAGIDIAN, 5.0F, -3.0F).soulStreamContainable(),
        ) { registerAxeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.item) }
        val MIRAGIDIAN_SHOVEL = !ToolCard(
            "miragidian_shovel", EnJa("Miragidian Shovel", "ミラジディアンのシャベル"),
            PoemList(4).poem(EnJa("An alloy that will outlive humanity", "文明の光よりも堅牢な闇。")),
            FairyShovelConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable(),
        ) { registerShovelRecipeGeneration(item, MaterialCard.MIRAGIDIAN.item) }
        val MIRAGIDIAN_HOE = !ToolCard(
            "miragidian_hoe", EnJa("Miragidian Hoe", "ミラジディアンのクワ"),
            PoemList(4).poem(EnJa("They were killed by containment targets", "宇宙人類の然程輝きのない栄光。")),
            FairyHoeConfiguration(ToolMaterialCard.MIRAGIDIAN, -3, 0.0F).soulStreamContainable(),
        ) { registerHoeRecipeGeneration(item, MaterialCard.MIRAGIDIAN.item) }
        val MIRAGIDIAN_SWORD = !ToolCard(
            "miragidian_sword", EnJa("Miragidian Sword", "ミラジディアンの剣"),
            PoemList(4).poem(EnJa("Watching the end of civilization.", "この星の運命を写す瞳。")),
            FairySwordConfiguration(ToolMaterialCard.MIRAGIDIAN).soulStreamContainable(),
        ) { registerSwordRecipeGeneration(item, MaterialCard.MIRAGIDIAN.item) }
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
            FairyScytheConfiguration(ToolMaterialCard.MIRANAGITE).enchantment(Enchantments.SILK_TOUCH),
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
    val item = configuration.also { it.apply() }.createItem()

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
