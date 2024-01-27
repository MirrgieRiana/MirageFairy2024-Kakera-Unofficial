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
import miragefairy2024.util.get
import miragefairy2024.util.green
import miragefairy2024.util.int
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemModelGeneration
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import miragefairy2024.util.yellow
import mirrg.kotlin.hydrogen.or
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.World
import kotlin.math.roundToInt

object FairyCard {
    val enName = "Invalid Fairy"
    val jaName = "無効な妖精"
    val identifier = Identifier(MirageFairy2024.modId, "fairy")
    val item = FairyItem(Item.Settings())
}

private val RARE_TRANSLATION = Translation({ "item.miragefairy2024.fairy.rare" }, "Rare", "レア")
private val MANA_TRANSLATION = Translation({ "item.miragefairy2024.fairy.mana" }, "Mana", "魔力")
private val CONDENSATION_TRANSLATION = Translation({ "item.miragefairy2024.fairy.condensation" }, "Condensation", "凝縮数")
private val CONDENSATION_RECIPE_TRANSLATION = Translation({ "item.miragefairy2024.fairy.condensation_recipe" }, "Can be (de)condensed by crafting table", "作業台で凝縮・展開")

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
            if (tintIndex == 4) {
                val condensation = itemStack.getFairyCondensation()
                when (getNiceCondensation(condensation).first) {
                    0 -> 0xFF8E8E // 赤
                    1 -> 0xB90000
                    2 -> 0xAAAAFF // 青
                    3 -> 0x0000FF
                    4 -> 0x00D100 // 緑
                    5 -> 0x007A00
                    6 -> 0xFFFF60 // 黄色
                    7 -> 0x919100
                    8 -> 0x00D1D1 // 水色
                    9 -> 0x009E9E
                    10 -> 0xFF87FF // マゼンタ
                    11 -> 0xDB00DB
                    12 -> 0xFFBB77 // オレンジ
                    13 -> 0xCE6700
                    14 -> 0x66FFB2 // 草
                    15 -> 0x00B758
                    16 -> 0xD1A3FF // 紫
                    17 -> 0xA347FF
                    18 -> 0xCECECE // 灰色
                    19 -> 0x919191
                    else -> 0x333333
                }
            } else {
                val motif = itemStack.getFairyMotif() ?: return@registerColorProvider 0xFF00FF
                when (tintIndex) {
                    0 -> motif.skinColor
                    1 -> motif.frontColor
                    2 -> motif.backColor
                    3 -> motif.hairColor
                    else -> 0xFF00FF
                }
            }
        }

        card.item.enJa(card.enName, card.jaName)
    }

    RARE_TRANSLATION.enJa()
    MANA_TRANSLATION.enJa()
    CONDENSATION_TRANSLATION.enJa()
    CONDENSATION_RECIPE_TRANSLATION.enJa()
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
    override fun getName(stack: ItemStack): Text {
        val originalName = stack.getFairyMotif()?.displayName ?: super.getName(stack)
        val condensation = stack.getFairyCondensation()
        return if (condensation != 1) text { originalName + " x$condensation"() } else originalName
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val motif = stack.getFairyMotif() ?: return
        tooltip += text { (RARE_TRANSLATION() + " ${motif.rare}"()).aqua }
        tooltip += text { (CONDENSATION_TRANSLATION() + ": x${stack.getFairyCondensation()}"()).green }
        tooltip += text { CONDENSATION_RECIPE_TRANSLATION().yellow }
    }

    override fun isItemBarVisible(stack: ItemStack): Boolean {
        val condensation = stack.getFairyCondensation()
        val niceCondensation = getNiceCondensation(condensation).second
        return condensation != niceCondensation
    }

    override fun getItemBarStep(stack: ItemStack): Int {
        val condensation = stack.getFairyCondensation()
        val niceCondensation = getNiceCondensation(condensation).second.toLong()
        val nextNiceCondensation = niceCondensation * 3L
        return (13.0 * (condensation.toLong() - niceCondensation).toDouble() / (nextNiceCondensation - niceCondensation).toDouble()).roundToInt()
    }

    override fun getItemBarColor(stack: ItemStack) = 0x00FF00
}

fun ItemStack.getFairyMotifId(): Identifier? = this.nbt.or { return null }.wrapper["FairyMotif"].string.get().or { return null }.toIdentifier()
fun ItemStack.getFairyMotif() = this.getFairyMotifId()?.let { motifRegistry.get(it) }

fun ItemStack.setFairyMotifId(identifier: Identifier) = getOrCreateNbt().wrapper["FairyMotif"].string.set(identifier.string)
fun ItemStack.setFairyMotif(recipe: Motif) = this.setFairyMotifId(motifRegistry.getId(recipe)!!)

fun ItemStack.getFairyCondensation() = this.nbt.or { return 1 }.wrapper["FairyCondensation"].int.get() ?: 1
fun ItemStack.setFairyCondensation(condensation: Int) = getOrCreateNbt().wrapper["FairyCondensation"].int.set(condensation)
