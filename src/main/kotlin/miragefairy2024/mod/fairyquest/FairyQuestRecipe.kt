package miragefairy2024.mod.fairyquest

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.placeditem.PlacedItemBlockEntity
import miragefairy2024.mod.placeditem.PlacedItemCard
import miragefairy2024.util.Chance
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.registerChestLoot
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.toIngredient
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.toJsonWrapper
import mirrg.kotlin.hydrogen.join
import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.loot.LootTables
import net.minecraft.loot.condition.LootCondition
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.function.ConditionalLootFunction
import net.minecraft.loot.function.LootFunctionType
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.ItemTags
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.world.Heightmap
import net.minecraft.world.gen.GenerationStep
import net.minecraft.world.gen.feature.DefaultFeatureConfig
import net.minecraft.world.gen.feature.Feature
import net.minecraft.world.gen.feature.PlacedFeatures
import net.minecraft.world.gen.feature.util.FeatureContext
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier
import net.minecraft.world.gen.placementmodifier.RarityFilterPlacementModifier
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier

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
    val lootCategory: LootCategory,
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
    EMERGENCY_SITUATION(
        "emergency_situation", 0xFF6900, LootCategory.RARE,
        "Emergency Situation", "非常事態",
        "Help!!!\n\n!!!!\n\n!!!!!\n\nI'm out of toilet paper!!!!!!",
        "助けて！！！\n\n！！！！\n\n！！！！！\n\nトイレットペーパーが無いの！！！！！！",
        "The Institute of Fairy Research\nEthics Department\nTirirknofe Herirmina", "妖精研究所\n倫理部\nティリルクノフェ・ヘリルミーナ",
        listOf(Items.PAPER.toIngredient() to 24),
        listOf(Items.EMERALD.createItemStack(1)),
    ),
    NOTICE_FOR_CERAMIC_BRICK_DONATIONS(
        "notice_for_ceramic_brick_donations", 0xFFFAF2, LootCategory.RARE,
        "Notice for Ceramic Brick Donations", "セラミックレンガ募集のお知らせ",
        "We are actively seeking donations of ceramic bricks for the reconstruction project of the vacuum decay reactor. If you are in need, we will selectively provide reusable building materials.",
        "真空崩壊炉改修工事に向けてセラミックレンガの寄付を広く募集しております。必要な場合は、リユース可能な建築資材を選別してご提供いたします。",
        "The Institute of Fairy Research\nCordelia Branch", "妖精研究所\nコーディリア支部",
        listOf(Items.BRICKS.toIngredient() to 1),
        listOf(BlockMaterialCard.DRYWALL.item.createItemStack(1), Items.STONE_BRICKS.createItemStack(1), Items.WHITE_CONCRETE.createItemStack(1)),
    ),
    IMPROMPTU_FANTASTIC_CARNIVAL(
        "impromptu_fantastic_carnival", 0xFCF5DF, LootCategory.RARE,
        "Impromptu Fantastic Carnival", "即席ファンタスティックカーニバル",
        "Help! We're running out of cakes, and the chickens are taking their sweet time laying eggs! Can someone please help out? We don't have time to bake, so substitute it right away!",
        "たいへん！ケーキが足りないのにニワトリがなかなか卵を産まないの！お願い！作ってる時間はないから、今すぐ誰か代わりになって！",
        "Breadia the fairy of bread", "麺麭精ブレアージャ",
        listOf(MaterialCard.FRACTAL_WISP.item.toIngredient() to 1), // TODO -> ケーキ精
        listOf(Items.CAKE.createItemStack(1)),
    ),
    NEW_PRODUCT_FROM_FRI(
        "new_product_from_fri", 0xAC5BD8, LootCategory.COMMON,
        "New product from FRI!", "妖精研究所から新商品登場！",
        listOf(
            "The Institute of Fairy Research proudly presents its latest product, the “Fairy Quest Card”! It's an innovative communication device that allows communication and material transfer with different and parallel worlds!",
            "Have you ever been caught in a sudden thunderstorm while out and about? With this, you won't fear unexpected thunderstorms anymore!",
            "Simply insert the payment into the “Fairy Quest Card” connected to the stores of parallel worlds, and all you need to do is receive the data for the product. The built-in 3D printer will handle the rest, just like this!",
            "",
            "*For receiving items containing special elements, a separate cartridge for special elements (sold separately) may be required.",
            "*For receiving items with low entropy, a separate anti-entropy cartridge (sold separately) may be required.",
            "*The institute assumes no responsibility for disputes arising from transactions with communication destinations.",
            "*Please be cautious to avoid trapping your fingers in the portal.",
            "*Keep out of reach of children.",
        ).join("\n"),
        listOf(
            "妖精研究所が誇りを持ってお届けする新商品、“フェアリークエストカード”が待望の新登場！異世界や並行世界との通信や物質転送が可能な画期的な通信端末です！",
            "外出先で急に雷雨が降りだしちゃったなんて経験、一度はありませんか？これさえあれば、急な雷なんて怖くない！",
            "平行世界のストアに接続された“フェアリークエストカード”に代金を投入すれば、あとは商品のデータを受け取るだけで、内蔵の3Dプリンターが、ほら、この通り！",
            "",
            "※特殊な元素を含む物品の受け取りには、特殊元素用カートリッジ（別売り）が必要となる場合がございます。",
            "※エントロピーの低い物品の受け取りには、反エントロピー用カートリッジ（別売り）が必要となる場合がございます。",
            "※通信先との取引に伴う紛争については、当研究所は一切の責任を負いません。",
            "※ポータルに指等を挟まないようご注意ください。",
            "※お子様の手の届かない場所に保管してください。",
        ).join("\n"),
        "The Institute of Fairy Research\nCreation Department", "妖精研究所\n創製部",
        listOf(ItemTags.COALS.toIngredient() to 1),
        listOf(Items.WHITE_BED.createItemStack(1)),
    ),
    VEGETATION_SURVEY(
        "vegetation_survey", 0x6BAF7C, LootCategory.RARE,
        "Vegetation Survey", "植生調査",
        "The fairy trees...? We should be over a million light-years away from the Habitabilis Zona. I'm curious to divine the past of this star, so would you consider sending me samples of the vegetation?",
        "妖精の樹…？ここはハビタビリスゾーナから100万光年以上も離れた場所のはず…。この星の過去を占ってみたいから、植生サンプルを送ってくれないかしら？",
        "The Pearl Knights of Miranagi\nShinonome Astrology Academy\nRumeri", "みらなぎ聖騎士団\n東雲占卜院\nるめり",
        listOf(HaimeviskaBlockCard.LOG.item.toIngredient() to 4, HaimeviskaBlockCard.LEAVES.item.toIngredient() to 16),
        listOf(MaterialCard.MIRANAGITE.item.createItemStack(1)),
    ),
    FATAL_ACCIDENT(
        "fatal_accident", 0x000027, LootCategory.RARE,
        "Fatal Accident", "重大な事故",
        """
        I pray that this message reaches some human in a parallel world.
        No, at this point, it could be fairies, malformed creatures, anything.
        A catastrophic accident has occurred, and our world is on the verge of ending.
        The vacuum decay reactor at Cordelia Branch has gone out of control, and an asteroid was swallowed by the vacuum decay.
        It's not just Cordelia.
        Europa, Haumea, and even Headquarters are unresponsive.
        Communication has ceased, and even the Divine Ear is silent.
        Anti-entropy sludge is leaking from the warp routes, and there's no telling when this place will be disintegrated to zero.
        Before being engulfed by the vacuum decay, we plan to escape into space with colonization pods.
        Damn it, the vacuum decay reactor was never something humans should have messed with!
        If someone is reading this, please understand.
        The vacuum decay reactor wasn't a safe, environmentally friendly source of energy.
        One wrong move, and it's a terrifying thing that could wipe out an entire planet.
        If anyone is in control of the vacuum decay reactor, please stop it now!
        Before your world ceases to exist!!!
        """.trimIndent().trim().replace("\n", "\n\n"),
        """
        このメッセージが平行世界の人間に届くことを祈る。
        いや、この際妖精でも奇形生物でも何でもいい。
        重大な事故が起こって俺の世界が終わろうとしている。
        コーディリア支部の真空崩壊炉が制御を失い、小惑星ごと真空崩壊に飲み込まれちまった。
        コーディリアだけじゃない。
        エウロパも、ハウメアも、本部も応答しない。
        もう通信も途絶えたし、天耳通も反応しない。
        アンチエントロピースラッジが縮地路から漏れ出て、ここもいつゼロまで分解されるか分からない。
        真空崩壊に飲み込まれる前に、俺たちは植民ポッドで宇宙に脱出するつもりだ。
        クソッ、真空崩壊炉なんて人間が手を出していい代物じゃなかったんだ！
        もし誰かがこれを見ているなら、理解してくれ。
        真空崩壊炉は安全で環境に優しいエネルギーなんかじゃなかった。
        一歩間違えば惑星ごと消し去ってしまうヤバい奴だ。
        真空崩壊炉を管理している奴がいたら、今すぐそれを止めてくれ！
        お前たちの世界がまだ生きているうちに！！！
        """.trimIndent().trim().replace("\n", "\n\n"),
        "The Institute of Fairy Research\nOphelia Branch\nLibrariania the fairy of librarian", "妖精研究所\nオフィーリア支部\n司書精リブラリアーニャ",
        listOf(
            Items.BEDROCK.toIngredient() to 64,
            Items.BEDROCK.toIngredient() to 64,
            Items.BEDROCK.toIngredient() to 64,
            Items.BEDROCK.toIngredient() to 64,
        ),
        listOf(
            BlockMaterialCard.LOCAL_VACUUM_DECAY.item.createItemStack(64),
            BlockMaterialCard.LOCAL_VACUUM_DECAY.item.createItemStack(64),
            BlockMaterialCard.LOCAL_VACUUM_DECAY.item.createItemStack(64),
            BlockMaterialCard.MIRANAGITE_BLOCK.item.createItemStack(64),
        ),
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)

    val titleTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.title" }, enTitle, jaTitle)
    val messageTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.message" }, enMessage, jaMessage)
    val clientTranslation = Translation({ "miragefairy2024.fairyQuestRecipe.$path.client" }, enClient, jaClient)

    override val title get() = titleTranslation()
    override val message get() = messageTranslation()
    override val client get() = clientTranslation()

    enum class LootCategory {
        NONE,
        COMMON,
        RARE,
    }
}

val SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE = LootFunctionType(SetFairyQuestRecipeLootFunction.SERIALIZER)

val FAIRY_QUEST_CARD_FEATURE = FairyQuestCardFeature(DefaultFeatureConfig.CODEC)

context(ModContext)
fun initFairyQuestRecipe() {
    FairyQuestRecipeCard.entries.forEach { card ->
        card.register(fairyQuestRecipeRegistry, card.identifier)

        card.titleTranslation.enJa()
        card.messageTranslation.enJa()
        card.clientTranslation.enJa()


        // 村チェストドロップ
        run {
            val allVillageChests = listOf(
                LootTables.VILLAGE_WEAPONSMITH_CHEST,
                LootTables.VILLAGE_TOOLSMITH_CHEST,
                LootTables.VILLAGE_ARMORER_CHEST,
                LootTables.VILLAGE_CARTOGRAPHER_CHEST,
                LootTables.VILLAGE_MASON_CHEST,
                LootTables.VILLAGE_SHEPARD_CHEST,
                LootTables.VILLAGE_BUTCHER_CHEST,
                LootTables.VILLAGE_FLETCHER_CHEST,
                LootTables.VILLAGE_FISHER_CHEST,
                LootTables.VILLAGE_TANNERY_CHEST,
                LootTables.VILLAGE_TEMPLE_CHEST,
                LootTables.VILLAGE_DESERT_HOUSE_CHEST,
                LootTables.VILLAGE_PLAINS_CHEST,
                LootTables.VILLAGE_TAIGA_HOUSE_CHEST,
                LootTables.VILLAGE_SNOWY_HOUSE_CHEST,
                LootTables.VILLAGE_SAVANNA_HOUSE_CHEST,
            )

            fun registerChestLoot(lootTableId: Identifier, weight: Int) {
                FairyQuestCardCard.item.registerChestLoot(lootTableId, weight) {
                    apply { SetFairyQuestRecipeLootFunction(card.identifier) }
                }
            }

            when (card.lootCategory) {
                FairyQuestRecipeCard.LootCategory.NONE -> Unit

                FairyQuestRecipeCard.LootCategory.COMMON -> {
                    allVillageChests.forEach {
                        registerChestLoot(it, 10)
                    }
                }

                FairyQuestRecipeCard.LootCategory.RARE -> {
                    allVillageChests.forEach {
                        registerChestLoot(it, 1)
                    }
                }
            }
        }

    }

    ModEvents.onInitialize {

        // 地形生成
        run {
            val configuredFeatureKey = registerDynamicGeneration(RegistryKeys.CONFIGURED_FEATURE, Identifier(MirageFairy2024.modId, "fairy_quest_card")) {
                FAIRY_QUEST_CARD_FEATURE with DefaultFeatureConfig.INSTANCE
            }
            val placedFeatureKey = registerDynamicGeneration(RegistryKeys.PLACED_FEATURE, Identifier(MirageFairy2024.modId, "fairy_quest_card")) {
                val placementModifiers = listOf(
                    RarityFilterPlacementModifier.of(256),
                    SquarePlacementModifier.of(),
                    PlacedFeatures.MOTION_BLOCKING_HEIGHTMAP,
                    BiomePlacementModifier.of(),
                )
                it.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE).getOrThrow(configuredFeatureKey) with placementModifiers
            }
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.VEGETAL_DECORATION, placedFeatureKey)
        }

    }

    SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE.register(Registries.LOOT_FUNCTION_TYPE, Identifier(MirageFairy2024.modId, "set_fairy_quest_recipe"))

    FAIRY_QUEST_CARD_FEATURE.register(Registries.FEATURE, Identifier(MirageFairy2024.modId, "fairy_quest_card"))

}

