package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey

context(ModContext)
fun initPlanksFenceGateHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.FENCE_GATES }
    card.item.registerItemTagGeneration { ItemTags.FENCE_GATES }
    card.block.registerBlockTagGeneration { TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }
    card.item.registerItemTagGeneration { TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", "fence_gates/wooden")) }

}
