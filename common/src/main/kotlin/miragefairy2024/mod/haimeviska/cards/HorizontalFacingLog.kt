package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.haimeviska.createBaseWoodSetting
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.normal
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.times
import miragefairy2024.util.with
import miragefairy2024.util.withHorizontalRotation
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.minecraft.data.models.model.ModelTemplates as Models
import net.minecraft.data.models.model.TextureSlot as TextureKey
import net.minecraft.world.level.block.HorizontalDirectionalBlock as HorizontalFacingBlock

abstract class HaimeviskaHorizontalFacingLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F).mapColor(MapColor.RAW_IRON)

    context(ModContext)
    override fun init() {
        super.init()

        // レンダリング
        block.registerVariantsBlockStateGeneration { normal("block/" * block().getIdentifier()).withHorizontalRotation(HorizontalFacingBlock.FACING) }
        block.registerModelGeneration {
            Models.CUBE_ORIENTABLE.with(
                TextureKey.TOP to "block/" * LOG.block().getIdentifier() * "_top",
                TextureKey.SIDE to "block/" * LOG.block().getIdentifier(),
                TextureKey.FRONT to "block/" * it.getIdentifier(),
            )
        }

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
        HAIMEVISKA_LOGS_BLOCK_TAG.generator.registerChild(block)
        HAIMEVISKA_LOGS_ITEM_TAG.generator.registerChild(item)

    }
}
