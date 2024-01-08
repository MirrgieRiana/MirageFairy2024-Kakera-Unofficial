package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.MaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.toIngredient
import mirrg.kotlin.hydrogen.join
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.Identifier

val fairyQuestRecipeRegistryKey: RegistryKey<Registry<FairyQuestRecipe>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "fairy_quest_recipe"))
val fairyQuestRecipeRegistry: Registry<FairyQuestRecipe> = FabricRegistryBuilder.createSimple(fairyQuestRecipeRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface FairyQuestRecipe {
    val title: Text
    val message: Text
    val client: Text
    val duration: Int
    val inputs: List<Pair<Ingredient, Int>>
    val outputs: List<ItemStack>
}

enum class FairyQuestRecipeCard(
    path: String,
    enTitle: String,
    jaTitle: String,
    enMessage: String,
    jaMessage: String,
    enClient: String,
    jaClient: String,
    override val duration: Int,
    override val inputs: List<Pair<Ingredient, Int>>,
    override val outputs: List<ItemStack>,
) : FairyQuestRecipe {
    IMPROMPTU_FANTASTIC_CARNIVAL(
        "impromptu_fantastic_carnival", "Impromptu Fantastic Carnival", "即席ファンタスティックカーニバル",
        "Help! We're running out of cakes, and the chickens are taking their sweet time laying eggs! Can someone please help out? We don't have time to bake, so substitute it right away!",
        "たいへん！ケーキが足りないのにニワトリがなかなか卵を産まないの！お願い！作ってる時間はないから、今すぐ誰か代わりになって！",
        "Breadia the fairy of bread", "麺麭精ブレアージャ",
        20 * 10,
        listOf(MaterialCard.FRACTAL_WISP.item.toIngredient() to 1, Items.DIRT.toIngredient() to 1, ItemTags.COALS.toIngredient() to 1),
        listOf(Items.CAKE.createItemStack(1)),
    ),
    NEW_PRODUCT_FROM_FRI(
        "new_product_from_fri", "New product from FRI!", "妖精研究所から新商品登場！",
        listOf(
            "Introducing the eagerly awaited new product from The Institute of Fairy Research, the Fairy Quest Card! This groundbreaking communication device allows communication and material transfer with otherworldly and parallel realms!",
            "No more fear of rainy days with this innovative gadget! Simply attach the dedicated cartridge, receive the data of your desired item, and let the built-in 3D printer work its magic – just like that!",
            "",
            "*Note: Ink requires a dedicated cartridge (sold separately).",
            "*The Institute of Fairy Research assumes no responsibility for any disputes arising from transactions with communication destinations.",
            "*Keep out of reach of children.",
        ).join("\n"),
        listOf(
            "妖精研究所が誇りを持って贈る新商品、フェアリークエストカードが待望の新登場！異世界や並行世界との通信や物質転送が可能な画期的な通信端末です！",
            "これさえあれば、もう雨の日なんて怖くない！専用のカートリッジを装着し、品物のデータを受け取るだけ！あとは内蔵の3Dプリンターが、ほら、この通り！",
            "",
            "※インクは専用のカートリッジ（別売り）を装着する必要があります。",
            "※通信先との取引に伴う紛争について、当研究所は一切の責任を負いません。",
            "※お子様の手の届かない場所に保管してください。",
        ).join("\n"),
        "The Institute of Fairy Research,\nCreation Department", "妖精研究所\n創製部",
        20 * 10,
        listOf(MaterialCard.FRACTAL_WISP.item.toIngredient() to 1, Items.DIRT.toIngredient() to 1, ItemTags.COALS.toIngredient() to 1),
        listOf(Items.COOKED_BEEF.createItemStack(1)),
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)

    val titleTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.title" }, enTitle, jaTitle)
    val messageTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.message" }, enMessage, jaMessage)
    val clientTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.client" }, enClient, jaClient)

    override val title get() = titleTranslation()
    override val message get() = messageTranslation()
    override val client get() = clientTranslation()
}

fun initFairyQuestRecipe() {
    FairyQuestRecipeCard.entries.forEach { card ->
        card.register(fairyQuestRecipeRegistry, card.identifier)
        card.titleTranslation.enJa()
        card.messageTranslation.enJa()
        card.clientTranslation.enJa()
    }
}
