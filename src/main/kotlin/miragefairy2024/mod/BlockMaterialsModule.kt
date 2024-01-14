package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTagGeneration
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.MapColor
import net.minecraft.data.client.TexturedModel
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.util.Identifier

enum class BlockMaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList,
    mapColor: MapColor,
    hardness: Float,
    resistance: Float,
    requiresTool: Boolean = false,
    dropsNothing: Boolean = false,
    restrictsSpawning: Boolean = false,
    blockSoundGroup: BlockSoundGroup? = null,
    blockCreator: ((AbstractBlock.Settings) -> Block)? = null,
    val tags: List<TagKey<Block>> = listOf(),
    val model: TexturedModel.Factory? = null,
    val isCutoutRenderLayer: Boolean = false,
) {
    MIRANAGITE_BLOCK(
        "miranagite_block", "Miranagite Block", "蒼天石ブロック",
        PoemList(1).poem("Passivation confines discontinuous space", "虚空に導かれし、霊界との接合点。"),
        MapColor.LAPIS_BLUE, 3.0F, 3.0F, requiresTool = true,
        tags = listOf(BlockTags.PICKAXE_MINEABLE, BlockTags.NEEDS_STONE_TOOL),
    ),
    DRYWALL(
        "drywall", "Drywall", "石膏ボード",
        PoemList(1).poem("Please use on the office ceiling, etc.", "オフィスの天井等にどうぞ。"),
        MapColor.PALE_YELLOW, 3.0F, 3.0F,
        tags = listOf(BlockTags.PICKAXE_MINEABLE),
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = run {
        val settings = AbstractBlock.Settings.create()
        settings.mapColor(mapColor)
        if (requiresTool) settings.requiresTool()
        if (dropsNothing) settings.dropsNothing()
        if (restrictsSpawning) settings.allowsSpawning { _, _, _, _ -> false }
        settings.strength(hardness, resistance)
        if (blockSoundGroup != null) settings.sounds(blockSoundGroup)
        if (blockCreator != null) blockCreator(settings) else Block(settings)
    }
    val item = BlockItem(block, Item.Settings())
}

fun initBlockMaterialsModule() {
    BlockMaterialCard.entries.forEach { card ->
        card.block.register(Registries.BLOCK, card.identifier)
        card.item.register(Registries.ITEM, card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroup)

        card.block.registerSingletonBlockStateGeneration()
        if (card.model != null) {
            card.block.registerModelGeneration(card.model)
        } else {
            card.block.registerModelGeneration(TexturedModel.CUBE_ALL)
        }
        if (card.isCutoutRenderLayer) card.block.registerCutoutRenderLayer()

        card.block.enJa(card.enName, card.jaName)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerDefaultLootTableGeneration()

        card.tags.forEach {
            card.block.registerTagGeneration(it)
        }
    }

    registerShapedRecipeGeneration(BlockMaterialCard.MIRANAGITE_BLOCK.item) {
        pattern("###")
        pattern("###")
        pattern("###")
        input('#', MaterialCard.MIRANAGITE.item)
    } on MaterialCard.MIRANAGITE.item from MaterialCard.MIRANAGITE.item
    registerShapelessRecipeGeneration(MaterialCard.MIRANAGITE.item, 9) {
        input(BlockMaterialCard.MIRANAGITE_BLOCK.item)
    } on BlockMaterialCard.MIRANAGITE_BLOCK.item from BlockMaterialCard.MIRANAGITE_BLOCK.item

}
