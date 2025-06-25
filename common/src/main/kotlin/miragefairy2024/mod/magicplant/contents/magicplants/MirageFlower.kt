package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.FairyForestBiomeCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.center
import miragefairy2024.util.count
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.end
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.not
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.randomInt
import miragefairy2024.util.register
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.biome.Biomes as BiomeKeys
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

abstract class AbstractMirageFlowerCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Miragaceae", "妖花目ミラージュ科")

    override val ageProperty: IntProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(6.0, 12.0),
        createCuboidShape(6.0, 15.0),
        createCuboidShape(6.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("mirage")
    override val defaultTraitBits = mapOf(
        TraitCard.ETHER_RESPIRATION.trait to 0b1000, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait to 0b1000, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0b1000, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b1000, // 養分吸収
        TraitCard.AIR_ADAPTATION.trait to 0b1000, // 空間適応
        TraitCard.COLD_ADAPTATION.trait to 0b1000, // 低温適応
        TraitCard.WARM_ADAPTATION.trait to 0b1000, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0b1000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b1000, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait to 0b1000, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0b1000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b1000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b1000, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b1000, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0b1000, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0b1000, // 経験値生成
        TraitCard.FOUR_LEAFED.trait to 0b1000, // 四つ葉
        TraitCard.SPINY_LEAVES.trait to 0b1000, // 棘のある葉
        TraitCard.HEATING_MECHANISM.trait to 0b1000, // 発熱機構
        TraitCard.ETHER_PREDATION.trait to 0b1000, // エーテル捕食
        TraitCard.PROSPERITY_OF_SPECIES.trait to 0b1000, // 種の繁栄
        TraitCard.CROSSBREEDING.trait to 0b1000, // 交雑
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0b1000, // 自我を持つ植物
    )
    override val randomTraitChances = mapOf(
        TraitCard.ETHER_RESPIRATION.trait to 0.05, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0.05, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.AIR_ADAPTATION.trait to 0.05, // 空間適応
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.HEATING_MECHANISM.trait to 0.05, // 発熱機構
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PROSPERITY_OF_SPECIES.trait to 0.05, // 種の繁栄
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0.05, // 自我を持つ植物
    )
}

object MirageFlowerCard : AbstractMirageFlowerCard<MirageFlowerBlock>() {
    override fun getBlockPath() = "mirage_flower"
    override val blockName = EnJa("Mirage Flower", "妖花ミラージュ")
    override fun getItemPath() = "mirage_bulb"
    override val itemName = EnJa("Mirage Bulb", "妖花ミラージュの球根")
    override val tier = 1
    override val poem = EnJa("Evolution to escape extermination", "可憐にして人畜無害たる魔物。")

