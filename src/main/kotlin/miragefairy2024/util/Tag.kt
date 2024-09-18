package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

context(ModContext)
fun Block.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = DataGenerationEvents.onGenerateBlockTag {
    it(tagProvider()).add(this)
}

context(ModContext)
fun TagKey<Block>.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = DataGenerationEvents.onGenerateBlockTag {
    it(tagProvider()).addOptionalTag(this)
}

context(ModContext)
fun Item.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = DataGenerationEvents.onGenerateItemTag {
    it(tagProvider()).add(this)
}

context(ModContext)
fun TagKey<Item>.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = DataGenerationEvents.onGenerateItemTag {
    it(tagProvider()).addOptionalTag(this)
}

context(ModContext)
fun Identifier.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = DataGenerationEvents.onGenerateBiomeTag {
    it(tagProvider()).add(this)
}

context(ModContext)
fun TagKey<Biome>.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = DataGenerationEvents.onGenerateBiomeTag {
    it(tagProvider()).addOptionalTag(this)
}

context(ModContext)
fun EntityType<*>.registerEntityTypeTagGeneration(tagProvider: () -> TagKey<EntityType<*>>) = DataGenerationEvents.onGenerateEntityTypeTag {
    it(tagProvider()).add(this)
}

context(ModContext)
fun TagKey<EntityType<*>>.registerEntityTypeTagGeneration(tagProvider: () -> TagKey<EntityType<*>>) = DataGenerationEvents.onGenerateEntityTypeTag {
    it(tagProvider()).addOptionalTag(this)
}
