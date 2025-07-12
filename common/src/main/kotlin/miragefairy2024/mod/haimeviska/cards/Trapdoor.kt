package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType

class HaimeviskaTrapdoorBlockCard(configuration: HaimeviskaBlockConfiguration, private val blockSetType: () -> BlockSetType, private val parent: () -> Block) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings()
        .strength(3.0F)
        .noOcclusion()
        .isValidSpawn(Blocks::never)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = TrapDoorBlock(blockSetType(), properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(parent) { it.trapdoor(block()) }
        block.registerDefaultLootTableGeneration()

        // レンダリング
        block.registerCutoutRenderLayer()

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_TRAPDOORS }
        item.registerItemTagGeneration { ItemTags.WOODEN_TRAPDOORS }

    }
}
