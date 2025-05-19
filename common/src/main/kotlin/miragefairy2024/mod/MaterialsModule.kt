package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.RandomFairySummoningItem
import miragefairy2024.mod.fairy.SOUL_STREAM_CONTAINABLE_TAG
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.machine.AuraReflectorFurnaceRecipeCard
import miragefairy2024.mod.machine.FermentationBarrelRecipeCard
import miragefairy2024.mod.machine.registerSimpleMachineRecipeGeneration
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.modId
import miragefairy2024.util.obtain
import miragefairy2024.util.on
import miragefairy2024.util.plus
import miragefairy2024.util.pull
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerBlastingRecipeGeneration
import miragefairy2024.util.registerChestLoot
import miragefairy2024.util.registerComposterInput
import miragefairy2024.util.registerCompressionRecipeGeneration
import miragefairy2024.util.registerExtraOreDrop
import miragefairy2024.util.registerFuel
import miragefairy2024.util.registerGeneratedModelGeneration
import miragefairy2024.util.registerGrassDrop
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerItemTagGeneration
import miragefairy2024.util.registerMobDrop
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerShapelessRecipeGeneration
import miragefairy2024.util.registerSmeltingRecipeGeneration
import miragefairy2024.util.registerSpecialRecipe
import miragefairy2024.util.text
import miragefairy2024.util.toRomanText
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.stats.Stats
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.gameevent.GameEvent
import kotlin.math.pow
import net.minecraft.advancements.CriteriaTriggers as Criteria
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.util.StringUtil as StringHelper
import net.minecraft.world.InteractionHand as Hand
import net.minecraft.world.InteractionResultHolder as TypedActionResult
import net.minecraft.world.effect.MobEffectInstance as StatusEffectInstance
import net.minecraft.world.effect.MobEffects as StatusEffects
import net.minecraft.world.entity.player.Player as PlayerEntity
import net.minecraft.world.food.FoodProperties as FoodComponent
import net.minecraft.world.item.ItemUtils as ItemUsage
import net.minecraft.world.item.UseAnim as UseAction
import net.minecraft.world.level.storage.loot.BuiltInLootTables as LootTables

class MaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val fuelValue: Int? = null,
    val soulStreamContainable: Boolean = false,
    val foodComponentCreator: (suspend () -> FoodComponent)? = null,
    val recipeRemainder: Item? = null,
    val creator: (Item.Properties) -> Item = ::Item,
    val advancementCreator: (MaterialCard.(ResourceLocation) -> AdvancementCard)? = null,
    val initializer: context(ModContext) MaterialCard.() -> Unit = {},
) {
    companion object {
        val entries = mutableListOf<MaterialCard>()
        private operator fun MaterialCard.not() = apply { entries += this }

        val XARPITE = !MaterialCard(
            "xarpite", "Xarpite", "紅天石",
            PoemList(2).poem("Binds astral flux with magnetic force", "黒鉄の鎖は繋がれる。血腥い魂の檻へ。"),
            fuelValue = 200 * 16,
            // TODO 使えるワード：牢獄
        ) {
            item.registerGrassDrop(0.03F, 1) // TODO 古代の遺構
            item.registerMobDrop(EntityType.WITCH, onlyKilledByPlayer = true, dropRate = Pair(0.2F, 0.1F))
        }
        val MIRANAGITE = !MaterialCard(
            "miranagite", "Miranagite", "蒼天石",
            PoemList(2).poem("Astral body crystallized by anti-entropy", "秩序の叛乱、天地創造の逆光。"),
            // TODO The origin of the universe 無限の深淵、破壊と再生の輪廻。
        )
        val MIRANAGITE_ROD = !MaterialCard(
            "miranagite_rod", "Miranagite Rod", "蒼天石の棒",
            PoemList(2).poem("Mana flows well through the core", "蒼天に従える光条は、魔力の祝福を示す。"),
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("  #")
                pattern(" # ")
                pattern("#  ")
                define('#', MIRANAGITE.item())
            } on MIRANAGITE.item from MIRANAGITE.item
        }
        val CHAOS_STONE = !MaterialCard(
            "chaos_stone", "Chaos Stone", "混沌の石",
            PoemList(4).poem("Chemical promoting catalyst", "魔力の暴走、加速する無秩序の流れ。"),
        ) {
            item.registerChestLoot({ LootTables.SIMPLE_DUNGEON }, 10, 3..5)
            item.registerChestLoot({ LootTables.ABANDONED_MINESHAFT }, 5)
            item.registerChestLoot({ LootTables.ANCIENT_CITY }, 10, 1..5)
            item.registerChestLoot({ LootTables.DESERT_PYRAMID }, 10)
            item.registerChestLoot({ LootTables.VILLAGE_DESERT_HOUSE }, 3)
            item.registerChestLoot({ LootTables.DESERT_PYRAMID_ARCHAEOLOGY }, 1)
            item.registerChestLoot({ LootTables.DESERT_WELL_ARCHAEOLOGY }, 1)
        }

        val MIRAGE_LEAVES = !MaterialCard(
            "mirage_leaves", "Mirage Leaves", "ミラージュの葉",
            PoemList(1).poem("Don't cut your fingers!", "刻まれる、記憶の破片。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
        }
        val MIRAGE_STEM = !MaterialCard(
            "mirage_stem", "Mirage Stem", "ミラージュの茎",
            PoemList(1).poem("Cell wall composed of amorphous ether", "植物が手掛ける、分子レベルの硝子細工。"),
            fuelValue = 100,
        ) {
            registerShapelessRecipeGeneration(item) {
                requires(MIRAGE_LEAVES.item())
            } on MIRAGE_LEAVES.item
            item.registerComposterInput(0.5F)
            registerShapedRecipeGeneration({ Items.STICK }, 2) {
                pattern("#")
                pattern("#")
                define('#', item())
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_GLASS_FIBER = !MaterialCard(
            "fairy_glass_fiber", "Fairy Glass Fiber", "きらめきの糸",
            PoemList(1).poem("Fiber-optic nervous system", "意識の一部だったもの。"),
            soulStreamContainable = true,
        ) {
            registerShapedRecipeGeneration(item) {
                pattern("###")
                pattern("# #")
                pattern("###")
                define('#', MIRAGE_STEM.item())
            } on MIRAGE_STEM.item
            registerShapedRecipeGeneration({ Items.STRING }) {
                pattern("##")
                pattern("##")
                define('#', item())
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_CRYSTAL = !MaterialCard(
            "fairy_crystal", "Fairy Crystal", "フェアリークリスタル",
            PoemList(2).poem("Crystallized soul", "生物を生物たらしめるもの"),
            soulStreamContainable = true,
            advancementCreator = {
                AdvancementCard(
                    identifier = it,
                    icon = item(),
                    name = EnJa("TODO", "水晶の飴"),
                    description = EnJa("TODO", "妖花ミラージュを栽培し希少品を収穫する"),
                )
            },
        )
        val PHANTOM_LEAVES = !MaterialCard(
            "phantom_leaves", "Phantom Leaves", "ファントムの葉",
            PoemList(3).poem("The eroding reality", "析出する空想。"),
            fuelValue = 100,
        )
        val PHANTOM_DROP = !MaterialCard(
            "phantom_drop", "Phantom Drop", "幻想の雫",
            PoemList(4).poem("Beyond the end of the world", "祈りを形に、再生の蜜。"),
            soulStreamContainable = true,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(2)
                    .saturationModifier(0.3F)
                    .effect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60), 1.0F)
                    .alwaysEdible()
                    .build()
            },
        )
        val MIRAGIUM_NUGGET = !MaterialCard(
            "miragium_nugget", "Miragium Nugget", "ミラジウムナゲット",
            PoemList(3).poem("Dismembered metallic body", "小分けにされた妖精のインゴット。"),
            soulStreamContainable = true,
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(MIRAGE_FLOUR.item()) }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60,
            ) on MIRAGE_FLOUR.item
        }
        val MIRAGIUM_INGOT = !MaterialCard(
            "miragium_ingot", "Miragium Ingot", "ミラジウムインゴット",
            PoemList(3).poem("Metallic body", "妖精インゴット。"),
            soulStreamContainable = true,
        )
        val VEROPEDA_LEAF = !MaterialCard(
            "veropeda_leaf", "Veropeda Leaf", "ヴェロペダの葉",
            PoemList(1).poem("Said to house the soul of a demon", "その身融かされるまでの快楽。"),
            fuelValue = 100,
        ) {
            item.registerComposterInput(0.5F)
            registerSmeltingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
            registerBlastingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.1) on item modId MirageFairy2024.MOD_ID from item
        }
        val LILAGIUM_INGOT = !MaterialCard(
            "lilagium_ingot", "Lilagium Ingot", "リラジウムインゴット",
            PoemList(3).poem("Ethereal plant-attractant polysaccharide", "セルロースの精霊よ、エーテルの道を開け。"),
            soulStreamContainable = true,
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(MIRAGIUM_INGOT.item()) }, 1),
                    Pair({ Ingredient.of(Items.LILAC) }, 4),
                    Pair({ Ingredient.of(Items.PEONY) }, 4),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60,
            ) on { Items.LILAC }
        } // TODO "Botanical alloy", "牡丹合金。"
        val MIRAGIDIAN_SHARD = !MaterialCard(
            "miragidian_shard", "Miragidian Shard", "ミラジディアンの欠片",
            PoemList(4).poem("The great collapse 30,000 years ago", "遥か三万年前のミラジウムが見た夢。"),
            soulStreamContainable = true,
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(item()) }, 9),
                ),
                output = { MIRAGIDIAN.item().createItemStack() },
                duration = 20 * 60,
            ) on item from item
        }
        val MIRAGIDIAN = !MaterialCard(
            "miragidian", "Miragidian", "ミラジディアン",
            PoemList(4).poem("A fantasy world told by tungsten", "タングステンが語る幻想世界。"),
            soulStreamContainable = true,
        )
        val ETHEROBALLISTIC_BOLT_FRAGMENT = !MaterialCard(
            "etheroballistic_bolt_fragment", "Etheroballistic Bolt Fragment", "エテロバリスティック弾の破片",
            PoemList(4).poem("More abrasion resistant than lethal", "合金として生きるということ。"),
            soulStreamContainable = true,
        ) {
            registerSmeltingRecipeGeneration(item, MIRAGIDIAN_SHARD.item) on item from item
        }
        val VEROPEDA_BERRIES = !MaterialCard(
            "veropeda_berries", "Veropeda Berries", "ヴェロペダの実",
            PoemList(1)
                .poem("Has analgesic and stimulant effects", "悪魔の囁きを喰らう。")
                .description("Healing and rare nausea by eating", "食べると回復、まれに吐き気"),
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .fast()
                    .effect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 3), 1.0F)
                    .effect(StatusEffectInstance(StatusEffects.CONFUSION, 20 * 20), 0.01F)
                    .build()
            },
        ) {
            item.registerComposterInput(0.3F)
        }
        val LUMINITE = !MaterialCard(
            "luminite", "Luminite", "ルミナイト",
            PoemList(4).poem("An end point of reincarnation", "彷徨える魂の行方。"),
        )
        val RESONITE_INGOT = !MaterialCard(
            "resonite_ingot", "Resonite Ingot", "共鳴石インゴット",
            PoemList(5).poem("Synchronized sound and light", "同調する魂の波動。"),
            soulStreamContainable = true,
        ) {
            registerSimpleMachineRecipeGeneration(
                AuraReflectorFurnaceRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(MIRAGIUM_INGOT.item()) }, 1),
                    Pair({ Ingredient.of(Items.ECHO_SHARD) }, 1),
                    Pair({ Ingredient.of(LUMINITE.item()) }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60,
            ) on LUMINITE.item
        }
        val HAIMEVISKA_SAP = !MaterialCard(
            "haimeviska_sap", "Haimeviska Sap", "ハイメヴィスカの樹液",
            PoemList(1)
                .poem("Smooth and mellow on the palate", "口福のアナムネシス。")
                .description("Gain experience by eating", "食べると経験値を獲得"),
            fuelValue = 200,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(experienceStatusEffect.awaitHolder(), 20), 1.0F)
                    .build()
            },
        ) {
            // →松明
            registerShapedRecipeGeneration({ Items.TORCH }) {
                pattern("#")
                pattern("S")
                define('#', item())
                define('S', Items.STICK)
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val HAIMEVISKA_ROSIN = !MaterialCard(
            "haimeviska_rosin", "Haimeviska Rosin", "妖精の木の涙",
            PoemList(2).poem("High-friction material", "琥珀の月が昇るとき、妖精の木は静かに泣く"),
            fuelValue = 200,
        ) {
            // →粘着ピストン
            registerShapedRecipeGeneration({ Blocks.STICKY_PISTON.asItem() }) {
                pattern("S")
                pattern("P")
                define('P', Blocks.PISTON)
                define('S', item())
            } on item modId MirageFairy2024.MOD_ID from item
        }
        val FAIRY_PLASTIC = !MaterialCard(
            // TODO add recipe
            // TODO add purpose
            "fairy_plastic", "Fairy Plastic", "妖精のプラスチック",
            PoemList(4).poem("Thermoplastic organic polymer", "凍てつく記憶の宿る石。"),
            fuelValue = 200 * 8,
        )
        val FAIRY_RUBBER = !MaterialCard(
            // TODO add purpose
            "fairy_rubber", "Fairy Rubber", "夜のかけら",
            PoemList(3).poem("Minimize the risk of losing belongings", "空は怯える夜精に一握りの温かい闇を与えた"),
        )

        val TINY_MIRAGE_FLOUR = !MaterialCard(
            "tiny_mirage_flour", "Tiny Pile of Mirage Flour", "小さなミラージュの花粉",
            PoemList(1).poem("Compose the body of Mirage fairy", "ささやかな温もりを、てのひらの上に。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(-1.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR = !MaterialCard(
            "mirage_flour", "Mirage Flour", "ミラージュの花粉",
            PoemList(1).poem("Containing metallic organic matter", "叡智の根源、創発のファンタジア。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(0.0), it) },
            advancementCreator = {
                AdvancementCard(it) {
                    Advancement.Builder.advancement()
                        .display(
                            item,
                            text { "植物の支配する世界"() },
                            text { "妖花ミラージュは右クリックで収穫できる"() },
                            MirageFairy2024.identifier("textures/block/aura_stone.png"),
                            AdvancementType.TASK,
                            false,
                            false,
                            false
                        )
                        .addCriterion("has_mirage_flour", InventoryChangeTrigger.TriggerInstance.hasItems(item))
                }
            }
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_NATURE = !MaterialCard(
            "mirage_flour_of_nature", "Mirage Flour of Nature", "自然のミラージュの花粉",
            PoemList(1).poem("Use the difference in ether resistance", "艶やかなほたる色に煌めく鱗粉。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(1.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_EARTH = !MaterialCard(
            "mirage_flour_of_earth", "Mirage Flour of Earth", "大地のミラージュの花粉",
            PoemList(2).poem("As intelligent as humans", "黄金の魂が示す、好奇心の輝き。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(2.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_UNDERWORLD = !MaterialCard(
            "mirage_flour_of_underworld", "Mirage Flour of Underworld", "地底のミラージュの花粉",
            PoemList(2).poem("Awaken fairies in the world and below", "1,300ケルビンの夜景。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(3.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_SKY = !MaterialCard(
            "mirage_flour_of_sky", "Mirage Flour of Sky", "天空のミラージュの花粉",
            PoemList(3).poem("Explore atmosphere and nearby universe", "蒼淵を彷徨う影、導きの光。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(4.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_UNIVERSE = !MaterialCard(
            "mirage_flour_of_universe", "Mirage Flour of Universe", "宇宙のミラージュの花粉",
            PoemList(3)
                .poem("poem1", "Leap spaces by collapsing time crystals,", "運命の束、時の結晶、光速の呪いを退けよ、")
                .poem("poem2", "capture ether beyond observable universe", "讃えよ、アーカーシャに眠る自由の頂きを。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(5.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }
        val MIRAGE_FLOUR_OF_TIME = !MaterialCard(
            "mirage_flour_of_time", "Mirage Flour of Time", "時空のミラージュの花粉",
            PoemList(4)
                .poem("poem1", "Attracts nearby parallel worlds outside", "虚空に眠る時の断片。因果の光が貫くとき、")
                .poem("poem2", "this universe and collects their ether.", "亡失の世界は探し始める。無慈悲な真実を。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(6.0), it) },
        ) {
            item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
        }

        val FAIRY_SCALES = !MaterialCard(
            "fairy_scales", "Fairy Scales", "妖精の鱗粉",
            PoemList(1)
                .poem("A catalyst that converts mana into erg", "湧き上がる、エルグの誘い。"),
            soulStreamContainable = true,
            // TODO レシピ 妖精の森バイオームの雑草
            // TODO 妖精からクラフト
            // TODO 用途
        ) {
            item.registerGrassDrop(0.1F, 1)
        }
        val FRACTAL_WISP = !MaterialCard(
            "fractal_wisp", "Fractal Wisp", "フラクタルウィスプ",
            PoemList(1)
                .poem("poem1", "The fairy of the fairy of the fairy", "妖精の妖精の妖精の妖精の妖精の妖精の妖精")
                .poem("poem2", "of the fairy of the fairy of the f", "の妖精の妖精の妖精の妖精の妖精の妖精の妖"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) }
            // TODO 用途
        )

        val FAIRY_QUEST_CARD_BASE = !MaterialCard(
            "fairy_quest_card_base", "Fairy Quest Card Base", "フェアリークエストカードベース",
            PoemList(1).poem("Am I hopeful in the parallel world?", "存在したかもしれない僕たちのかたち。")
        )

        val MAGNETITE = !MaterialCard(
            "magnetite", "Magnetite", "磁鉄鉱",
            null,
        ) {
            registerSmeltingRecipeGeneration(item, { Items.IRON_NUGGET }, 0.7) on item modId MirageFairy2024.MOD_ID from item
        }

        val FLUORITE = !MaterialCard(
            "fluorite", "Fluorite", "蛍石",
            null,
        )
        val SPHERE_BASE = !MaterialCard(
            "sphere_base", "Sphere Base", "スフィアベース",
            PoemList(2)
                .poem("A mirror that reflects sadistic desires", "前世が見える。              （らしい）"),
            // TODO 用途
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" S ")
                pattern("SFS")
                pattern(" S ")
                define('F', FLUORITE.item())
                define('S', FAIRY_SCALES.item())
            } on FLUORITE.item from FLUORITE.item
        }

        val TINY_BISMUTH_DUST = !MaterialCard(
            "tiny_bismuth_dust", "Tiny Pile of Bismuth Dust", "小さなビスマスの粉",
            null,
        ) {
            item.registerExtraOreDrop(Blocks.COPPER_ORE, fortuneMultiplier = 1)
            item.registerExtraOreDrop(Blocks.DEEPSLATE_COPPER_ORE, fortuneMultiplier = 1)
        }
        val BISMUTH_DUST = !MaterialCard(
            "bismuth_dust", "Bismuth Dust", "ビスマスの粉",
            null,
        )
        val BISMUTH_INGOT = !MaterialCard(
            "bismuth_ingot", "Bismuth Ingot", "ビスマスインゴット",
            null,
        ) {
            registerSmeltingRecipeGeneration(BISMUTH_DUST.item, item) on BISMUTH_DUST.item from BISMUTH_DUST.item
        }

        val MINA_1 = !MaterialCard(
            "mina_1", "1 Mina", "1ミナ",
            PoemList(0)
                .poem("Put this money to work until I come back", "私が帰って来るまでこれで商売をしなさい")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(1, it.fireResistant()) },
        )
        val MINA_5 = !MaterialCard(
            "mina_5", "5 Mina", "5ミナ",
            PoemList(0)
                .poem("Fairy snack", "ご縁があるよ")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(5, it.fireResistant()) },
        )
        val MINA_10 = !MaterialCard(
            "mina_10", "10 Mina", "10ミナ",
            PoemList(0)
                .poem("Can purchase the souls of ten fairies.", "10の妖精が宿る石。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(10, it.fireResistant()) },
        )
        val MINA_50 = !MaterialCard(
            "mina_50", "50 Mina", "50ミナ",
            PoemList(0)
                .poem("The Society failed to replicate this.", "形而上学的有機結晶")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(50, it.fireResistant()) },
        )
        val MINA_100 = !MaterialCard(
            "mina_100", "100 Mina", "100ミナ",
            PoemList(0)
                .poem("Place where fairies and humans intersect", "妖精と人間が交差する場所。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(100, it.fireResistant()) },
        )
        val MINA_500 = !MaterialCard(
            "mina_500", "500 Mina", "500ミナ",
            PoemList(0)
                .poem("A brilliance with a hardness of 7.5", "硬度7.5の輝き。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(500, it.fireResistant()) },
        )
        val MINA_1000 = !MaterialCard(
            "mina_1000", "1000 Mina", "1000ミナ",
            PoemList(0)
                .poem("Created by the fairies of commerce.", "妖精の業が磨き上げる。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(1000, it.fireResistant()) },
        )
        val MINA_5000 = !MaterialCard(
            "mina_5000", "5000 Mina", "5000ミナ",
            PoemList(0)
                .poem("The price of a soul.", "魂の値段。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(5000, it.fireResistant()) },
        )
        val MINA_10000 = !MaterialCard(
            "mina_10000", "10000 Mina", "10000ミナ",
            PoemList(0)
                .poem("Become an eternal gemstone.", "妖花の蜜よ、永遠の宝石となれ。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(10000, it.fireResistant()) },
        )

        val JEWEL_1 = !MaterialCard(
            "jewel_1", "1 Fairy Jewel", "1フェアリージュエル",
            PoemList(0)
                .poem("Long ago, fairies were the nectar.", "その昔、妖精は木の蜜だった。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_5 = !MaterialCard(
            "jewel_5", "5 Fairy Jewel", "5フェアリージュエル",
            PoemList(0)
                .poem("The nectar bloomed from the ground.", "木の蜜は地に触れ、花を咲かせた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_10 = !MaterialCard(
            "jewel_10", "10 Fairy Jewel", "10フェアリージュエル",
            PoemList(0)
                .poem("The wind, sky, and sun laughed.", "風と空と太陽が笑った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_50 = !MaterialCard(
            "jewel_50", "50 Fairy Jewel", "50フェアリージュエル",
            PoemList(0)
                .poem("Fairies simply drifted along.", "妖精はただ漂っていた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_100 = !MaterialCard(
            "jewel_100", "100 Fairy Jewel", "100フェアリージュエル",
            PoemList(0)
                .poem("One day, humans touched fairies.", "その日、人が現れ、妖精に触れた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_500 = !MaterialCard(
            "jewel_500", "500 Fairy Jewel", "500フェアリージュエル",
            PoemList(0)
                .poem("Fairies took form and learned emotion.", "妖精は妖精の姿へとなり、感情を知った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_1000 = !MaterialCard(
            "jewel_1000", "1000 Fairy Jewel", "1000フェアリージュエル",
            PoemList(0)
                .poem("Fairies learned joy and pain.", "妖精は悦びと痛みを知った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_5000 = !MaterialCard(
            "jewel_5000", "5000 Fairy Jewel", "5000フェアリージュエル",
            PoemList(0)
                .poem("Humans saw the fairies and felt relief.", "人は妖精を見て、安堵した。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )
        val JEWEL_10000 = !MaterialCard(
            "jewel_10000", "10000 Fairy Jewel", "10000フェアリージュエル",
            PoemList(0)
                .poem("Thus, humans lost their form.", "こうして、人は人の姿を失った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireResistant()) },
        )

        val APOSTLE_WAND = !MaterialCard(
            "apostle_wand", "Apostle's Wand", "使徒のステッキ",
            PoemList(2).poem("The key to the fairy world", "妖精界への鍵。"),
            creator = { ApostleWandItem(it.stacksTo(1)) },
        ) {
            registerShapedRecipeGeneration(item) {
                pattern(" G")
                pattern("S ")
                define('S', MIRAGE_STEM.item())
                define('G', Items.GOLD_INGOT)
            } on MIRAGE_STEM.item
        }

        val RUM = !MaterialCard(
            "rum", "Rum", "ラム酒",
            null,
            fuelValue = 200 * 4, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(StatusEffects.DAMAGE_BOOST, 20 * 60, 1), 1.0F)
                    .effect(StatusEffectInstance(StatusEffects.CONFUSION, 20 * 60), 0.1F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            // TODO 蒸留装置
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(Items.SUGAR_CANE) }, 16),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60 * 5,
            ) on { Items.SUGAR_CANE }
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + Items.SUGAR_CANE
            }
        }
        val CIDRE = !MaterialCard(
            "cidre", "Cidre", "シードル",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(StatusEffects.DAMAGE_RESISTANCE, 20 * 60), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(Items.APPLE) }, 4),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60 * 1,
            ) on { Items.APPLE }
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + Items.APPLE
            }
        }
        val FAIRY_LIQUEUR = !MaterialCard(
            "fairy_liqueur", "Fairy Liqueur", "妖精のリキュール",
            PoemList(2).poem("Fairies get high, humans get burned", "妖精はハイになり、人間は火傷する。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(experienceStatusEffect.awaitHolder(), 20 * 8, 1), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it, flaming = 5) },
        ) {
            // TODO 醸造樽で作れるのは原酒で、リキュールはマンドレイクを使ってクラフト
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(HAIMEVISKA_SAP.item()) }, 8),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60 * 5,
            ) on HAIMEVISKA_SAP.item
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + HAIMEVISKA_SAP.item()
            }
        }
        val VEROPEDELIQUORA = !MaterialCard(
            "veropedeliquora", "Veropedeliquora", "ヴェロペデリコラ",
            PoemList(2).poem("A dark flavour from the underworld.", "冥界へといざなう、暗黒の味。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(6)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60), 1.0F)
                    .effect(StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 60), 0.1F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(VEROPEDA_BERRIES.item()) }, 8),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 60 * 5,
            ) on VEROPEDA_BERRIES.item
            ModEvents.onInitialize {
                FoodIngredientsRegistry.registry[item()] = FoodIngredients() + FoodIngredientCategoryCard.ALCOHOL + VEROPEDA_BERRIES.item()
            }
        }
        val POISON = !MaterialCard(
            "poison", "Poison", "毒薬",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponentCreator = {
                FoodComponent.Builder()
                    .nutrition(1)
                    .saturationModifier(0.1F)
                    .effect(StatusEffectInstance(StatusEffects.HARM, 1, 9), 1.0F)
                    .effect(StatusEffectInstance(StatusEffects.WITHER, 20 * 60, 4), 1.0F)
                    .build()
            },
            creator = { DrinkItem(it) },
        ) {
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(Items.PUFFERFISH) }, 1),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 5,
            ) on { Items.PUFFERFISH } from { Items.PUFFERFISH }
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(Items.POISONOUS_POTATO) }, 4),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 5,
            ) on { Items.POISONOUS_POTATO } from { Items.POISONOUS_POTATO }
            registerSimpleMachineRecipeGeneration(
                FermentationBarrelRecipeCard,
                inputs = listOf(
                    Pair({ Ingredient.of(Items.GLASS_BOTTLE) }, 1),
                    Pair({ Ingredient.of(Items.SPIDER_EYE) }, 4),
                    Pair({ WaterBottleIngredient.toVanilla() }, 1),
                ),
                output = { item().createItemStack() },
                duration = 20 * 5,
            ) on { Items.SPIDER_EYE } from { Items.SPIDER_EYE }
        }
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = Registration(BuiltInRegistries.ITEM, identifier) {
        Item.Properties()
            .let { foodComponentCreator?.let { c -> it.food(c()) } ?: it }
            .let { if (recipeRemainder != null) it.craftRemainder(recipeRemainder) else it }
            .let { creator(it) }
    }
    val advancementCard = advancementCreator?.invoke(identifier)
}

val MIRAGE_FLOUR_TAG: TagKey<Item> = TagKey.create(Registries.ITEM, MirageFairy2024.identifier("mirage_flour"))

val APPEARANCE_RATE_BONUS_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mirage_flour").toLanguageKey()}.appearance_rate_bonus" }, "Appearance Rate Bonus", "出現率ボーナス")
val MINA_DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mina").toLanguageKey()}.description" }, "Can exchange for Minia with apostle's wand", "使徒のステッキでミーニャと両替可能")

