package miragefairy2024.util

import miragefairy2024.MirageFairy2024DataGenerator
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey

fun Block.registerTagGeneration(tagProvider: () -> TagKey<Block>) = MirageFairy2024DataGenerator.blockTagGenerators {
    it(tagProvider()).add(this)
}

@JvmName("registerTagGenerationOfBlockTag")
fun TagKey<Block>.registerTagGeneration(tagProvider: () -> TagKey<Block>) = MirageFairy2024DataGenerator.blockTagGenerators {
    it(tagProvider()).addOptionalTag(this)
}

fun Item.registerTagGeneration(tagProvider: () -> TagKey<Item>) = MirageFairy2024DataGenerator.itemTagGenerators {
    it(tagProvider()).add(this)
}

@JvmName("registerTagGenerationOfItemTag")
fun TagKey<Item>.registerTagGeneration(tagProvider: () -> TagKey<Item>) = MirageFairy2024DataGenerator.itemTagGenerators {
    it(tagProvider()).addOptionalTag(this)
}
