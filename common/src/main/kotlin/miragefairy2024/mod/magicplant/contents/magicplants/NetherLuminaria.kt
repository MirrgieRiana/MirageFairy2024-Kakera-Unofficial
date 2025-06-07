package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.magicplant.MagicPlantBlockEntity
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.rangedNetherFlower
import miragefairy2024.util.register
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
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

abstract class AbstractProminariaCard<B : SimpleMagicPlantBlock> : SimpleMagicPlantCard<B>() {
    override val classification = EnJa("Order Miragales, family Luminariaceae", "妖花目ルミナリア科")

    override val ageProperty: IntProperty = BlockStateProperties.AGE_3

    override val outlineShapes = listOf(
        createCuboidShape(4.0, 6.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val family = MirageFairy2024.identifier("luminaria")
    override val possibleTraits = setOf(
        //TraitCard.ETHER_RESPIRATION.trait, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait, // 光合成
        //TraitCard.PHAEOSYNTHESIS.trait, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait, // 鉱物吸収
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
        //TraitCard.RARE_PRODUCTION.trait, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait, // 経験値生成
        //TraitCard.FAIRY_BLESSING.trait, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait, // 四つ葉
        //TraitCard.NODED_STEM.trait, // 節状の茎
        //TraitCard.FRUIT_OF_KNOWLEDGE.trait, // 禁断の果実
        TraitCard.GOLDEN_APPLE.trait, // 金のリンゴ
        //TraitCard.SPINY_LEAVES.trait, // 棘のある葉
        //TraitCard.DESERT_GEM.trait, // 砂漠の宝石
        //TraitCard.HEATING_MECHANISM.trait, // 発熱機構
        //TraitCard.WATERLOGGING_TOLERANCE.trait, // 浸水耐性
        //TraitCard.ADVERSITY_FLOWER.trait, // 高嶺の花
        //TraitCard.FLESHY_LEAVES.trait, // 肉厚の葉
        //TraitCard.NATURAL_ABSCISSION.trait, // 自然落果
        //TraitCard.CARNIVOROUS_PLANT.trait, // 食虫植物
        //TraitCard.ETHER_PREDATION.trait, // エーテル捕食
        //TraitCard.PAVEMENT_FLOWERS.trait, // アスファルトに咲く花
        //TraitCard.PROSPERITY_OF_SPECIES.trait, // 種の繁栄
        //TraitCard.PHANTOM_FLOWER.trait, // 幻の花
        TraitCard.ETERNAL_TREASURE.trait, // 悠久の秘宝
        TraitCard.TREASURE_OF_XARPA.trait, // シャルパの秘宝
        //TraitCard.CROSSBREEDING.trait, // 交雑
        //TraitCard.PLANTS_WITH_SELF_AWARENESS.trait, // 自我を持つ植物
        //TraitCard.FLOWER_OF_THE_END.trait, // 終焉の花
    )
}

object ProminariaCard : AbstractProminariaCard<ProminariaBlock>() {
    override fun getBlockPath() = "prominaria"
    override val blockName = EnJa("Prominaria", "紅炎草プロミナリア")
    override fun getItemPath() = "prominaria_bulb"
    override val itemName = EnJa("Prominaria Bulb", "プロミナリアの球根")
    override val tier = 4
    override val poem = EnJa("Purification of the tainted soul", "地獄にともる灯。")

    override fun createBlock() = ProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.CRIMSON_HYPHAE).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.2

    override val drops = listOf(MaterialCard.PROMINARIA_BERRY.item, MaterialCard.PROMINITE.item)
    override fun getFruitDrops(count: Int, random: Random) = listOf(MaterialCard.PROMINARIA_BERRY.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PROMINITE.item().createItemStack(count))

    val PROMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY = Registries.CONFIGURED_FEATURE with MirageFairy2024.identifier("prominaria_cluster")
    val PROMINARIA_CLUSTER_PLACED_FEATURE_KEY = Registries.PLACED_FEATURE with MirageFairy2024.identifier("prominaria_cluster")

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Subterranean Sun", "地底の太陽"),
        description = EnJa("Search for Prominaria in the bottom of Nether Wastes", "ネザーの荒地の最下層でプロミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("prominaria")) { ProminariaBlock.CODEC }.register()

        Feature.FLOWER {
            PROMINARIA_CLUSTER_CONFIGURED_FEATURE_KEY({
                RandomPatchFeatureConfig(6, 6, 2, PlacedFeatures.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockFeatureConfig(it)))
            }) {
                PROMINARIA_CLUSTER_PLACED_FEATURE_KEY({ per(4) + rangedNetherFlower(32, 45) }) { +Biomes.NETHER_WASTES }
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
    override val itemName = EnJa("Gold Prominaria Bulb", "ゴールドプロミナリアの球根")
    override val tier = 4
    override val poem = EnJa("Gold-plated prominence.", "摂氏100度の金環食。")

    override fun createBlock() = GoldProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.GOLD).sound(BlockSoundGroup.CROP))

    override val baseGrowth = 0.05
    override val baseSeedGeneration = 0.0

    override val drops = listOf { Items.GOLD_NUGGET }
    override fun getFruitDrops(count: Int, random: Random) = listOf(Items.GOLD_NUGGET.createItemStack(count))

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { ProminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Gold Tree", "金のなる木"),
        description = EnJa("Use a gold ingot on the Prominaria", "プロミナリアに金インゴットを使用する"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("gold_prominaria")) { GoldProminariaBlock.CODEC }.register()
    }
}

class GoldProminariaBlock(settings: Properties) : SimpleMagicPlantBlock(GoldProminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<GoldProminariaBlock> = simpleCodec(::GoldProminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntProperty = BlockStateProperties.AGE_3
}