context(ModContext)
fun initMaterialsModule() {
    MaterialCard.entries.forEach { card ->
        card.item.register()
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }
        if (card.fuelValue != null) card.item.registerFuel(card.fuelValue)
        if (card.soulStreamContainable) card.item.registerItemTagGeneration { SOUL_STREAM_CONTAINABLE_TAG }
        if (card.advancementCard != null) card.advancementCard.init()
        card.initializer(this@ModContext, card)
    }

    APPEARANCE_RATE_BONUS_TRANSLATION.enJa()
    MINA_DESCRIPTION_TRANSLATION.enJa()
    DrinkItem.FLAMING_TRANSLATION.enJa()

    // ミラジウム圧縮
    registerCompressionRecipeGeneration(MaterialCard.MIRAGIUM_NUGGET.item, MaterialCard.MIRAGIUM_INGOT.item)

    // ミラージュの花粉圧縮
    registerCompressionRecipeGeneration(MaterialCard.TINY_MIRAGE_FLOUR.item, MaterialCard.MIRAGE_FLOUR.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_FLOUR_OF_NATURE.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item, MaterialCard.MIRAGE_FLOUR_OF_EARTH.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_EARTH.item, MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item, MaterialCard.MIRAGE_FLOUR_OF_SKY.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_SKY.item, MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item, MaterialCard.MIRAGE_FLOUR_OF_TIME.item, noGroup = true)

    // ビスマスの粉圧縮
    registerCompressionRecipeGeneration(MaterialCard.TINY_BISMUTH_DUST.item, MaterialCard.BISMUTH_DUST.item)

    // ミナ両替
    registerCompressionRecipeGeneration(MaterialCard.MINA_1.item, MaterialCard.MINA_5.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_5.item, MaterialCard.MINA_10.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_10.item, MaterialCard.MINA_50.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_50.item, MaterialCard.MINA_100.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_100.item, MaterialCard.MINA_500.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_500.item, MaterialCard.MINA_1000.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.MINA_1000.item, MaterialCard.MINA_5000.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.MINA_5000.item, MaterialCard.MINA_10000.item, 2)

    // ミーニャ⇔ミナ両替
    registerSpecialRecipe("minia_from_mina", 1) { inventory ->
        val itemStacks = inventory.items().filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it.`is`(MaterialCard.APOSTLE_WAND.item()) } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val itemStack = itemStacks.pull { true } ?: return@registerSpecialRecipe null // アイテム取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = itemStack.item as? MinaItem ?: return@registerSpecialRecipe null // そのアイテムはミナでなければならない
        object : SpecialRecipeResult {
            override fun craft() = MotifCard.MINA.createFairyItemStack(condensation = item.mina)
        }
    }
    registerSpecialRecipe("mina_from_minia", 1) { inventory ->
        val itemStacks = inventory.items().filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it.`is`(MaterialCard.APOSTLE_WAND.item()) } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val fairyItemStack = itemStacks.pull { it.`is`(FairyCard.item()) && it.getFairyMotif() == MotifCard.MINA } ?: return@registerSpecialRecipe null // ミーニャ取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = when (fairyItemStack.getFairyCondensation()) {
            1 -> MaterialCard.MINA_1.item()
            5 -> MaterialCard.MINA_5.item()
            10 -> MaterialCard.MINA_10.item()
            50 -> MaterialCard.MINA_50.item()
            100 -> MaterialCard.MINA_100.item()
            500 -> MaterialCard.MINA_500.item()
            1000 -> MaterialCard.MINA_1000.item()
            5000 -> MaterialCard.MINA_5000.item()
            10000 -> MaterialCard.MINA_10000.item()
            else -> return@registerSpecialRecipe null
        }
        object : SpecialRecipeResult {
            override fun craft() = item.createItemStack()
        }
    }

    // フェアリージュエル両替
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_1.item, MaterialCard.JEWEL_5.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_5.item, MaterialCard.JEWEL_10.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_10.item, MaterialCard.JEWEL_50.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_50.item, MaterialCard.JEWEL_100.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_100.item, MaterialCard.JEWEL_500.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_500.item, MaterialCard.JEWEL_1000.item, 2)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_1000.item, MaterialCard.JEWEL_5000.item, 5)
    registerCompressionRecipeGeneration(MaterialCard.JEWEL_5000.item, MaterialCard.JEWEL_10000.item, 2)

}

