package miragefairy2024.mod

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.platformProxy
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.register
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.core.component.DataComponents
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("miragefairy2024"), "MF24KU", "MF24KU",
) { MaterialCard.PHANTOM_DROP.item().createItemStack() }

context(ModContext)
fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()
    WaterBottleIngredient.SERIALIZER.register()

    platformProxy!!.registerModifyItemEnchantmentsHandler { itemStack, mutableItemEnchantments, enchantmentLookup ->
        val item = itemStack.item as? ModifyItemEnchantmentsHandler ?: return@registerModifyItemEnchantmentsHandler
        item.modifyItemEnchantments(itemStack, mutableItemEnchantments, enchantmentLookup)
    }
}

object WaterBottleIngredient : CustomIngredient {
    val ID = MirageFairy2024.identifier("water_bottle")
    val SERIALIZER = object : CustomIngredientSerializer<WaterBottleIngredient> {
        override fun getIdentifier() = ID
        override fun getCodec(allowEmpty: Boolean): MapCodec<WaterBottleIngredient> = MapCodec.unit(WaterBottleIngredient)
        override fun getPacketCodec(): StreamCodec<RegistryFriendlyByteBuf, WaterBottleIngredient> = StreamCodec.unit(WaterBottleIngredient)
    }

    override fun requiresTesting() = true

    override fun test(stack: ItemStack): Boolean {
        if (stack.`is`(Items.POTION)) {
            val potionContents = stack.get(DataComponents.POTION_CONTENTS) ?: return false
            if (potionContents.`is`(Potions.WATER)) {
                return true
            }
        }
        return false
    }

    override fun getMatchingStacks() = listOf(PotionContents.createItemStack(Items.POTION, Potions.WATER))
    override fun getSerializer() = SERIALIZER
}
