package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.registerBlockTagGeneration
import miragefairy2024.util.registerClientDebugItem
import miragefairy2024.util.writeAction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.Items
import net.minecraft.core.registries.Registries as RegistryKeys
import net.minecraft.tags.TagKey
import kotlin.jvm.optionals.getOrElse

enum class BlockTagCard(path: String) {
    CONCRETE("concrete"),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val tag: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, identifier)
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
        val tags = world.registryManager.get(RegistryKeys.BIOME).streamTags().toList()
        val sb = StringBuilder()
        tags.sortedBy { it.id }.forEach { tag ->
            sb.append("${tag.id}\n")
            val biomes = world.registryManager.get(RegistryKeys.BIOME).getEntryList(tag).getOrElse { listOf() }.toList()
            biomes.sortedBy { it.key.get().value }.forEach { biome ->
                sb.append("  ${biome.key.get().value}\n")
            }
        }
        writeAction(player, "dump_biome_tags.txt", sb.toString())
    }

}
