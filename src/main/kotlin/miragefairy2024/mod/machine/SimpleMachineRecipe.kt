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
import kotlin.jvm.optionals.getOrNull

abstract class SimpleMachineRecipeCard<R : SimpleMachineRecipe> {
    abstract val identifier: Identifier
    abstract val icon: ItemStack

    val type = object : RecipeType<R> {
        override fun toString() = identifier.string
    }

    val serializer = object : SimpleMachineRecipe.Serializer<R>() {
        override fun createRecipe(recipeIdentifier: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R {
            return this@SimpleMachineRecipeCard.createRecipe(recipeIdentifier, group, inputs, output, duration)
        }
    }

    abstract val recipeClass: Class<R>

    abstract fun createRecipe(recipeIdentifier: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R

    fun match(world: World, inventory: Inventory) = world.recipeManager.getFirstMatch(type, inventory, world).getOrNull()

    context(ModContext)
    fun init() {
        serializer.register(Registries.RECIPE_SERIALIZER, identifier)
    }
}

abstract class SimpleMachineRecipe(
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

    abstract class Serializer<R : SimpleMachineRecipe> : RecipeSerializer<R> {
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
            return createRecipe(
                recipeIdentifier = id,
                group = root["group"].asString(),
                inputs = root["inputs"].asList().map { readInput(it) },
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
            json.add("inputs", JsonArray().also { inputs ->
                recipe.inputs.forEach { input ->
                    inputs.add(toJsonInput(input))
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
            return createRecipe(
                recipeIdentifier = id,
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

        abstract fun createRecipe(recipeIdentifier: Identifier, group: String, inputs: List<Pair<Ingredient, Int>>, output: ItemStack, duration: Int): R
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
