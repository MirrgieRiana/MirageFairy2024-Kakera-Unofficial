package miragefairy2024.mod.fairyquest

import com.google.gson.JsonObject
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.mirageFairy2024ItemGroupCard
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.on
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.string
import miragefairy2024.util.text
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import mirrg.kotlin.hydrogen.or
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

object FairyQuestCardCard {
    val enName = "Broken Fairy Quest Card"
    val jaName = "破損したフェアリークエストカード"
    val identifier = Identifier(MirageFairy2024.modId, "fairy_quest_card")
    val item = FairyQuestCardItem(Item.Settings())
}

private val fairyQuestCardFairyQuestTranslation = Translation({ FairyQuestCardCard.item.translationKey + ".format" }, "“%s”", "『%s』")

context(ModContext)
fun initFairyQuestCardItem() {
    FairyQuestCardCard.let { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey) {
            fairyQuestRecipeRegistry.sortedEntrySet.map {
                val itemStack = card.item.createItemStack()
                itemStack.setFairyQuestRecipe(it.value)
                itemStack
            }
        }
        card.item.registerModelGeneration(createFairyQuestCardModel())
        card.item.registerColorProvider { itemStack, tintIndex ->
            if (tintIndex == 0) {
                itemStack.getFairyQuestRecipe()?.color ?: 0xFF00FF
            } else {
                0xFFFFFF
            }
        }
        card.item.enJa(card.enName, card.jaName)
    }

    fairyQuestCardFairyQuestTranslation.enJa()

    registerShapelessRecipeGeneration(MaterialCard.FAIRY_QUEST_CARD_BASE.item) {
        input(FairyQuestCardIngredient.toVanilla())
    } on FairyQuestCardCard.item from FairyQuestCardCard.item

    ModEvents.onInitialize {
        CustomIngredientSerializer.register(FairyQuestCardIngredient.SERIALIZER)
    }
}

class FairyQuestCardItem(settings: Settings) : Item(settings) {
    override fun getName(stack: ItemStack): Text {
        val recipe = stack.getFairyQuestRecipe() ?: return super.getName(stack).red
        return fairyQuestCardFairyQuestTranslation(recipe.title)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        val recipeId = stack.getFairyQuestRecipeId()
        if (recipeId == null) {
            tooltip += text { "null"() }
        } else {
            if (fairyQuestRecipeRegistry.containsId(recipeId)) {
                // nop
            } else {
                tooltip += text { recipeId.string() }
            }
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val itemStack = user.getStackInHand(hand)
        val recipe = itemStack.getFairyQuestRecipe() ?: return TypedActionResult.fail(itemStack)
        if (world.isClient) return TypedActionResult.success(itemStack)
        user.openHandledScreen(object : ExtendedScreenHandlerFactory {
            override fun createMenu(syncId: Int, playerInventory: PlayerInventory, player: PlayerEntity): ScreenHandler {
                return FairyQuestCardScreenHandler(syncId, playerInventory, recipe, ScreenHandlerContext.create(world, user.blockPos))
            }

            override fun getDisplayName() = recipe.title

            override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
                buf.writeString(fairyQuestRecipeRegistry.getId(recipe)!!.string)
            }
        })
        return TypedActionResult.consume(itemStack)
    }
}

fun ItemStack.getFairyQuestRecipeId(): Identifier? = nbt.or { return null }.wrapper["FairyQuestRecipe"].string.get().or { return null }.toIdentifier()
fun ItemStack.getFairyQuestRecipe() = this.getFairyQuestRecipeId()?.let { fairyQuestRecipeRegistry.get(it) }

fun ItemStack.setFairyQuestRecipeId(identifier: Identifier) = getOrCreateNbt().wrapper["FairyQuestRecipe"].string.set(identifier.string)
fun ItemStack.setFairyQuestRecipe(recipe: FairyQuestRecipe) = this.setFairyQuestRecipeId(fairyQuestRecipeRegistry.getId(recipe)!!)

private fun createFairyQuestCardModel() = Model {
    ModelData(
        parent = Identifier("item/generated"),
        textures = ModelTexturesData(
            "layer0" to Identifier(MirageFairy2024.modId, "item/fairy_quest_card_background").string,
            "layer1" to Identifier(MirageFairy2024.modId, "item/fairy_quest_card_frame").string,
        ),
    )
}

object FairyQuestCardIngredient : CustomIngredient {
    val ID = Identifier(MirageFairy2024.modId, "fairy_quest_card")
    val SERIALIZER = object : CustomIngredientSerializer<FairyQuestCardIngredient> {
        override fun getIdentifier() = ID
        override fun read(json: JsonObject?) = FairyQuestCardIngredient
        override fun write(json: JsonObject?, ingredient: FairyQuestCardIngredient?) = Unit
        override fun read(buf: PacketByteBuf) = FairyQuestCardIngredient
        override fun write(buf: PacketByteBuf, ingredient: FairyQuestCardIngredient) = Unit
    }

    override fun requiresTesting() = true
    override fun test(stack: ItemStack) = stack.isOf(FairyQuestCardCard.item)

    override fun getMatchingStacks(): List<ItemStack> {
        return fairyQuestRecipeRegistry.sortedEntrySet.map {
            val itemStack = FairyQuestCardCard.item.createItemStack()
            itemStack.setFairyQuestRecipe(it.value)
            itemStack
        }
    }

    override fun getSerializer() = SERIALIZER
}
