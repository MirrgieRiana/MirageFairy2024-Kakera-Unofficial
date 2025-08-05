package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.materials.item.Material
import miragefairy2024.mod.materials.item.Shape
import miragefairy2024.mod.materials.item.Tag
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import miragefairy2024.util.toTextureSource
import miragefairy2024.util.writeAction
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import kotlin.jvm.optionals.getOrElse

enum class ItemTagCard(identifier: ResourceLocation) {
    ECHO_SHARDS(ResourceLocation.fromNamespaceAndPath("c", "echo_shards")),
    PRISMARINE_SHARDS(ResourceLocation.fromNamespaceAndPath("c", "prismarine_shards")),
    ;

    val tag = identifier.toItemTag()
}

enum class BlockTagCard(identifier: ResourceLocation) {
    CONCRETE(MirageFairy2024.identifier("concrete")),
    ;

    val tag = identifier.toBlockTag()
}

context(ModContext)
fun initVanillaModule() {

    ({ Items.FLINT }).registerItemTagGeneration { Tag(Shape.GEM, Material.FLINT) }
    ({ Items.ECHO_SHARD }).registerItemTagGeneration { ItemTagCard.ECHO_SHARDS.tag }
    ({ Items.AMETHYST_SHARD }).registerItemTagGeneration { Tag(Shape.SHARD, Material.AMETHYST) }
    ({ Items.PRISMARINE_SHARD }).registerItemTagGeneration { ItemTagCard.PRISMARINE_SHARDS.tag }

    ({ Blocks.WHITE_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.ORANGE_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.MAGENTA_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.LIGHT_BLUE_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.YELLOW_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.LIME_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.PINK_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.GRAY_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.LIGHT_GRAY_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.CYAN_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.PURPLE_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.BLUE_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.BROWN_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.GREEN_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.RED_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    ({ Blocks.BLACK_CONCRETE }).registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }


    registerClientDebugItem("dump_biome_tags", Items.STRING.toTextureSource(), 0xFF00FF00.toInt()) { world, player, _, _ ->
        val tags = world.registryAccess().registryOrThrow(Registries.BIOME).tagNames.toList()
        val sb = StringBuilder()
        tags.sortedBy { it.location() }.forEach { tag ->
            sb.append("${tag.location()}\n")
            val biomes = world.registryAccess().registryOrThrow(Registries.BIOME).getTag(tag).getOrElse { listOf() }.toList()
            biomes.sortedBy { it.unwrapKey().get().location() }.forEach { biome ->
                sb.append("  ${biome.unwrapKey().get().location()}\n")
            }
        }
        writeAction(player, "dump_biome_tags.txt", sb.toString())
    }

}
