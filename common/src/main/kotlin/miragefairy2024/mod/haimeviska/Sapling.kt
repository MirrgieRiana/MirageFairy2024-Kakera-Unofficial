package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.level.block.SoundType as BlockSoundGroup
import net.minecraft.world.level.block.state.BlockBehaviour as AbstractBlock
import net.minecraft.world.level.material.PushReaction as PistonBehavior

fun createSaplingSettings() = AbstractBlock.Properties.of().mapColor(MapColor.PLANT).noCollission().randomTicks().instabreak().sound(BlockSoundGroup.GRASS).pushReaction(PistonBehavior.DESTROY)

context(ModContext)
fun initSaplingHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // レンダリング
    card.block.registerSingletonBlockStateGeneration()
    card.block.registerModelGeneration {
        Models.CROSS.with(
            TextureKey.CROSS to "block/" * it.getIdentifier(),
        )
    }
    card.item.registerBlockGeneratedModelGeneration(card.block)
    card.block.registerCutoutRenderLayer()

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.SAPLINGS }
    card.item.registerItemTagGeneration { ItemTags.SAPLINGS }

}
