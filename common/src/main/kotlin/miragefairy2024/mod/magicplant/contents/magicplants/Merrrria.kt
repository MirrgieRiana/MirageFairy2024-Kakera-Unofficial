package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.rootAdvancement
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
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.material.MapColor
import kotlin.math.pow

object MerrrriaCard : SimpleMagicPlantCard<MerrrriaBlock>() {
    override fun getBlockPath() = "merrrria"
    override val blockName = EnJa("Merrrria", "月鈴花メルルルリア")
    override fun getItemPath() = "merrrria_bulb"
    override val itemName = EnJa("Merrrria Bulb", "月鈴花メルルルリアの球根")
    override val tier = 3
    override val poem = EnJa("Windswept concert.", "風の知らせる演奏会。")
    override val classification = EnJa("Order Miragales, family Merrrriaceae", "妖花目メルルルリア科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_4
    override fun createBlock() = MerrrriaBlock(createCommonSettings().breakInstantly().mapColor(MapColor.ICE).lightLevel { if (it.getValue(ageProperty) == 4) 6 else 0 }.emissiveRendering { it, _, _ -> it.getValue(ageProperty) == 4 }.sound(SoundType.AMETHYST))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 5.0), // TODO
        createCuboidShape(4.0, 7.0), // TODO
        createCuboidShape(7.0, 9.0), // TODO
        createCuboidShape(7.0, 16.0), // TODO
        createCuboidShape(7.0, 16.0), // TODO
    )

    override val drops = listOf(MaterialCard.MERRRRIA_DROP.item)

    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.MERRRRIA_DROP.item().createItemStack(count))

    override val family = MirageFairy2024.identifier("merrrria")
    override val possibleTraits = setOf(
        // TODO
        //TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        //TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
        //TraitCard.AIR_ADAPTATION.trait, // 空間適応
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
        TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        //TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        //TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        //TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        //TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        //TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        //TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        TraitCard.CROSSBREEDING.trait, // 交雑
        //TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        //TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )

    val MERRRRIA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("merrrria_cluster")
    val MERRRRIA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("merrrria_cluster")
    val MERRRRIA_LARGE_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("merrrria_large_cluster")
    val MERRRRIA_LARGE_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("merrrria_large_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Hill where a Moon Blooms", "月の咲く丘"),
        description = EnJa("Search for Merrrria flowers blooming in the windswept", "吹きさらしの丘に咲くメルルルリアの花を探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("merrrria")) { MerrrriaBlock.CODEC }.register()

        Feature.FLOWER {
            MERRRRIA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchConfiguration(1, 0, 0, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(it)))
            }) {
                MERRRRIA_CLUSTER_PLACED_FEATURE_KEY({ per(16) + flower }) { +ConventionalBiomeTags.IS_WINDSWEPT }
            }
            MERRRRIA_LARGE_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchConfiguration(40, 8, 3, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(it)))
            }) {
                MERRRRIA_LARGE_CLUSTER_PLACED_FEATURE_KEY({ per(128) + flower }) { +ConventionalBiomeTags.IS_WINDSWEPT }
            }
        }
    }
}

class MerrrriaBlock(settings: Properties) : SimpleMagicPlantBlock(MerrrriaCard, settings) {
    companion object {
        val CODEC: MapCodec<MerrrriaBlock> = simpleCodec(::MerrrriaBlock)
        private val PITCHES = listOf(
            listOf(0, 2, 4, 5, 7, 9, 11).map { it + 12 * 0 },
            listOf(0, 2, 4, 5, 7, 9, 11).map { it + 12 * 1 },
            listOf(0).map { it + 12 * 2 },
        ).flatten().map { 2F.pow(it / 12F) }
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_4

    override fun canGrow(blockState: BlockState) = getAge(blockState) < 3 // 0→3までは自然成長

    override fun move(world: ServerLevel, blockPos: BlockPos, blockState: BlockState, speed: Double, autoPick: Boolean) {
        super.move(world, blockPos, blockState, speed, autoPick)

        // 3と4の間は昼と夜で繰り返す
        if (getAge(blockState) >= 3) {
            val newBlockState = withAge(if (world.isNight) 4 else 3)
            if (newBlockState != blockState) {
                world.setBlock(blockPos, newBlockState, UPDATE_CLIENTS)
            }
        }

    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, world, pos, random)
        if (getAge(state) == 4) {
            if (random.nextInt(50) == 0) {
                // クライアント側ではisNightが機能しないので夜だけ演奏はできない
                val pitch = PITCHES[random.nextInt(PITCHES.size)]
                world.playLocalSound(
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble() + 0.5,
                    pos.z.toDouble() + 0.5,
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS,
                    0.25F,
                    0.3F * pitch,
                    false,
                )
                repeat(4) {
                    world.addParticle(
                        ParticleTypeCard.AURA.particleType,
                        pos.x.toDouble() + random.nextDouble(),
                        pos.y.toDouble() + random.nextDouble(),
                        pos.z.toDouble() + random.nextDouble(),
                        0.0,
                        0.1,
                        0.0,
                    )
                }
            }
        }
    }
}
