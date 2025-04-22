package miragefairy2024.mod.machine

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.util.RecipeGenerationSettings
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.register
import miragefairy2024.util.string
import mirrg.kotlin.gson.hydrogen.JsonWrapper
import mirrg.kotlin.gson.hydrogen.toJsonWrapper
import mirrg.kotlin.hydrogen.atMost
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.Criterion
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.Level
import net.minecraft.advancements.RequirementsStrategy as CriterionMerger
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger as RecipeUnlockedCriterion
import net.minecraft.core.NonNullList as DefaultedList
import net.minecraft.core.RegistryAccess as DynamicRegistryManager
import net.minecraft.data.recipes.FinishedRecipe as RecipeJsonProvider
import net.minecraft.data.recipes.RecipeBuilder as CraftingRecipeJsonBuilder
import net.minecraft.world.Container as Inventory

abstract class SimpleMachineRecipeCard<R : SimpleMachineRecipe> {

    abstract val identifier: ResourceLocation

    abstract val icon: ItemStack

    val type = object : RecipeType<R> {
        override fun toString() = identifier.string
    }

    @Suppress("LeakingThis")
    val serializer = SimpleMachineRecipe.Serializer(this)

    abstract val recipeClass: Class<R>

    abstract fun createRecipe(recipeId: ResourceLocation, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R

    context(ModContext)
    fun init() {
        type.register(BuiltInRegistries.RECIPE_TYPE, identifier)
        serializer.register(BuiltInRegistries.RECIPE_SERIALIZER, identifier)
    }

}

open class SimpleMachineRecipe(
    private val card: SimpleMachineRecipeCard<*>,
    val recipeId: ResourceLocation,
    private val group: String,
    val inputs: List<Pair<Ingredient, Int>>,
    val output: ItemStack,
    val duration: Int,
) : Recipe<Inventory> {

    override fun getGroup() = group

    // TODO 順不同
    override fun matches(inventory: Inventory, world: Level): Boolean {
        inputs.forEachIndexed { index, input ->
            if (!input.first.test(inventory.getItem(index))) return false
            if (inventory.getItem(index).count < input.second) return false
        }
        return true
    }

    open fun getCustomizedRemainder(itemStack: ItemStack): ItemStack = itemStack.item.getRecipeRemainder(itemStack)

    override fun getRemainingItems(inventory: Inventory): DefaultedList<ItemStack> {
        val list = DefaultedList.create<ItemStack>()
        inputs.forEachIndexed { index, input ->
            val remainder = getCustomizedRemainder(inventory.getItem(index))
            if (remainder.isEmpty) return@forEachIndexed

            var totalRemainderCount = remainder.count * input.second
            while (totalRemainderCount > 0) {
                val count = totalRemainderCount atMost remainder.maxStackSize
                list += remainder.copyWithCount(count)
                totalRemainderCount -= count
            }
        }
        return list
    }

    override fun assemble(inventory: Inventory, registryManager: DynamicRegistryManager): ItemStack = output.copy()
    override fun canCraftInDimensions(width: Int, height: Int) = width * height >= inputs.size
    override fun getResultItem(registryManager: DynamicRegistryManager?) = output
    override fun getToastSymbol() = card.icon
    override fun getId() = recipeId
    override fun getSerializer() = card.serializer
    override fun getType() = card.type

    class Serializer<R : SimpleMachineRecipe>(private val card: SimpleMachineRecipeCard<R>) : RecipeSerializer<R> {
        override fun fromJson(id: ResourceLocation, json: JsonObject): R {
            val root = json.toJsonWrapper()
            fun readInput(json: JsonWrapper): Pair<Ingredient, Int> {
                return Pair(
                    if (json["ingredient"].isArray) {
                        Ingredient.fromJson(json["ingredient"].asJsonArray(), false)
                    } else {
                        Ingredient.fromJson(json["ingredient"].asJsonObject(), false)
                    },
                    json["count"].asInt(),
                )
            }
            return card.createRecipe(
                recipeId = id,
                group = root["group"].asString(),
                inputs = root["inputs"].asList().map { input ->
                    readInput(input)
                },
                output = run {
                    val itemId = root["output"]["item"].asString()
                    val count = root["output"]["count"].asInt()
                    ItemStack(BuiltInRegistries.ITEM[ResourceLocation.parse(itemId)], count)
                },
                duration = root["duration"].asInt(),
            )
        }

        fun write(json: JsonObject, recipe: R) {
            fun toJsonInput(input: Pair<Ingredient, Int>): JsonObject {
                val inputJson = JsonObject()
                inputJson.add("ingredient", input.first.toJson())
                inputJson.addProperty("count", input.second)
                return inputJson
            }

            fun toJsonOutput(output: ItemStack): JsonObject {
                val outputJson = JsonObject()
                outputJson.addProperty("item", output.item.getIdentifier().string)
                outputJson.addProperty("count", output.count)
                return outputJson
            }
            json.addProperty("group", recipe.group)
            json.add("inputs", JsonArray().also { inputsJson ->
                recipe.inputs.forEach { input ->
                    inputsJson.add(toJsonInput(input))
                }
            })
            json.add("output", toJsonOutput(recipe.output))
            json.addProperty("duration", recipe.duration)
        }

        override fun fromNetwork(id: ResourceLocation, buf: FriendlyByteBuf): R {
            val group = buf.readUtf()
            val inputCount = buf.readInt()
            val inputs = (0 until inputCount).map {
                Pair(Ingredient.fromNetwork(buf), buf.readInt())
            }
            val output = buf.readItem()
            val duration = buf.readInt()
            return card.createRecipe(
                recipeId = id,
                group = group,
                inputs = inputs,
                output = output,
                duration = duration,
            )
        }

        override fun toNetwork(buf: FriendlyByteBuf, recipe: R) {
            buf.writeUtf(recipe.group)
            buf.writeInt(recipe.inputs.size)
            recipe.inputs.forEach {
                it.first.toNetwork(buf)
                buf.writeInt(it.second)
            }
            buf.writeItem(recipe.output)
            buf.writeInt(recipe.duration)
        }
    }

}

context(ModContext)
fun <R : SimpleMachineRecipe> registerSimpleMachineRecipeGeneration(
    card: SimpleMachineRecipeCard<R>,
    inputs: List<Pair<Ingredient, Int>>,
    output: ItemStack,
    duration: Int,
    block: SimpleMachineRecipeJsonBuilder<R>.() -> Unit = {},
): RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>> {
    val settings = RecipeGenerationSettings<SimpleMachineRecipeJsonBuilder<R>>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = SimpleMachineRecipeJsonBuilder(card, RecipeCategory.MISC, inputs, output, duration)
        builder.group(output.item)
        settings.listeners.forEach { listener ->
            listener(builder)
        }
        block(builder)
        val identifier = settings.idModifiers.fold(output.item.getIdentifier()) { id, idModifier -> idModifier(id) }
        builder.save(it, identifier)
    }
    return settings
}

