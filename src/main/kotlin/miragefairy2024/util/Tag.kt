package miragefairy2024.util

import miragefairy2024.InitializationContext
import miragefairy2024.MirageFairy2024DataGenerator
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

context(InitializationContext)
fun Block.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = MirageFairy2024DataGenerator.blockTagGenerators {
    it(tagProvider()).add(this)
}

context(InitializationContext)
fun TagKey<Block>.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = MirageFairy2024DataGenerator.blockTagGenerators {
    it(tagProvider()).addOptionalTag(this)
}

context(InitializationContext)
fun Item.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = MirageFairy2024DataGenerator.itemTagGenerators {
    it(tagProvider()).add(this)
}

context(InitializationContext)
fun TagKey<Item>.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = MirageFairy2024DataGenerator.itemTagGenerators {
    it(tagProvider()).addOptionalTag(this)
}

context(InitializationContext)
fun Identifier.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = MirageFairy2024DataGenerator.biomeTagGenerators {
    it(tagProvider()).add(this)
}

context(InitializationContext)
fun TagKey<Biome>.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = MirageFairy2024DataGenerator.biomeTagGenerators {
    it(tagProvider()).addOptionalTag(this)
}
