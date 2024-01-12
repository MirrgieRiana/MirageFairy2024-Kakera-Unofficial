package miragefairy2024.mod.fairyquest

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
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
    val color: Int
    val icon: ItemStack
    val title: Text
    val message: Text
    val client: Text
    val duration: Int
    val inputs: List<Pair<Ingredient, Int>>
    val outputs: List<ItemStack>
}

enum class FairyQuestRecipeCard(
    path: String,
    override val color: Int,
    enTitle: String,
    jaTitle: String,
    enMessage: String,
    jaMessage: String,
    enClient: String,
    jaClient: String,
    override val inputs: List<Pair<Ingredient, Int>>,
    override val outputs: List<ItemStack>,
    override val duration: Int = 20 * 10,
    override val icon: ItemStack = outputs.first(),
) : FairyQuestRecipe {
    SOLICITING_CERAMIC_BRICKS(
        "soliciting_ceramic_bricks", 0xFFFAF2,
        "Soliciting Ceramic Bricks", "セラミックレンガの募集",
        "In light of the reconstruction project for the vacuum decay furnace, we are actively seeking donations of ceramic bricks. If needed, we will selectively provide reusable materials from the collected building components.",
        "真空崩壊炉の建て替え工事につき、セラミック製レンガの寄贈を広く募集しております。ご入用であれば、リユース可能な建築資材を選別し、ご提供いたします。",
        "The Institute of Fairy Research\nCordelia Branch", "妖精研究所\nコーディリア支部",
        listOf(Items.BRICKS.toIngredient() to 1),
        listOf(BlockMaterialCard.DRYWALL.item.createItemStack(1), Items.STONE_BRICKS.createItemStack(1), Items.WHITE_CONCRETE.createItemStack(1)),
    ),
    IMPROMPTU_FANTASTIC_CARNIVAL(
        "impromptu_fantastic_carnival", 0xFCF5DF,
        "Impromptu Fantastic Carnival", "即席ファンタスティックカーニバル",
        "Help! We're running out of cakes, and the chickens are taking their sweet time laying eggs! Can someone please help out? We don't have time to bake, so substitute it right away!",
        "たいへん！ケーキが足りないのにニワトリがなかなか卵を産まないの！お願い！作ってる時間はないから、今すぐ誰か代わりになって！",
        "Breadia the fairy of bread", "麺麭精ブレアージャ",
        listOf(MaterialCard.FRACTAL_WISP.item.toIngredient() to 1, Items.DIRT.toIngredient() to 1, ItemTags.COALS.toIngredient() to 1),
        listOf(Items.CAKE.createItemStack(1)),
    ),
    NEW_PRODUCT_FROM_FRI(
        "new_product_from_fri", 0xAC5BD8,
        "New product from FRI!", "妖精研究所から新商品登場！",
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
        listOf(MaterialCard.FRACTAL_WISP.item.toIngredient() to 1, Items.DIRT.toIngredient() to 1, ItemTags.COALS.toIngredient() to 1),
        listOf(Items.COOKED_BEEF.createItemStack(1)),
    ),
    VEGETATION_SURVEY(
        "vegetation_survey", 0x6BAF7C,
        "Vegetation Survey", "植生調査",
        "The fairy trees...? We should be over a million light-years away from the Habitabilis Zona. I'm curious to divine the past of this star, so would you consider sending me samples of the vegetation?",
        "妖精の樹…？ここはハビタビリスゾーナから100万光年以上も離れた場所のはず…。この星の過去を占ってみたいから、植生サンプルを送ってくれないかしら？",
        "The Pearl Knights of Miranagi\nShinonome Astrology Academy\nRumeri", "みらなぎ聖騎士団\n東雲占卜院\nるめり",
        listOf(HaimeviskaBlockCard.LOG.item.toIngredient() to 4, HaimeviskaBlockCard.LEAVES.item.toIngredient() to 16),
        listOf(MaterialCard.MIRANAGITE.item.createItemStack(1)),
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
