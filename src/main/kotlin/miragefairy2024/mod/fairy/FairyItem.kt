package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Translation
import miragefairy2024.util.aqua
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import miragefairy2024.util.string
import miragefairy2024.util.text
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World

object FairyCard {
    val enName = "Invalid Fairy"
    val jaName = "無効な妖精"
    val identifier = Identifier(MirageFairy2024.modId, "fairy")
    val item = FairyItem(Item.Settings())
}

val RARE_TRANSLATION = Translation({ "item.miragefairy2024.fairy.rare" }, "Rare", "レア")

fun initFairyItem() {
    FairyCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey) {
            motifRegistry.entrySet.sortedBy { it.key.value }.map {
                val itemStack = card.item.createItemStack()
                itemStack.setFairyMotif(it.value)
                itemStack
            }
        }

        card.item.registerItemModelGeneration(createFairyModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            val motif = itemStack.getFairyMotif() ?: return@registerColorProvider 0xFF00FF
            when (tintIndex) {
                0 -> motif.skinColor
                1 -> motif.frontColor
                2 -> motif.backColor
                3 -> motif.hairColor
                4 -> 0xAA0000
                else -> 0xFF00FF
            }
        }

        card.item.enJa(card.enName, card.jaName)
    }

    RARE_TRANSLATION.enJa()
}

private fun createFairyModel() = Model {
    ModelData(
        parent = Identifier("item/generated"),
        textures = ModelTexturesData(
            "layer0" to Identifier(MirageFairy2024.modId, "item/fairy_skin").string,
            "layer1" to Identifier(MirageFairy2024.modId, "item/fairy_front").string,
            "layer2" to Identifier(MirageFairy2024.modId, "item/fairy_back").string,
            "layer3" to Identifier(MirageFairy2024.modId, "item/fairy_hair").string,
            "layer4" to Identifier(MirageFairy2024.modId, "item/fairy_dress").string,
        ),
    )
}

class FairyItem(settings: Settings) : Item(settings) {
    override fun getName(stack: ItemStack): Text = stack.getFairyMotif()?.displayName ?: super.getName(stack)
    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val motif = stack.getFairyMotif() ?: return
        tooltip += text { (RARE_TRANSLATION() + " ${motif.rare}"()).aqua }
    }
}

fun ItemStack.getFairyMotifId(): Identifier? {
    val nbt = this.nbt ?: return null
    val id = nbt.getString("Motif") ?: return null
    return Identifier(id)
}

fun ItemStack.getFairyMotif() = this.getFairyMotifId()?.let { motifRegistry.get(it) }

fun ItemStack.setFairyMotifId(identifier: Identifier) {
    getOrCreateNbt().putString("Motif", identifier.string)
}

fun ItemStack.setFairyMotif(recipe: Motif) = this.setFairyMotifId(motifRegistry.getId(recipe)!!)