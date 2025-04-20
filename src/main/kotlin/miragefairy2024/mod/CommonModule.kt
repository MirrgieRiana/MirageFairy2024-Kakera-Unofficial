package miragefairy2024.mod

import com.google.gson.JsonObject
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.register
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.alchemy.PotionUtils as PotionUtil
import net.minecraft.world.item.alchemy.Potions

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("miragefairy2024"), "MF24KU", "MF24KU",
) { MaterialCard.PHANTOM_DROP.item.createItemStack() }

context(ModContext)
fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()
    WaterBottleIngredient.SERIALIZER.register()
}

object WaterBottleIngredient : CustomIngredient {
    val ID = MirageFairy2024.identifier("water_bottle")
    val SERIALIZER = object : CustomIngredientSerializer<WaterBottleIngredient> {
        override fun getIdentifier() = ID
        override fun read(json: JsonObject) = WaterBottleIngredient
        override fun write(json: JsonObject, ingredient: WaterBottleIngredient) = Unit
        override fun read(buf: FriendlyByteBuf) = WaterBottleIngredient
        override fun write(buf: FriendlyByteBuf, ingredient: WaterBottleIngredient) = Unit
    }

    override fun requiresTesting() = true
    override fun test(stack: ItemStack) = stack.`is`(Items.POTION) && PotionUtil.getPotion(stack) == Potions.WATER
    override fun getMatchingStacks() = listOf(PotionUtil.setPotion(Items.POTION.createItemStack(), Potions.WATER))
    override fun getSerializer() = SERIALIZER
}
