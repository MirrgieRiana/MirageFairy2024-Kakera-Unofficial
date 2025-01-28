package miragefairy2024.mod.fermentationbarrel

import com.google.gson.JsonObject
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.RecipeGenerationSettings
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.register
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.toJsonWrapper
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementRewards
import net.minecraft.advancement.CriterionMerger
import net.minecraft.advancement.criterion.CriterionConditions
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder
import net.minecraft.data.server.recipe.RecipeJsonProvider
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import net.minecraft.world.World
import java.util.function.Consumer

// TODO 空き瓶の入力
// TODO 水ボトルのingredient
// TODO 水ボトルは空き瓶をremainderで返さない
class FermentationBarrelRecipe(
    val identifier: Identifier,
    private val group: String,
    val input1: Ingredient,
    val inputCount1: Int,
    val input2: Ingredient,
    val inputCount2: Int,
    val output: ItemStack,
    val duration: Int,
) : Recipe<Inventory> {
    companion object {
        val IDENTIFIER = MirageFairy2024.identifier("fermentation_barrel")
        val TYPE: RecipeType<FermentationBarrelRecipe> = object : RecipeType<FermentationBarrelRecipe> {
            override fun toString() = MirageFairy2024.identifier("fermentation_barrel").string
        }

        context(ModContext)
        fun init() {
            TYPE.register(Registries.RECIPE_TYPE, IDENTIFIER)
            Serializer.register(Registries.RECIPE_SERIALIZER, IDENTIFIER)
        }
    }

    override fun getGroup() = group

    override fun matches(inventory: Inventory, world: World): Boolean {
        if (!input1.test(inventory.getStack(0))) return false
        if (inventory.getStack(0).count < inputCount1) return false
        if (!input2.test(inventory.getStack(1))) return false
        if (inventory.getStack(1).count < inputCount2) return false
        return true
    }

    override fun getRemainder(inventory: Inventory): DefaultedList<ItemStack> {
        val list = DefaultedList.of<ItemStack>()

        fun f(index: Int, inputCount: Int) {
            val itemStack = inventory.getStack(index)
            val remainder = itemStack.item.getRecipeRemainder(itemStack)
            if (remainder.isEmpty) return

            var totalRemainderCount = remainder.count * inputCount
            while (totalRemainderCount > 0) {
                val count = totalRemainderCount atMost remainder.maxCount
                list += remainder.copyWithCount(count)
                totalRemainderCount -= count
            }
        }

        f(0, inputCount1)
        f(1, inputCount2)

        return list
    }

    override fun craft(inventory: Inventory?, registryManager: DynamicRegistryManager?): ItemStack = output.copy()
    override fun fits(width: Int, height: Int) = width * height >= 2
    override fun getOutput(registryManager: DynamicRegistryManager?) = output
    override fun createIcon(): ItemStack = FermentationBarrelCard.item.createItemStack()
    override fun getId() = identifier
    override fun getSerializer() = Serializer
    override fun getType() = TYPE

    object Serializer : RecipeSerializer<FermentationBarrelRecipe> {
        override fun read(id: Identifier, json: JsonObject): FermentationBarrelRecipe {
            val root = json.toJsonWrapper()
            return FermentationBarrelRecipe(
                identifier = id,
                group = root["group"].asString(),
                input1 = if (root["input1"].isArray) {
                    Ingredient.fromJson(root["input1"].asJsonArray(), false)
                } else {
                    Ingredient.fromJson(root["input1"].asJsonObject(), false)
                },
                inputCount1 = root["input_count1"].asInt(),
                input2 = if (root["input2"].isArray) {
                    Ingredient.fromJson(root["input2"].asJsonArray(), false)
                } else {
                    Ingredient.fromJson(root["input2"].asJsonObject(), false)
                },
                inputCount2 = root["input_count2"].asInt(),
                output = run {
                    val itemId = root["output"].asString()
                    val count = root["output_count"].asInt()
                    ItemStack(Registries.ITEM[Identifier(itemId)], count)
                },
                duration = root["duration"].asInt(),
            )
        }

        fun write(json: JsonObject, recipe: FermentationBarrelRecipe) {
            json.addProperty("group", recipe.group ?: "")
            json.add("input1", recipe.input1.toJson())
            json.addProperty("input_count1", recipe.inputCount1)
            json.add("input2", recipe.input2.toJson())
            json.addProperty("input_count2", recipe.inputCount2)
            json.addProperty("output", recipe.output.item.getIdentifier().string)
            json.addProperty("output_count", recipe.output.count)
            json.addProperty("duration", recipe.duration)
        }

        override fun read(id: Identifier, buf: PacketByteBuf): FermentationBarrelRecipe {
            val group = buf.readString()
            val input1 = Ingredient.fromPacket(buf)
            val inputCount1 = buf.readInt()
            val input2 = Ingredient.fromPacket(buf)
            val inputCount2 = buf.readInt()
            val output = buf.readItemStack()
            val duration = buf.readInt()
            return FermentationBarrelRecipe(
                identifier = id,
                group = group,
                input1 = input1,
                inputCount1 = inputCount1,
                input2 = input2,
                inputCount2 = inputCount2,
                output = output,
                duration = duration,
            )
        }

        override fun write(buf: PacketByteBuf, recipe: FermentationBarrelRecipe) {
            buf.writeString(recipe.group)
            recipe.input1.write(buf)
            buf.writeInt(recipe.inputCount1)
            recipe.input2.write(buf)
            buf.writeInt(recipe.inputCount2)
            buf.writeItemStack(recipe.output)
            buf.writeInt(recipe.duration)
        }
    }

}


