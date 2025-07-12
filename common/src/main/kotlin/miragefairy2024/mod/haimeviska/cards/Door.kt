package miragefairy2024.mod.haimeviska.cards

import miragefairy2024.ModContext
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.util.registerBlockFamily
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerLootTableGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.DoubleHighBlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.material.PushReaction

class HaimeviskaDoorBlockCard(configuration: HaimeviskaBlockConfiguration, private val blockSetType: () -> BlockSetType, private val parent: () -> Block) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createPlankSettings()
        .strength(3.0F)
        .noOcclusion()
        .pushReaction(PushReaction.DESTROY)

    override suspend fun createBlock(properties: BlockBehaviour.Properties) = DoorBlock(blockSetType(), properties)
    override suspend fun createItem(block: Block, properties: Item.Properties) = DoubleHighBlockItem(block, properties)

    context(ModContext)
    override fun init() {
        super.init()

        registerBlockFamily(parent) { it.door(block()) }
        block.registerLootTableGeneration { it, _ -> it.createDoorTable(block()) }

        // レンダリング
        block.registerCutoutRenderLayer()

        // タグ
        block.registerBlockTagGeneration { BlockTags.WOODEN_DOORS }
        item.registerItemTagGeneration { ItemTags.WOODEN_DOORS }

    }
}
