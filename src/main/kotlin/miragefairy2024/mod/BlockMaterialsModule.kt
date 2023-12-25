package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.util.criterion
import miragefairy2024.util.enJa
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.register
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.registerTagGeneration
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.MapColor
import net.minecraft.data.client.TexturedModel
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier

enum class BlockMaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: List<Poem>,
    mapColor: MapColor,
) {
    MIRANAGITE_BLOCK(
        "miranagite_block", "Miranagite Block", "蒼天石ブロック",
        listOf(Poem("Passivation confines discontinuous space", "虚空に導かれし、霊界との接合点。")),
        MapColor.LAPIS_BLUE,
    )
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val block = AbstractBlock.Settings.create()
        .mapColor(mapColor)
        .requiresTool()
        .strength(3.0F, 3.0F)
        .let { Block(it) }
    val item = BlockItem(block, Item.Settings())
}

fun initBlockMaterialsModule() {
    BlockMaterialCard.entries.forEach { card ->
        card.block.register(card.identifier)
        card.item.register(card.identifier)

        card.item.registerItemGroup(mirageFairy2024ItemGroup)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration(TexturedModel.CUBE_ALL)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(card.enName, card.jaName)
        card.item.registerPoem(card.poemList)
        card.item.registerPoemGeneration(card.poemList)

        card.block.registerDefaultLootTableGeneration()

        card.block.registerTagGeneration(BlockTags.PICKAXE_MINEABLE)
        card.block.registerTagGeneration(BlockTags.NEEDS_STONE_TOOL)
    }

    MirageFairy2024DataGenerator.recipeGenerators {
        val high = BlockMaterialCard.MIRANAGITE_BLOCK.item
        val low = MaterialCard.MIRANAGITE.item
        ShapedRecipeJsonBuilder
            .create(RecipeCategory.MISC, high, 1)
            .group(high)
            .input('#', low)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .criterion(low)
            .offerTo(it, Identifier.of(MirageFairy2024.modId, "${high.getIdentifier().path}_from_${low.getIdentifier().path}"))
        ShapelessRecipeJsonBuilder
            .create(RecipeCategory.MISC, low, 9)
            .group(low)
            .input(high)
            .criterion(high)
            .offerTo(it, Identifier.of(MirageFairy2024.modId, "${low.getIdentifier().path}_from_${high.getIdentifier().path}"))
    }

}