class MinaItem(val mina: Int, settings: Properties) : Item(settings)

class ApostleWandItem(settings: Properties) : Item(settings) {
    override fun hasCraftingRemainingItem() = true
    override fun getRecipeRemainder(stack: ItemStack) = stack.item.createItemStack()
}

class DrinkItem(settings: Properties, private val flaming: Int? = null) : Item(settings) {
    companion object {
        val FLAMING_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("drink").toLanguageKey()}.burning" }, "Flaming", "炎上")
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, tooltipComponents: MutableList<Component>, tooltipFlag: TooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag)

        run {
            val foodComponent = stack[DataComponents.FOOD] ?: return@run
            foodComponent.effects.forEach { entry ->
                var text = entry.effect.effect.value().displayName
                if (entry.effect.amplifier > 0) text = text { text + " "() + (entry.effect.amplifier + 1).toRomanText() }
                if (!entry.effect.effect.value().isInstantenous) text = text { text + " (${StringHelper.formatTickDuration(entry.effect.duration, context.tickRate())}"() + ")"() }
                if (entry.probability != 1.0F) text = text { text + " (${entry.probability * 100 formatAs "%.0f"}%)"() }
                text = if (entry.effect.effect.value().isBeneficial) text.blue else text.red
                tooltipComponents += text
            }
        }

        if (flaming != null) tooltipComponents += text { (FLAMING_TRANSLATION() + " (${StringHelper.formatTickDuration(flaming * 20, context.tickRate())}"() + ")"()).red }
    }

    override fun finishUsingItem(stack: ItemStack, world: Level, user: LivingEntity): ItemStack {
        super.finishUsingItem(stack, world, user)
        if (user is ServerPlayerEntity) Criteria.CONSUME_ITEM.trigger(user, stack)
        if (user is PlayerEntity) user.awardStat(Stats.ITEM_USED.get(this))
        user.gameEvent(GameEvent.DRINK)
        if (!world.isClientSide) {
            if (flaming != null) user.igniteForSeconds(flaming.toFloat())
        }
        return if (stack.isEmpty) {
            Items.GLASS_BOTTLE.createItemStack()
        } else {
            if (user !is PlayerEntity || !user.abilities.instabuild) user.obtain(Items.GLASS_BOTTLE.createItemStack())
            stack
        }
    }

    override fun getUseDuration(stack: ItemStack, entity: LivingEntity) = 32
    override fun getUseAnimation(stack: ItemStack) = UseAction.DRINK
    override fun use(world: Level, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> = ItemUsage.startUsingInstantly(world, user, hand)
}
