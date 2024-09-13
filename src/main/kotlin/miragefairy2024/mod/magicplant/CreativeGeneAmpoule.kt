package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.description
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.poem
import miragefairy2024.mod.registerPoem
import miragefairy2024.mod.registerPoemGeneration
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import mirrg.kotlin.hydrogen.or
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

val creativeGeneAmpouleItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("creative_gene_ampoule"), "Creative Gene Ampoule", "アカーシャによる生命設計の針",
) { CreativeGeneAmpouleCard.item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(TraitCard.AIR_ADAPTATION.trait, 1))) } }

object CreativeGeneAmpouleCard {
    val item = CreativeGeneAmpouleItem(Item.Settings().maxCount(1))
    val identifier = MirageFairy2024.identifier("creative_gene_ampoule")
}

context(ModContext)
fun initCreativeGeneAmpoule() {
    creativeGeneAmpouleItemGroupCard.init()
    CreativeGeneAmpouleCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(creativeGeneAmpouleItemGroupCard.itemGroupKey) {
            traitRegistry.sortedEntrySet.map { (_, trait) ->
                card.item.createItemStack().also { it.setTraitStacks(TraitStacks.of(TraitStack(trait, 1))) }
            }
        }
        card.item.registerModelGeneration(createCreativeGeneAmpouleModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 1) {
                itemStack.getTraitStacks().or { return@registerColorProvider 0xFFFFFF }.traitStackList.firstOrNull().or { return@registerColorProvider 0xFFFFFF }.trait.primaryEffect.color
            } else {
                0xFFFFFF
            }
        }
        card.item.enJa("Creative Gene Ampoule", "アカーシャによる生命創造の針")
        val poemList = PoemList(99)
            .poem("", "") // TODO
            .description("description1", "Use: Grant the trait", "使用時、特性を付与")
            .description("description2", "Use while sneaking: Remove the trait", "スニーク中に使用時、特性を削除")
            .description("description3", "Use: Increases bits", "使用時、ビットを増加")
            .description("description4", "Use while sneaking: Decreases bits", "スニーク中に使用時、ビットを減少")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)
    }
}

class CreativeGeneAmpouleItem(settings: Settings) : Item(settings) {

}

private fun createCreativeGeneAmpouleModel() = Model {
    ModelData(
        parent = Identifier("item/generated"),
        textures = ModelTexturesData(
            "layer0" to MirageFairy2024.identifier("item/creative_gene_ampoule_background").string,
            "layer1" to MirageFairy2024.identifier("item/creative_gene_ampoule_liquid").string,
        ),
    )
}
