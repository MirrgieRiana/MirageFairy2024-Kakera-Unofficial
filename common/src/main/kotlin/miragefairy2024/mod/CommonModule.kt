package miragefairy2024.mod

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.platformProxy
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemGroupCard
import miragefairy2024.util.Registration
import miragefairy2024.util.TextureMap
import miragefairy2024.util.createItemStack
import miragefairy2024.util.register
import miragefairy2024.util.registerModelGeneration
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions

val mirageFairy2024ItemGroupCard = ItemGroupCard(
    MirageFairy2024.identifier("miragefairy2024"), "MF24KU", "MF24KU",
) { MaterialCard.PHANTOM_DROP.item().createItemStack() }

val rootAdvancement = AdvancementCard(
    identifier = MirageFairy2024.identifier("root"),
    context = AdvancementCard.Root(MirageFairy2024.identifier("textures/block/haimeviska_planks.png")),
    icon = { MotifCard.MAGENTA_GLAZED_TERRACOTTA.createFairyItemStack() },
    name = EnJa("MF24KU", "MF24KU"),
    description = EnJa("The Noisy Land of Tertia", "かしましきテルティアの地"),
    criterion = AdvancementCard.hasItem { Items.STICK },
    silent = true,
)

object IconItem {
    val mirageFlowerIconItem = Registration(BuiltInRegistries.ITEM, MirageFairy2024.identifier("icon/mirage_flower")) { Item(Item.Properties()) }
    val phantomFlowerIconItem = Registration(BuiltInRegistries.ITEM, MirageFairy2024.identifier("icon/phantom_flower")) { Item(Item.Properties()) }

    context(ModContext)
    fun init() {
        mirageFlowerIconItem.register()
        mirageFlowerIconItem.registerModelGeneration(ModelTemplates.FLAT_ITEM) { TextureMap(TextureSlot.LAYER0 to MirageFairy2024.identifier("block/magic_plant/mirage_flower_age3")) }
        phantomFlowerIconItem.register()
        phantomFlowerIconItem.registerModelGeneration(ModelTemplates.FLAT_ITEM) { TextureMap(TextureSlot.LAYER0 to MirageFairy2024.identifier("block/magic_plant/phantom_flower_age3")) }
    }
}

context(ModContext)
fun initCommonModule() {
    mirageFairy2024ItemGroupCard.init()

    WaterBottleIngredient.SERIALIZER.register()

    rootAdvancement.init()

    IconItem.init()

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
