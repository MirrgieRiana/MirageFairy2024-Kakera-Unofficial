package miragefairy2024.mod.haimeviska

import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerItemTagGeneration
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags

context(ModContext)
fun initPlanksFenceHaimeviskaBlock(card: HaimeviskaBlockCard) {

    // 性質
    card.block.registerFlammable(5, 20)

    // タグ
    card.block.registerBlockTagGeneration { BlockTags.WOODEN_FENCES }
    card.item.registerItemTagGeneration { ItemTags.WOODEN_FENCES }

}
