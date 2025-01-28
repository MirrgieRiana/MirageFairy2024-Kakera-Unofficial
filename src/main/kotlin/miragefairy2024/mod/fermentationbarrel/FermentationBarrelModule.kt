package miragefairy2024.mod.fermentationbarrel

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.util.createItemStack
import miragefairy2024.util.from
import miragefairy2024.util.modId
import miragefairy2024.util.on
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient

context(ModContext)
fun initFermentationBarrelModule() {
    FermentationBarrelCard.init()
    FermentationBarrelRecipe.init()

    // TODO
    registerFermentationBarrelRecipeGeneration(
        input1 = Ingredient.ofItems(MaterialCard.HAIMEVISKA_SAP.item),
        inputCount1 = 16,
        input2 = Ingredient.ofItems(Items.POTION), // TODO water only
        inputCount2 = 1,
        output = Items.IRON_INGOT.createItemStack(),
        duration = 200,
    ) modId MirageFairy2024.MOD_ID from MaterialCard.HAIMEVISKA_SAP.item on MaterialCard.HAIMEVISKA_SAP.item

    // TODO
    registerFermentationBarrelRecipeGeneration(
        input1 = Ingredient.ofItems(Items.HONEY_BOTTLE),
        inputCount1 = 4,
        input2 = Ingredient.ofItems(Items.POTION), // TODO water only
        inputCount2 = 1,
        output = Items.COPPER_INGOT.createItemStack(),
        duration = 10,
    ) modId MirageFairy2024.MOD_ID from Items.HONEY_BOTTLE on Items.HONEY_BOTTLE

}
