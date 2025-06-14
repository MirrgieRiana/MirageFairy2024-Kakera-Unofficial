package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.BiomeCards
import miragefairy2024.mod.FairyForestBiomeCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.per
import miragefairy2024.util.register
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.worldgen.placement.PlacementUtils as PlacedFeatures
import net.minecraft.util.RandomSource as Random
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.properties.IntegerProperty as IntProperty
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration as RandomPatchFeatureConfig
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration as SimpleBlockFeatureConfig

object PhantomFlowerCard : SimpleMagicPlantCard<PhantomFlowerBlock>() {
    override fun getBlockPath() = "phantom_flower"
    override val blockName = EnJa("Phantom Flower", "幻花ファントム")
    override fun getItemPath() = "phantom_bulb"
    override val itemName = EnJa("Phantom Bulb", "ファントムの球根")
    override val tier = 3
    override val poem = EnJa("Illusory telepathy", "――おいでよ、僕たちのところへ")
    override val classification = EnJa("Order Miragales, family Miragaceae", "妖花目ミラージュ科")

    override val ageProperty: IntProperty = BlockStateProperties.AGE_3
    override fun createBlock() = PhantomFlowerBlock(createCommonSettings().breakInstantly().mapColor(MapColor.COLOR_PINK).sound(BlockSoundGroup.GLASS))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0),
        createCuboidShape(6.0, 12.0),
        createCuboidShape(6.0, 15.0),
        createCuboidShape(6.0, 16.0),
    )

    override val baseGrowth = 1.0 / 8.0
    override val baseFruitGeneration = 9.0

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.PHANTOM_LEAVES.item, MaterialCard.PHANTOM_DROP.item)
    override fun getFruitDrops(count: Int, random: Random) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: Random) = listOf(MaterialCard.PHANTOM_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: Random) = listOf(MaterialCard.PHANTOM_DROP.item().createItemStack(count))

    override val family = MirageFairy2024.identifier("mirage")
    override val possibleTraits = setOf(
        TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        //TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        TraitCard.AIR_ADAPTATION.trait, // 空間適応
        TraitCard.COLD_ADAPTATION.trait, // 低温適応
        TraitCard.WARM_ADAPTATION.trait, // 中温適応
        TraitCard.HOT_ADAPTATION.trait, // 高温適応
        TraitCard.ARID_ADAPTATION.trait, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait, // 経験値生成
        //TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        //TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        //TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        //TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        //TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        //TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        //TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        //TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        //TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        //TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        TraitCard.CROSSBREEDING.trait, // 交雑
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )

    val PHANTOM_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("phantom_cluster")
    val PHANTOM_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("phantom_cluster")

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

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("phantom_flower")) { PhantomFlowerBlock.CODEC }.register()

        Feature.FLOWER {
            PHANTOM_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it)))
            }) {
                PHANTOM_CLUSTER_PLACED_FEATURE_KEY({ per(16) + flower }) { +BiomeCards.FAIRY_FOREST.registryKey }
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