    override val blockCodec = MirageFlowerBlock.CODEC
    override fun createBlock() = MirageFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.DIAMOND).sound(BlockSoundGroup.GLASS))

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)
    override fun getFruitDrops(count: Int, random: Random) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.MIRAGE_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(MaterialCard.FAIRY_CRYSTAL.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.CRYSTAL_ABSORPTION.trait to 0b1000, // 鉱物吸収
        TraitCard.FAIRY_BLESSING.trait to 0b1000, // 妖精の祝福
        TraitCard.NODED_STEM.trait to 0b1000, // 節状の茎
        TraitCard.FRUIT_OF_KNOWLEDGE.trait to 0b1000, // 禁断の果実
        TraitCard.DESERT_GEM.trait to 0b1000, // 砂漠の宝石
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0b1000, // 浸水耐性
        TraitCard.ADVERSITY_FLOWER.trait to 0b1000, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait to 0b1000, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait to 0b1000, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait to 0b1000, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0b1000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.FAIRY_BLESSING.trait to 0.05, // 妖精の祝福
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.FRUIT_OF_KNOWLEDGE.trait to 0.05, // 禁断の果実
        TraitCard.DESERT_GEM.trait to 0.05, // 砂漠の宝石
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0.05, // 浸水耐性
        TraitCard.ADVERSITY_FLOWER.trait to 0.05, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait to 0.05, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait to 0.05, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    private val FAIRY_RING_FEATURE = FairyRingFeature(FairyRingFeatureConfig.CODEC)

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("A World Ruled by Plants", "植物の支配する世界"),
        //name = EnJa("Terraformer of the Fantasy World", "幻想世界のテラフォーマー"), // TODO どこかで使う
        description = EnJa("Find the Mirage flower", "妖花ミラージュを摘もう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("fairy_ring")) { FAIRY_RING_FEATURE }.register() // Fairy Ring
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) { // 小さな塊
                placedFeature("cluster", { per(16) + flower(square, surface) }) { overworld + end * !+BiomeKeys.THE_END }  // 地上・エンド外縁の島々に通常クラスタ
                placedFeature("nether_cluster", { per(64) + flower(square, nether) }) { nether } // ネザーにネザー用クラスタ
                placedFeature("fairy_forest_cluster", { count(4) + flower(square, surface) }) { +BiomeCards.FAIRY_FOREST.registryKey + +BiomeCards.DEEP_FAIRY_FOREST.registryKey } // 妖精の森
            }
        }
        FAIRY_RING_FEATURE {
            configuredFeature("fairy_ring", { FairyRingFeatureConfig(100, 6F, 8F, 3, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) { // Fairy Ring
                placedFeature("fairy_ring", { per(600) + flower(center, surface) }) { overworld }  // 地上にFairy Ring
            }
        }
    }
}

class MirageFlowerBlock(settings: Properties) : SimpleMagicPlantBlock(MirageFlowerCard, settings) {
    companion object {
        val CODEC: MapCodec<MirageFlowerBlock> = simpleCodec(::MirageFlowerBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

object PhantomFlowerCard : AbstractMirageFlowerCard<PhantomFlowerBlock>() {
    override fun getBlockPath() = "phantom_flower"
    override val blockName = EnJa("Phantom Flower", "幻花ファントム")
    override fun getItemPath() = "phantom_bulb"
    override val itemName = EnJa("Phantom Bulb", "幻花ファントムの球根")
    override val tier = 3
    override val poem = EnJa("Illusory telepathy", "――おいでよ、僕たちのところへ")

    override val blockCodec = PhantomFlowerBlock.CODEC
    override fun createBlock() = PhantomFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.COLOR_PINK).sound(BlockSoundGroup.GLASS))

    override val baseGrowth = 1.0 / 8.0
    override val baseFruitGeneration = 9.0

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.PHANTOM_LEAVES.item, MaterialCard.PHANTOM_DROP.item)
    override fun getFruitDrops(count: Int, random: Random) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.PHANTOM_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(MaterialCard.PHANTOM_DROP.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.PHANTOM_FLOWER.trait to 0b1000, // 幻の花
        TraitCard.FLOWER_OF_THE_END.trait to 0b1000, // 終焉の花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.PHANTOM_FLOWER.trait to 0.05, // 幻の花
        TraitCard.FLOWER_OF_THE_END.trait to 0.05, // 終焉の花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyForestBiomeCard.advancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Selective Pressure of Cuteness", "かわいいの淘汰圧"),
        description = EnJa("Search for the Phantom Flower hidden in the Fairy Forest", "妖精の森に隠れている幻花ファントムを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it))) }) {
                placedFeature("cluster", { per(16) + flower(square, surface) }) { +BiomeCards.FAIRY_FOREST.registryKey }
            }
        }
    }
}

class PhantomFlowerBlock(settings: Properties) : SimpleMagicPlantBlock(PhantomFlowerCard, settings) {
    companion object {
        val CODEC: MapCodec<PhantomFlowerBlock> = simpleCodec(::PhantomFlowerBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}

private fun getMirageFlour(count: Int, random: Random): List<ItemStack> {
    var count2 = count.toDouble()
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_EARTH.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_SKY.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    if (count2 < 3) return listOf(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item().createItemStack(random.randomInt(count2)))
    count2 /= 9.0
    return listOf(MaterialCard.MIRAGE_FLOUR_OF_TIME.item().createItemStack(random.randomInt(count2)))
}
