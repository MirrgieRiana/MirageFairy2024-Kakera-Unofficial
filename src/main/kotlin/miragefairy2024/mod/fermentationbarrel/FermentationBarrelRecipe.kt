package miragefairy2024.mod.fermentationbarrel

import com.google.gson.JsonObject
import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.RecipeGenerationSettings
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.group
import miragefairy2024.util.isNotEmpty
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
import net.minecraft.item.Items
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

class FermentationBarrelRecipe(
    val identifier: Identifier,
    private val group: String,
    val input1: Pair<Ingredient, Int>,
    val input2: Pair<Ingredient, Int>,
    val input3: Pair<Ingredient, Int>,
    val output: ItemStack,
    val duration: Int,
) : Recipe<Inventory> {
    companion object {
        val IDENTIFIER = MirageFairy2024.identifier("fermentation_barrel")
        val TYPE: RecipeType<FermentationBarrelRecipe> = object : RecipeType<FermentationBarrelRecipe> {
            override fun toString() = MirageFairy2024.identifier("fermentation_barrel").string
        }

        context (ModContext)
        fun init() {
            TYPE.register(Registries.RECIPE_TYPE, IDENTIFIER)
            Serializer.register(Registries.RECIPE_SERIALIZER, IDENTIFIER)
        }

        fun getCustomizedRemainder(itemStack: ItemStack): ItemStack {
            val remainder = itemStack.item.getRecipeRemainder(itemStack)
            if (remainder.isNotEmpty) return remainder

            if (itemStack.isOf(Items.POTION)) return Items.GLASS_BOTTLE.createItemStack()

            return EMPTY_ITEM_STACK
        }
    }

    val inputs = listOf(input1, input2, input3)

    override fun getGroup() = group

    override fun matches(inventory: Inventory, world: World): Boolean {
        inputs.forEachIndexed { index, input ->
            if (!input.first.test(inventory.getStack(index))) return false
            if (inventory.getStack(index).count < input.second) return false
        }
        return true
    }

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

    override fun craft(inventory: Inventory?, registryManager: DynamicRegistryManager?): ItemStack = output.copy()
    override fun fits(width: Int, height: Int) = width * height >= 3
    override fun getOutput(registryManager: DynamicRegistryManager?) = output
    override fun createIcon(): ItemStack = FermentationBarrelCard.item.createItemStack()
    override fun getId() = identifier
    override fun getSerializer() = Serializer
    override fun getType() = TYPE

    object Serializer : RecipeSerializer<FermentationBarrelRecipe> {
        override fun read(id: Identifier, json: JsonObject): FermentationBarrelRecipe {
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
            return FermentationBarrelRecipe(
                identifier = id,
                group = root["group"].asString(),
                input1 = readInput(root["input1"]),
                input2 = readInput(root["input2"]),
                input3 = readInput(root["input3"]),
                output = run {
                    val itemId = root["output"]["item"].asString()
                    val count = root["output"]["count"].asInt()
                    ItemStack(Registries.ITEM[Identifier(itemId)], count)
                },
                duration = root["duration"].asInt(),
            )
        }

        fun write(json: JsonObject, recipe: FermentationBarrelRecipe) {
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
            json.add("input1", toJsonInput(recipe.input1))
            json.add("input2", toJsonInput(recipe.input2))
            json.add("input3", toJsonInput(recipe.input3))
            json.add("output", toJsonOutput(recipe.output))
            json.addProperty("duration", recipe.duration)
        }

        override fun read(id: Identifier, buf: PacketByteBuf): FermentationBarrelRecipe {
            val group = buf.readString()
            val input1 = Pair(Ingredient.fromPacket(buf), buf.readInt())
            val input2 = Pair(Ingredient.fromPacket(buf), buf.readInt())
            val input3 = Pair(Ingredient.fromPacket(buf), buf.readInt())
            val output = buf.readItemStack()
            val duration = buf.readInt()
            return FermentationBarrelRecipe(
                identifier = id,
                group = group,
                input1 = input1,
                input2 = input2,
                input3 = input3,
                output = output,
                duration = duration,
            )
        }

        override fun write(buf: PacketByteBuf, recipe: FermentationBarrelRecipe) {
            buf.writeString(recipe.group)
            recipe.input1.first.write(buf)
            buf.writeInt(recipe.input1.second)
            recipe.input2.first.write(buf)
            buf.writeInt(recipe.input2.second)
            recipe.input3.first.write(buf)
            buf.writeInt(recipe.input3.second)
            buf.writeItemStack(recipe.output)
            buf.writeInt(recipe.duration)
        }
    }

}


context(ModContext)
fun registerFermentationBarrelRecipeGeneration(
    input1: Pair<Ingredient, Int>,
    input2: Pair<Ingredient, Int>,
    input3: Pair<Ingredient, Int>,
    output: ItemStack,
    duration: Int,
    block: FermentationBarrelRecipeJsonBuilder.() -> Unit = {},
): RecipeGenerationSettings<FermentationBarrelRecipeJsonBuilder> {
    val settings = RecipeGenerationSettings<FermentationBarrelRecipeJsonBuilder>()
    DataGenerationEvents.onGenerateRecipe {
        val builder = FermentationBarrelRecipeJsonBuilder(RecipeCategory.MISC, input1, input2, input3, output, duration)
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
    private val input1: Pair<Ingredient, Int>,
    private val input2: Pair<Ingredient, Int>,
    private val input3: Pair<Ingredient, Int>,
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
                FermentationBarrelRecipe.Serializer.write(json, FermentationBarrelRecipe(recipeId, group, input1, input2, input3, output, duration))
            }

            override fun getSerializer() = FermentationBarrelRecipe.Serializer
            override fun getRecipeId() = recipeId
            override fun toAdvancementJson() = advancementBuilder.toJson()
            override fun getAdvancementId() = advancementId
        }

        exporter.accept(recipeJsonProvider)
    }
}
