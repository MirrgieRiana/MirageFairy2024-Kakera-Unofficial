package miragefairy2024.mod.machine

import miragefairy2024.MirageFairy2024
import miragefairy2024.util.createItemStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient

object AuraReflectorFurnaceRecipeCard : SimpleMachineRecipeCard<AuraReflectorFurnaceRecipe>() {
    override val identifier = MirageFairy2024.identifier("aura_reflector_furnace")
    override val icon = AuraReflectorFurnaceCard.item.createItemStack()
    override val recipeClass = AuraReflectorFurnaceRecipe::class.java
    override fun createRecipe(recipeId: ResourceLocation, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): AuraReflectorFurnaceRecipe {
        return AuraReflectorFurnaceRecipe(this, recipeId, group, inputs, output, duration)
    }
}

class AuraReflectorFurnaceRecipe(
    card: AuraReflectorFurnaceRecipeCard,
    identifier: ResourceLocation,
    group: String,
    inputs: List<Pair<Ingredient, Int>>,
    output: ItemStack,
    duration: Int,
) : SimpleMachineRecipe(
    card,
    identifier,
    group,
    inputs,
    output,
    duration,
) {
    companion object {
        val FUELS = mutableListOf<Item>()

        init {
            FUELS += Items.SOUL_SAND
            FUELS += Items.SOUL_SOIL
        }
    }
}
