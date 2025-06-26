package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.MagicPlantBlockEntity
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.rangedNether
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.underground
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

abstract class AbstractLuminariaCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")

    override val ageProperty: IntProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("luminaria")
}

object DiamondLuminariaCard : AbstractLuminariaCard<DiamondLuminariaBlock>() {
    override fun getBlockPath() = "diamond_luminaria"
    override val blockName = EnJa("Diamond Luminaria", "金剛石輝草ダイヤモンドルミナリア")
    override fun getItemPath() = "diamond_luminaria_bulb"
    override val itemName = EnJa("Diamond Luminaria Bulb", "金剛石輝草ダイヤモンドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Fruits the crystallized carbon", "表土を飾る、凍てつく星。")

    override val blockCodec = DiamondLuminariaBlock.CODEC
    override fun createBlock() = DiamondLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.DIAMOND).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.2
    override val baseFruitGeneration = 0.1

    override val drops = listOf(MaterialCard.LUMINITE.item, { Items.DIAMOND })
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(Items.DIAMOND.createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0b00101000, // 低温適応
        TraitCard.MESIC_ADAPTATION.trait to 0b00101000, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0b00101000, // 悠久の秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.HEATING_MECHANISM.trait to 0.05, // 発熱機構
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0.05, // 悠久の秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Even Carbon Freezes", "炭素も凍る季節"),
        description = EnJa("Search for Diamond Luminaria in a cold biome", "寒冷バイオームでダイヤモンドルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) {
                placedFeature("cluster", { per(32) + flower(square, surface) }) { +ConventionalBiomeTags.IS_SNOWY + +ConventionalBiomeTags.IS_ICY }
            }
        }
    }
}

class DiamondLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(DiamondLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<DiamondLuminariaBlock> = simpleCodec(::DiamondLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

fun getLuminance(age: Int): Int {
    return when (age) {
        0 -> 2
        1 -> 4
        2 -> 8
        3 -> 15
        else -> 0
    }
}

object EmeraldLuminariaCard : AbstractLuminariaCard<EmeraldLuminariaBlock>() {
    override fun getBlockPath() = "emerald_luminaria"
    override val blockName = EnJa("Emerald Luminaria", "翠玉輝草エメラルドルミナリア")
    override fun getItemPath() = "emerald_luminaria_bulb"
    override val itemName = EnJa("Emerald Luminaria Bulb", "翠玉輝草エメラルドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Makes Berryllium by unknown means", "幸福もたらす、栄光の樹。")

    override val blockCodec = EmeraldLuminariaBlock.CODEC
    override fun createBlock() = EmeraldLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.EMERALD).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.2
    override val baseFruitGeneration = 0.1

    override val drops = listOf(MaterialCard.LUMINITE.item, { Items.EMERALD })
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(Items.EMERALD.createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0b00101000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0b00101000, // 悠久の秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0.05, // 悠久の秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Money Tree", "お金のなる木"),
        description = EnJa("Search for Emerald Luminaria in a plant-rich biome", "植物の繁茂するバイオームでエメラルドルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) {
                placedFeature("cluster", { per(32) + flower(square, surface) }) { +ConventionalBiomeTags.IS_JUNGLE }  // 地上
                placedFeature("underground_cluster", { count(32) + flower(square, underground) }) { +Biomes.LUSH_CAVES } // 地下
            }
        }
    }
}

class EmeraldLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(EmeraldLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<EmeraldLuminariaBlock> = simpleCodec(::EmeraldLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

abstract class AbstractProminariaCard<B : SimpleMagicPlantBlock> : AbstractLuminariaCard<B>()

object ProminariaCard : AbstractProminariaCard<ProminariaBlock>() {
    override fun getBlockPath() = "prominaria"
    override val blockName = EnJa("Prominaria", "紅炎草プロミナリア")
    override fun getItemPath() = "prominaria_bulb"
    override val itemName = EnJa("Prominaria Bulb", "紅炎草プロミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Cleansing of tainted souls.", "死霊を貪り、命に変える。")

    override val blockCodec = ProminariaBlock.CODEC
    override fun createBlock() = ProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.CRIMSON_HYPHAE).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.2

    override val drops = listOf(MaterialCard.PROMINARIA_BERRY.item, MaterialCard.PROMINITE.item)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.PROMINARIA_BERRY.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PROMINITE.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Subterranean Sun", "地獄にともる灯"),
        description = EnJa("Search for Prominaria in the bottom of Nether Wastes", "ネザーの荒地の最下層でプロミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) {
                placedFeature("cluster", { per(4) + flower(square, rangedNether(32, 45)) }) { +Biomes.NETHER_WASTES + +Biomes.CRIMSON_FOREST }
            }
        }
    }
}

class ProminariaBlock(settings: Properties) : SimpleMagicPlantBlock(ProminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<ProminariaBlock> = simpleCodec(::ProminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is MagicPlantBlockEntity) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        if (!stack.`is`(ConventionalItemTags.GOLD_INGOTS)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        if (level.isClientSide) return ItemInteractionResult.SUCCESS

        val age = state.getOr(BlockStateProperties.AGE_3) { 0 }
        val traitStacks = blockEntity.getTraitStacks()
        val rare = blockEntity.isRare()
        val natural = blockEntity.isNatural()

        level.setBlock(pos, GoldProminariaCard.block().defaultBlockState().setValue(BlockStateProperties.AGE_3, age), 0b1011)
        val newBlockEntity = level.getBlockEntity(pos) as MagicPlantBlockEntity
        newBlockEntity.setTraitStacks(traitStacks)
        newBlockEntity.setRare(rare)
        newBlockEntity.setNatural(natural)

        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F) // TODO 音を変更

        return ItemInteractionResult.CONSUME
    }
}

object GoldProminariaCard : AbstractProminariaCard<GoldProminariaBlock>() {
    override fun getBlockPath() = "gold_prominaria"
    override val blockName = EnJa("Gold Prominaria", "金炎草ゴールドプロミナリア")
    override fun getItemPath() = "gold_prominaria_bulb"
    override val itemName = EnJa("Gold Prominaria Bulb", "金炎草ゴールドプロミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Gold-plated prominence.", "地獄の沙汰も金次第。")

    override val blockCodec = GoldProminariaBlock.CODEC
    override fun createBlock() = GoldProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.GOLD).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.05
    override val baseSeedGeneration = 0.0

    override val drops = listOf(MaterialCard.GOLD_PROMINARIA_BERRY.item)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.GOLD_PROMINARIA_BERRY.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.GOLDEN_APPLE.trait to 0.05, // 金のリンゴ
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { ProminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Gold Tree", "金のなる木"),
        description = EnJa("Use a gold ingot on the Prominaria", "プロミナリアに金インゴットを使用する"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )
}

class GoldProminariaBlock(settings: Properties) : SimpleMagicPlantBlock(GoldProminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<GoldProminariaBlock> = simpleCodec(::GoldProminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}
