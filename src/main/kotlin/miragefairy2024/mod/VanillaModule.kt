package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.writeAction
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import kotlin.jvm.optionals.getOrElse

enum class BlockTagCard(path: String) {
    CONCRETE("concrete"),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val tag: TagKey<Block> = TagKey.create(Registries.BLOCK, identifier)
}

context(ModContext)
fun initVanillaModule() {

    Blocks.WHITE_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.ORANGE_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.MAGENTA_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.LIGHT_BLUE_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.YELLOW_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.LIME_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.PINK_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.GRAY_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.LIGHT_GRAY_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.CYAN_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.PURPLE_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.BLUE_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.BROWN_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.GREEN_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.RED_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }
    Blocks.BLACK_CONCRETE.registerBlockTagGeneration { BlockTagCard.CONCRETE.tag }


    registerClientDebugItem("dump_biome_tags", Items.STRING, 0x00FF00) { world, player, _, _ ->
        val tags = world.registryAccess().registryOrThrow(Registries.BIOME).getTagNames().toList()
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
