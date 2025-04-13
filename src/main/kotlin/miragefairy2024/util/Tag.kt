package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.entity.EntityType
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.item.Item
import net.minecraft.tags.TagKey
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.structure.Structure

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
fun Identifier.registerStructureTagGeneration(tagProvider: () -> TagKey<Structure>) = DataGenerationEvents.onGenerateStructureTag {
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

context(ModContext)
fun Identifier.registerDamageTypeTagGeneration(tagProvider: () -> TagKey<DamageType>) = DataGenerationEvents.onGenerateDamageTypeTag {
    it(tagProvider()).add(this)
}

context(ModContext)
fun TagKey<DamageType>.registerDamageTypeTagGeneration(tagProvider: () -> TagKey<DamageType>) = DataGenerationEvents.onGenerateDamageTypeTag {
    it(tagProvider()).addOptionalTag(this)
}
