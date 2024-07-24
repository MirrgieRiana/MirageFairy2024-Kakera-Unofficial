package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

context(ModContext)
fun Block.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = ModEvents.onInitialize {
    DataGenerationEvents.blockTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Block>.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = ModEvents.onInitialize {
    DataGenerationEvents.blockTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}

context(ModContext)
fun Item.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = ModEvents.onInitialize {
    DataGenerationEvents.itemTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Item>.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = ModEvents.onInitialize {
    DataGenerationEvents.itemTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}

context(ModContext)
fun Identifier.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = ModEvents.onInitialize {
    DataGenerationEvents.biomeTagGenerators {
        it(tagProvider()).add(this)
    }
}

context(ModContext)
fun TagKey<Biome>.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = ModEvents.onInitialize {
    DataGenerationEvents.biomeTagGenerators {
        it(tagProvider()).addOptionalTag(this)
    }
}
