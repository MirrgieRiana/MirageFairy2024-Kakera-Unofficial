package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.from
import miragefairy2024.util.on
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.MapColor

fun createPlankSettings(sound: Boolean = true) = createBaseWoodSetting(sound = sound).strength(2.0F, 3.0F).mapColor(MapColor.RAW_IRON)

class HaimeviskaPlanksBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = createBaseWoodSetting()
        .strength(2.0F, 3.0F)
        .mapColor(MapColor.RAW_IRON)
        .let { Block(it) }

    context(ModContext)
    override fun init() {
        super.init()

        block.registerDefaultLootTableGeneration()
        registerShapelessRecipeGeneration(item, 4) {
            requires(LOG.item())
        } on LOG.item from LOG.item

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.PLANKS }
        item.registerItemTagGeneration { ItemTags.PLANKS }

    }
}

class HaimeviskaBricksBlockCard(configuration: HaimeviskaBlockConfiguration, private val input: () -> Item) : AbstractHaimeviskaBlockCard(configuration) {
    override suspend fun createBlock() = createBaseWoodSetting()
        .strength(2.0F, 3.0F)
        .mapColor(MapColor.RAW_IRON)
        .let { Block(it) }

    context(ModContext)
    override fun init() {
        super.init()

        block.registerDefaultLootTableGeneration()
        registerShapedRecipeGeneration(item, 4) {
            pattern("##")
            pattern("##")
            define('#', input())
        } on input from input

        // 性質
        block.registerFlammable(5, 20)

        // タグ
        block.registerBlockTagGeneration { BlockTags.PLANKS }
        item.registerItemTagGeneration { ItemTags.PLANKS }

    }
}