class FairyQuestCardFeature(codec: Codec<DefaultFeatureConfig>) : Feature<DefaultFeatureConfig>(codec) {
    override fun generate(context: FeatureContext<DefaultFeatureConfig>): Boolean {
        val random = context.random
        val world = context.world

        var count = 0
        val currentBlockPos = BlockPos.Mutable()
        repeat(2) {
            currentBlockPos.set(
                context.origin,
                random.nextInt(3) - random.nextInt(3),
                random.nextInt(3) - random.nextInt(3),
                random.nextInt(3) - random.nextInt(3),
            )

            // 座標決定
            val actualBlockPos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, currentBlockPos)

            // 生成環境判定
            if (!world.getBlockState(actualBlockPos).isReplaceable) return@repeat // 配置先が埋まっている

            // レシピ抽選
            val table = mutableListOf<Chance<Identifier>>()
            FairyQuestRecipeCard.entries.forEach { recipe ->
                when (recipe.lootCategory) {
                    FairyQuestRecipeCard.LootCategory.NONE -> Unit
                    FairyQuestRecipeCard.LootCategory.COMMON -> table += Chance(5.0, recipe.identifier)
                    FairyQuestRecipeCard.LootCategory.RARE -> table += Chance(1.0, recipe.identifier)
                }
            }
            val recipeId = table.weightedRandom(random) ?: return@repeat // 有効なレシピが一つもない

            // 成功

            world.setBlockState(actualBlockPos, PlacedItemCard.block.defaultState, Block.NOTIFY_LISTENERS)
            val itemStack = FairyQuestCardCard.item.createItemStack().also { it.setFairyQuestRecipeId(recipeId) }
            val blockEntity = world.getBlockEntity(actualBlockPos) as? PlacedItemBlockEntity ?: return@repeat // ブロックの配置に失敗した
            blockEntity.itemStack = itemStack
            blockEntity.itemX = 0.25 + 0.5 * random.nextDouble()
            blockEntity.itemZ = 0.25 + 0.5 * random.nextDouble()
            blockEntity.itemRotateY = MathHelper.TAU * random.nextDouble()
            blockEntity.markDirty()

            count++
        }
        return count > 0
    }
}

class SetFairyQuestRecipeLootFunction(private val recipeId: Identifier, conditions: List<LootCondition> = listOf()) : ConditionalLootFunction(conditions.toTypedArray()) {
    companion object {
        val SERIALIZER = object : Serializer<SetFairyQuestRecipeLootFunction>() {
            override fun toJson(jsonObject: JsonObject, conditionalLootFunction: SetFairyQuestRecipeLootFunction, jsonSerializationContext: JsonSerializationContext) {
                super.toJson(jsonObject, conditionalLootFunction, jsonSerializationContext)
                jsonObject.add("id", conditionalLootFunction.recipeId.string.jsonElement)
            }

            override fun fromJson(json: JsonObject, context: JsonDeserializationContext, conditions: Array<LootCondition>): SetFairyQuestRecipeLootFunction {
                val id = json.toJsonWrapper()["id"].asString().toIdentifier()
                return SetFairyQuestRecipeLootFunction(id, conditions.toList())
            }
        }
    }

    override fun getType() = SET_FAIRY_QUEST_RECIPE_LOOT_FUNCTION_TYPE

    override fun process(stack: ItemStack, context: LootContext): ItemStack {
        stack.setFairyQuestRecipeId(recipeId)
        return stack
    }
}
