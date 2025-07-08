package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HAIMEVISKA_CONFIGURED_FEATURE_KEY
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import java.util.Optional
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.material.PushReaction as PistonBehavior

class HaimeviskaSaplingBlockCard(configuration: HaimeviskaBlockConfiguration, private val treeGrowerName: ResourceLocation) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings()
        .mapColor(MapColor.PLANT)
        .noCollission()
        .randomTicks()
        .instabreak()
        .sound(BlockSoundGroup.GRASS)
        .pushReaction(PistonBehavior.DESTROY)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = SaplingBlock(
        TreeGrower(treeGrowerName.string, Optional.empty(), Optional.of(HAIMEVISKA_CONFIGURED_FEATURE_KEY), Optional.empty()),
        properties,
    )

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerSingletonBlockStateGeneration()
        block.registerModelGeneration {
            Models.CROSS.with(
                TextureKey.CROSS to "block/" * it.getIdentifier(),
            )
        }
        item.registerBlockGeneratedModelGeneration(block)
        block.registerCutoutRenderLayer()

        // レシピ
        block.registerDefaultLootTableGeneration()
        item.registerComposterInput(0.3F)

        // タグ
        block.registerBlockTagGeneration { BlockTags.SAPLINGS }
        item.registerItemTagGeneration { ItemTags.SAPLINGS }

    }
}
