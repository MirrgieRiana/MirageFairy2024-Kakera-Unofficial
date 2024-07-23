package miragefairy2024.util

import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

context(ModContext)
fun Block.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.blockTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Block>.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.blockTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}

context(ModContext)
fun Item.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.itemTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Item>.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.itemTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}

context(ModContext)
fun Identifier.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.biomeTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Biome>.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = ModEvents.onInitialize {
    MirageFairy2024DataGenerator.biomeTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}
