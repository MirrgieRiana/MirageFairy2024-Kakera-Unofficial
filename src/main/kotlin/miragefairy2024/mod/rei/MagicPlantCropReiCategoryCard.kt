package miragefairy2024.mod.rei

import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import miragefairy2024.mod.magicplant.MagicPlantCropNotation
import miragefairy2024.util.compound
import miragefairy2024.util.get
import miragefairy2024.util.list
import miragefairy2024.util.toEntryIngredient
import miragefairy2024.util.toEntryStack
import miragefairy2024.util.toItemStack
import miragefairy2024.util.toNbt
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.castOrThrow
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList

object MagicPlantCropReiCategoryCard : ReiCategoryCard<MagicPlantCropReiCategoryCard.Display>("magic_plant_crop", "Magic Plant Crop", "魔法植物収穫物") {
    override val serializer: BasicDisplay.Serializer<Display> by lazy {
        BasicDisplay.Serializer.ofRecipeLess({ _, _, tag ->
            Display(
                MagicPlantCropNotation(
                    tag.wrapper["Seed"].compound.get()!!.toItemStack(),
                    tag.wrapper["Crops"].list.get()!!.map { it.castOrThrow<NbtCompound>().toItemStack() },
                )
            )
        }, { display, tag ->
            tag.wrapper["Seed"].set(display.recipe.seed.toNbt())
            tag.wrapper["Crops"].set(display.recipe.crops.mapTo(NbtList()) { it.toNbt() })
        })
    }

    class Display(val recipe: MagicPlantCropNotation) : BasicDisplay(listOf(recipe.seed.toEntryStack().toEntryIngredient()), recipe.crops.map { it.toEntryStack().toEntryIngredient() }) {
        override fun getCategoryIdentifier() = identifier
    }
}
