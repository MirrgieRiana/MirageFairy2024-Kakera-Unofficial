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
import net.minecraft.advancements.RequirementsStrategy as CriterionMerger
import net.minecraft.advancements.CriterionTriggerInstance as CriterionConditions
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger as RecipeUnlockedCriterion
import net.minecraft.data.recipes.RecipeBuilder as CraftingRecipeJsonBuilder
import net.minecraft.data.recipes.FinishedRecipe as RecipeJsonProvider
import net.minecraft.world.Container as Inventory
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.network.FriendlyByteBuf as PacketByteBuf
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.core.RegistryAccess as DynamicRegistryManager
import net.minecraft.core.registries.BuiltInRegistries as Registries
import net.minecraft.resources.ResourceLocation as Identifier
import net.minecraft.core.NonNullList as DefaultedList
import net.minecraft.world.level.Level as World
import java.util.function.Consumer

abstract class SimpleMachineRecipeCard<R : SimpleMachineRecipe> {

    abstract val identifier: Identifier

    abstract val icon: ItemStack

    val type = object : RecipeType<R> {
        override fun toString() = identifier.string
    }

    @Suppress("LeakingThis")
    val serializer = SimpleMachineRecipe.Serializer(this)

    abstract val recipeClass: Class<R>

    abstract fun createRecipe(recipeId: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R

    context(ModContext)
    fun init() {
        type.register(Registries.RECIPE_TYPE, identifier)
        serializer.register(Registries.RECIPE_SERIALIZER, identifier)
    }

}

open class SimpleMachineRecipe(
    private val card: SimpleMachineRecipeCard<*>,
    val recipeId: Identifier,
    private val group: String,
    val inputs: List<Pair<Ingredient, Int>>,
    val output: ItemStack,
    val duration: Int,
) : Recipe<Inventory> {

    override fun getGroup() = group

    // TODO 順不同
    override fun matches(inventory: Inventory, world: World): Boolean {
        inputs.forEachIndexed { index, input ->
            if (!input.first.test(inventory.getStack(index))) return false
            if (inventory.getStack(index).count < input.second) return false
        }
        return true
    }

    open fun getCustomizedRemainder(itemStack: ItemStack): ItemStack = itemStack.item.getRecipeRemainder(itemStack)

    override fun getRemainder(inventory: Inventory): DefaultedList<ItemStack> {
        val list = DefaultedList.of<ItemStack>()
        inputs.forEachIndexed { index, input ->
            val remainder = getCustomizedRemainder(inventory.getStack(index))
            if (remainder.isEmpty) return@forEachIndexed

            var totalRemainderCount = remainder.count * input.second
            while (totalRemainderCount > 0) {
                val count = totalRemainderCount atMost remainder.maxCount
                list += remainder.copyWithCount(count)
                totalRemainderCount -= count
            }
        }
        return list
    }

    override fun craft(inventory: Inventory, registryManager: DynamicRegistryManager): ItemStack = output.copy()
    override fun fits(width: Int, height: Int) = width * height >= inputs.size
    override fun getOutput(registryManager: DynamicRegistryManager?) = output
    override fun createIcon() = card.icon
    override fun getId() = recipeId
    override fun getSerializer() = card.serializer
    override fun getType() = card.type

    class Serializer<R : SimpleMachineRecipe>(private val card: SimpleMachineRecipeCard<R>) : RecipeSerializer<R> {
        override fun read(id: Identifier, json: JsonObject): R {
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
                    ItemStack(Registries.ITEM[Identifier(itemId)], count)
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

        override fun read(id: Identifier, buf: PacketByteBuf): R {
            val group = buf.readString()
            val inputCount = buf.readInt()
            val inputs = (0 until inputCount).map {
                Pair(Ingredient.fromPacket(buf), buf.readInt())
            }
            val output = buf.readItemStack()
            val duration = buf.readInt()
            return card.createRecipe(
                recipeId = id,
                group = group,
                inputs = inputs,
                output = output,
                duration = duration,
            )
        }

        override fun write(buf: PacketByteBuf, recipe: R) {
            buf.writeString(recipe.group)
            buf.writeInt(recipe.inputs.size)
            recipe.inputs.forEach {
                it.first.write(buf)
                buf.writeInt(it.second)
            }
            buf.writeItemStack(recipe.output)
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
        builder.offerTo(it, identifier)
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
                card.serializer.write(json, card.createRecipe(recipeId, group, inputs, output, duration))
            }

            override fun getSerializer() = card.serializer
            override fun getRecipeId() = recipeId
            override fun toAdvancementJson() = advancementBuilder.toJson()
            override fun getAdvancementId() = advancementId
        }

        exporter.accept(recipeJsonProvider)
    }
}