class SimpleMachineRecipeJsonBuilder<R : SimpleMachineRecipe>(
    private val card: SimpleMachineRecipeCard<R>,
    private val category: RecipeCategory,
    private val inputs: List<Pair<Ingredient, Int>>,
    private val output: ItemStack,
    private val duration: Int,
) : CraftingRecipeJsonBuilder {
    private val advancementBuilder: Advancement.Builder = Advancement.Builder.recipeAdvancement()
    private var group = ""

    override fun unlockedBy(name: String, condition: Criterion<*>) = this.also { advancementBuilder.addCriterion(name, condition) }
    override fun group(string: String?) = this.also { this.group = string ?: "" }
    override fun getResult(): Item = output.item

    override fun save(recipeOutput: RecipeOutput, recipeId: ResourceLocation) {
        check(advancementBuilder.criteria.isNotEmpty()) { "No way of obtaining recipe $recipeId" }

        advancementBuilder
            .parent(CraftingRecipeJsonBuilder.ROOT_RECIPE_ADVANCEMENT)
            .addCriterion("has_the_recipe", RecipeUnlockedCriterion.unlocked(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .requirements(CriterionMerger.OR)

        val advancementId: ResourceLocation = recipeId.withPrefix("recipes/${category.folderName}/")

        val recipeJsonProvider = object : RecipeJsonProvider {
            override fun serializeRecipeData(json: JsonObject) {
                card.serializer.write(json, card.createRecipe(recipeId, group, inputs, output, duration))
            }

            override fun getType() = card.serializer
            override fun getId() = recipeId
            override fun serializeAdvancement() = advancementBuilder.serializeToJson()
            override fun getAdvancementId() = advancementId
        }

        recipeOutput.accept(recipeJsonProvider)
    }
}