context(ModContext)
fun registerFermentationBarrelRecipeGeneration(
    input1: Ingredient,
    inputCount1: Int,
    input2: Ingredient,
    inputCount2: Int,
    output: ItemStack,
    duration: Int,
    block: FermentationBarrelRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<FermentationBarrelRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<FermentationBarrelRecipeJsonBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = FermentationBarrelRecipeJsonBuilder(RecipeCategory.MISC, input1, inputCount1, input2, inputCount2, output, duration)
        builder.group(output.item)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output.item.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.offerTo(it, identifier)
    }
    return settings
}

class FermentationBarrelRecipeJsonBuilder(
    private val category: RecipeCategory,
    private val input1: Ingredient,
    private val inputCount1: Int,
    private val input2: Ingredient,
    private val inputCount2: Int,
    private val output: ItemStack,
    private val duration: Int,
) : CraftingRecipeJsonBuilder {
    private val advancementBuilder: Advancement.Builder = Advancement.Builder.createUntelemetered()
    private var group = ""

    override fun criterion(name: String, conditions: CriterionConditions) = this.also { advancementBuilder.criterion(name, conditions) }
    override fun group(string: String?) = this.also { this.group = string ?: "" }
    override fun getOutputItem(): Item = output.item

    override fun offerTo(exporter: Consumer<RecipeJsonProvider>, recipeId: Identifier) {
        check(advancementBuilder.criteria.isNotEmpty()) { "No way of obtaining recipe $recipeId" }

        advancementBuilder
            .parent(CraftingRecipeJsonBuilder.ROOT)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(CriterionMerger.OR)

        val advancementId: Identifier = recipeId.withPrefixedPath("recipes/${category.getName()}/")

        val recipeJsonProvider = object : RecipeJsonProvider {
            override fun serialize(json: JsonObject) {
                FermentationBarrelRecipe.Serializer.write(json, FermentationBarrelRecipe(recipeId, group, input1, inputCount1, input2, inputCount2, output, duration))
            }

            override fun getSerializer() = FermentationBarrelRecipe.Serializer
            override fun getRecipeId() = recipeId
            override fun toAdvancementJson() = advancementBuilder.toJson()
            override fun getAdvancementId() = advancementId
        }

        exporter.accept(recipeJsonProvider)
    }
}
