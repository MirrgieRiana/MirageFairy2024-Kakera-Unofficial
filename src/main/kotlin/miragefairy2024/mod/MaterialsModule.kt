package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.fairy.FairyCard
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.RandomFairySummoningItem
import miragefairy2024.mod.fairy.SOUL_STREAM_CONTAINABLE_TAG
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.getFairyCondensation
import miragefairy2024.mod.fairy.getFairyMotif
import miragefairy2024.mod.fermentationbarrel.registerFermentationBarrelRecipeGeneration
import miragefairy2024.util.EnJa
import miragefairy2024.util.SpecialRecipeResult
import miragefairy2024.util.Translation
import miragefairy2024.util.blue
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.from
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotEmpty
import miragefairy2024.util.itemStacks
import miragefairy2024.util.modId
import miragefairy2024.util.noGroup
import miragefairy2024.util.obtain
import miragefairy2024.util.on
import miragefairy2024.util.plus
import miragefairy2024.util.pull
import miragefairy2024.util.red
import miragefairy2024.util.register
import miragefairy2024.util.registerBlastingRecipeGeneration
import miragefairy2024.util.registerChestLoot
import miragefairy2024.util.registerComposterInput
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
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.block.Blocks
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.FoodComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsage
import net.minecraft.item.Items
import net.minecraft.loot.LootTables
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.stat.Stats
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.StringHelper
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import kotlin.math.pow

class MaterialCard(
    path: String,
    val enName: String,
    val jaName: String,
    val poemList: PoemList?,
    val fuelValue: Int? = null,
    val soulStreamContainable: Boolean = false,
    val foodComponent: FoodComponent? = null,
    val recipeRemainder: Item? = null,
    val creator: (Item.Settings) -> Item = ::Item,
) {
    companion object {
        val entries = mutableListOf<MaterialCard>()
        private operator fun MaterialCard.not() = apply { entries += this }

        val XARPITE = !MaterialCard(
            "xarpite", "Xarpite", "紅天石",
            PoemList(2).poem("Binds astral flux with magnetic force", "黒鉄の鎖は繋がれる。血腥い魂の檻へ。"),
            fuelValue = 200 * 16,
            // TODO 使えるワード：牢獄
        )
        val MIRANAGITE = !MaterialCard(
            "miranagite", "Miranagite", "蒼天石",
            PoemList(2).poem("Astral body crystallized by anti-entropy", "秩序の叛乱、天地創造の逆光。"),
            // TODO The origin of the universe 無限の深淵、破壊と再生の輪廻。
        )
        val MIRANAGITE_ROD = !MaterialCard(
            "miranagite_rod", "Miranagite Rod", "蒼天石の棒",
            PoemList(2).poem("Mana flows well through the core", "蒼天に従える光条は、魔力の祝福を示す。"),
        )
        val CHAOS_STONE = !MaterialCard(
            "chaos_stone", "Chaos Stone", "混沌の石",
            PoemList(4).poem("Chemical promoting catalyst", "魔力の暴走、加速する無秩序の流れ。"),
        )

        val MIRAGE_LEAVES = !MaterialCard(
            "mirage_leaves", "Mirage Leaves", "ミラージュの葉",
            PoemList(1).poem("Don't cut your fingers!", "刻まれる、記憶の破片。"),
            fuelValue = 100,
        )
        val MIRAGE_STEM = !MaterialCard(
            "mirage_stem", "Mirage Stem", "ミラージュの茎",
            PoemList(1).poem("Cell wall composed of amorphous ether", "植物が手掛ける、分子レベルの硝子細工。"),
            fuelValue = 100,
        )
        val FAIRY_GLASS_FIBER = !MaterialCard(
            "fairy_glass_fiber", "Fairy Glass Fiber", "きらめきの糸",
            PoemList(1).poem("Fiber-optic nervous system", "意識の一部だったもの。"),
            soulStreamContainable = true,
        )
        val FAIRY_CRYSTAL = !MaterialCard(
            "fairy_crystal", "Fairy Crystal", "フェアリークリスタル",
            PoemList(2).poem("Crystallized soul", "生物を生物たらしめるもの"),
            soulStreamContainable = true,
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
            foodComponent = FoodComponent.Builder()
                .hunger(2)
                .saturationModifier(0.3F)
                .statusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60), 1.0F)
                .alwaysEdible()
                .build(),
        )
        val MIRAGIUM_NUGGET = !MaterialCard(
            "miragium_nugget", "Miragium Nugget", "ミラジウムナゲット",
            PoemList(3).poem("Dismembered metallic body", "小分けにされた妖精のインゴット。"),
            soulStreamContainable = true,
        )
        val MIRAGIUM_INGOT = !MaterialCard(
            "miragium_ingot", "Miragium Ingot", "ミラジウムインゴット",
            PoemList(3).poem("Metallic body", "妖精インゴット。"),
            soulStreamContainable = true,
        )
        val VEROPEDA_LEAF = !MaterialCard(
            "veropeda_leaf", "Veropeda Leaf", "ヴェロペダの葉",
            PoemList(1).poem("Said to house the soul of a demon", "その身融かされるまでの快楽。"),
            fuelValue = 100,
        )
        val VEROPEDA_BERRIES = !MaterialCard(
            "veropeda_berries", "Veropeda Berries", "ヴェロペダの実",
            PoemList(1)
                .poem("Has analgesic and stimulant effects", "悪魔の囁きを喰らう。")
                .description("Healing and rare nausea by eating", "食べると回復、まれに吐き気"),
            foodComponent = FoodComponent.Builder()
                .hunger(1)
                .saturationModifier(0.1F)
                .snack()
                .statusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 3), 1.0F)
                .statusEffect(StatusEffectInstance(StatusEffects.NAUSEA, 20 * 20), 0.01F)
                .build(),
        )
        val HAIMEVISKA_SAP = !MaterialCard(
            "haimeviska_sap", "Haimeviska Sap", "ハイメヴィスカの樹液",
            PoemList(1)
                .poem("Smooth and mellow on the palate", "口福のアナムネシス。")
                .description("Gain experience by eating", "食べると経験値を獲得"),
            fuelValue = 200,
            foodComponent = FoodComponent.Builder()
                .hunger(1)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(experienceStatusEffect, 20), 1.0F)
                .build(),
        )
        val HAIMEVISKA_ROSIN = !MaterialCard(
            "haimeviska_rosin", "Haimeviska Rosin", "妖精の木の涙",
            PoemList(2).poem("High-friction material", "琥珀の月が昇るとき、妖精の木は静かに泣く"),
            fuelValue = 200,
        )
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
        )
        val MIRAGE_FLOUR = !MaterialCard(
            "mirage_flour", "Mirage Flour", "ミラージュの花粉",
            PoemList(1).poem("Containing metallic organic matter", "叡智の根源、創発のファンタジア。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(0.0), it) },
        )
        val MIRAGE_FLOUR_OF_NATURE = !MaterialCard(
            "mirage_flour_of_nature", "Mirage Flour of Nature", "自然のミラージュの花粉",
            PoemList(1).poem("Use the difference in ether resistance", "艶やかなほたる色に煌めく鱗粉。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(1.0), it) },
        )
        val MIRAGE_FLOUR_OF_EARTH = !MaterialCard(
            "mirage_flour_of_earth", "Mirage Flour of Earth", "大地のミラージュの花粉",
            PoemList(2).poem("As intelligent as humans", "黄金の魂が示す、好奇心の輝き。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(2.0), it) },
        )
        val MIRAGE_FLOUR_OF_UNDERWORLD = !MaterialCard(
            "mirage_flour_of_underworld", "Mirage Flour of Underworld", "地底のミラージュの花粉",
            PoemList(2).poem("Awaken fairies in the world and below", "1,300ケルビンの夜景。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(3.0), it) },
        )
        val MIRAGE_FLOUR_OF_SKY = !MaterialCard(
            "mirage_flour_of_sky", "Mirage Flour of Sky", "天空のミラージュの花粉",
            PoemList(3).poem("Explore atmosphere and nearby universe", "蒼淵を彷徨う影、導きの光。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(4.0), it) },
        )
        val MIRAGE_FLOUR_OF_UNIVERSE = !MaterialCard(
            "mirage_flour_of_universe", "Mirage Flour of Universe", "宇宙のミラージュの花粉",
            PoemList(3)
                .poem("poem1", "Leap spaces by collapsing time crystals,", "運命の束、時の結晶、光速の呪いを退けよ、")
                .poem("poem2", "capture ether beyond observable universe", "讃えよ、アーカーシャに眠る自由の頂きを。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(5.0), it) },
        )
        val MIRAGE_FLOUR_OF_TIME = !MaterialCard(
            "mirage_flour_of_time", "Mirage Flour of Time", "時空のミラージュの花粉",
            PoemList(4)
                .poem("poem1", "Attracts nearby parallel worlds outside", "虚空に眠る時の断片。因果の光が貫くとき、")
                .poem("poem2", "this universe and collects their ether.", "亡失の世界は探し始める。無慈悲な真実を。"),
            soulStreamContainable = true,
            creator = { RandomFairySummoningItem(9.0.pow(6.0), it) },
        )

        val FAIRY_SCALES = !MaterialCard(
            "fairy_scales", "Fairy Scales", "妖精の鱗粉",
            PoemList(1)
                .poem("A catalyst that converts mana into erg", "湧き上がる、エルグの誘い。"),
            soulStreamContainable = true,
            // TODO レシピ 妖精の森バイオームの雑草
            // TODO 妖精からクラフト
            // TODO 用途
        )
        val FRACTAL_WISP = !MaterialCard(
            "fractal_wisp", "Fractal Wisp", "フラクタルウィスプ",
            PoemList(1)
                .poem("poem1", "The fairy of the fairy of the fairy", "妖精の妖精の妖精の妖精の妖精の妖精の妖精")
                .poem("poem2", "of the fairy of the fairy of the f", "の妖精の妖精の妖精の妖精の妖精の妖精の妖"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) }
            // TODO 用途
        )

        val FAIRY_QUEST_CARD_BASE = !MaterialCard(
            "fairy_quest_card_base", "Fairy Quest Card Base", "フェアリークエストカードベース",
            PoemList(1).poem("Am I hopeful in the parallel world?", "存在したかもしれない僕たちのかたち。")
        )

        val MAGNETITE = !MaterialCard(
            "magnetite", "Magnetite", "磁鉄鉱",
            null,
        )

        val FLUORITE = !MaterialCard(
            "fluorite", "Fluorite", "蛍石",
            null,
        )
        val SPHERE_BASE = !MaterialCard(
            "sphere_base", "Sphere Base", "スフィアベース",
            PoemList(2)
                .poem("A mirror that reflects sadistic desires", "前世が見える。              （らしい）"),
            // TODO 用途
        )

        val MINA_1 = !MaterialCard(
            "mina_1", "1 Mina", "1ミナ",
            PoemList(0)
                .poem("Put this money to work until I come back", "私が帰って来るまでこれで商売をしなさい")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(1, it.fireproof()) },
        )
        val MINA_5 = !MaterialCard(
            "mina_5", "5 Mina", "5ミナ",
            PoemList(0)
                .poem("Fairy snack", "ご縁があるよ")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(5, it.fireproof()) },
        )
        val MINA_10 = !MaterialCard(
            "mina_10", "10 Mina", "10ミナ",
            PoemList(0)
                .poem("Can purchase the souls of ten fairies.", "10の妖精が宿る石。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(10, it.fireproof()) },
        )
        val MINA_50 = !MaterialCard(
            "mina_50", "50 Mina", "50ミナ",
            PoemList(0)
                .poem("The Society failed to replicate this.", "形而上学的有機結晶")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(50, it.fireproof()) },
        )
        val MINA_100 = !MaterialCard(
            "mina_100", "100 Mina", "100ミナ",
            PoemList(0)
                .poem("Place where fairies and humans intersect", "妖精と人間が交差する場所。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(100, it.fireproof()) },
        )
        val MINA_500 = !MaterialCard(
            "mina_500", "500 Mina", "500ミナ",
            PoemList(0)
                .poem("A brilliance with a hardness of 7.5", "硬度7.5の輝き。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(500, it.fireproof()) },
        )
        val MINA_1000 = !MaterialCard(
            "mina_1000", "1000 Mina", "1000ミナ",
            PoemList(0)
                .poem("Created by the fairies of commerce.", "妖精の業が磨き上げる。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(1000, it.fireproof()) },
        )
        val MINA_5000 = !MaterialCard(
            "mina_5000", "5000 Mina", "5000ミナ",
            PoemList(0)
                .poem("The price of a soul.", "魂の値段。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(5000, it.fireproof()) },
        )
        val MINA_10000 = !MaterialCard(
            "mina_10000", "10000 Mina", "10000ミナ",
            PoemList(0)
                .poem("Become an eternal gemstone.", "妖花の蜜よ、永遠の宝石となれ。")
                .translation(PoemType.DESCRIPTION, MINA_DESCRIPTION_TRANSLATION),
            soulStreamContainable = true,
            creator = { MinaItem(10000, it.fireproof()) },
        )

        val JEWEL_1 = !MaterialCard(
            "jewel_1", "1 Fairy Jewel", "1フェアリージュエル",
            PoemList(0)
                .poem("Long ago, fairies were the nectar.", "その昔、妖精は木の蜜だった。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_5 = !MaterialCard(
            "jewel_5", "5 Fairy Jewel", "5フェアリージュエル",
            PoemList(0)
                .poem("The nectar bloomed from the ground.", "木の蜜は地に触れ、花を咲かせた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_10 = !MaterialCard(
            "jewel_10", "10 Fairy Jewel", "10フェアリージュエル",
            PoemList(0)
                .poem("The wind, sky, and sun laughed.", "風と空と太陽が笑った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_50 = !MaterialCard(
            "jewel_50", "50 Fairy Jewel", "50フェアリージュエル",
            PoemList(0)
                .poem("Fairies simply drifted along.", "妖精はただ漂っていた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_100 = !MaterialCard(
            "jewel_100", "100 Fairy Jewel", "100フェアリージュエル",
            PoemList(0)
                .poem("One day, humans touched fairies.", "その日、人が現れ、妖精に触れた。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_500 = !MaterialCard(
            "jewel_500", "500 Fairy Jewel", "500フェアリージュエル",
            PoemList(0)
                .poem("Fairies took form and learned emotion.", "妖精は妖精の姿へとなり、感情を知った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_1000 = !MaterialCard(
            "jewel_1000", "1000 Fairy Jewel", "1000フェアリージュエル",
            PoemList(0)
                .poem("Fairies learned joy and pain.", "妖精は悦びと痛みを知った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_5000 = !MaterialCard(
            "jewel_5000", "5000 Fairy Jewel", "5000フェアリージュエル",
            PoemList(0)
                .poem("Humans saw the fairies and felt relief.", "人は妖精を見て、安堵した。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )
        val JEWEL_10000 = !MaterialCard(
            "jewel_10000", "10000 Fairy Jewel", "10000フェアリージュエル",
            PoemList(0)
                .poem("Thus, humans lost their form.", "こうして、人は人の姿を失った。"),
            soulStreamContainable = true,
            creator = { Item(it.fireproof()) },
        )

        val APOSTLE_WAND = !MaterialCard(
            "apostle_wand", "Apostle's Wand", "使徒のステッキ",
            PoemList(2).poem("The key to the fairy world", "妖精界への鍵。"),
            creator = { ApostleWandItem(it.maxCount(1)) },
        )

        val RUM = !MaterialCard(
            "rum", "Rum", "ラム酒",
            null,
            fuelValue = 200 * 4, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponent = FoodComponent.Builder()
                .hunger(6)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(StatusEffects.STRENGTH, 20 * 60, 1), 1.0F)
                .statusEffect(StatusEffectInstance(StatusEffects.NAUSEA, 20 * 60), 0.1F)
                .build(),
            creator = { DrinkItem(it) },
        )
        val CIDRE = !MaterialCard(
            "cidre", "Cidre", "シードル",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponent = FoodComponent.Builder()
                .hunger(6)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(StatusEffects.RESISTANCE, 20 * 60), 1.0F)
                .build(),
            creator = { DrinkItem(it) },
        )
        val FAIRY_LIQUEUR = !MaterialCard(
            "fairy_liqueur", "Fairy Liqueur", "妖精のリキュール",
            PoemList(2).poem("Fairies get high, humans get burned", "妖精はハイになり、人間は火傷する。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponent = FoodComponent.Builder()
                .hunger(6)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(experienceStatusEffect, 20 * 8, 1), 1.0F)
                .build(),
            creator = { DrinkItem(it, flaming = 5) },
        )
        val VEROPEDELIQUORA = !MaterialCard(
            "veropedeliquora", "Veropedeliquora", "ヴェロペデリコラ",
            PoemList(2).poem("A dark flavour from the underworld.", "冥界へといざなう、暗黒の味。"),
            fuelValue = 200 * 12, recipeRemainder = Items.GLASS_BOTTLE,
            foodComponent = FoodComponent.Builder()
                .hunger(6)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 60), 1.0F)
                .statusEffect(StatusEffectInstance(StatusEffects.BLINDNESS, 20 * 60), 0.1F)
                .build(),
            creator = { DrinkItem(it) },
        )
        val POISON = !MaterialCard(
            "poison", "Poison", "毒薬",
            null,
            recipeRemainder = Items.GLASS_BOTTLE,
            foodComponent = FoodComponent.Builder()
                .hunger(1)
                .saturationModifier(0.1F)
                .statusEffect(StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 9), 1.0F)
                .statusEffect(StatusEffectInstance(StatusEffects.WITHER, 20 * 60, 4), 1.0F)
                .build(),
            creator = { DrinkItem(it) },
        )
    }

    val identifier = MirageFairy2024.identifier(path)
    val item = Item.Settings()
        .let { if (foodComponent != null) it.food(foodComponent) else it }
        .let { if (recipeRemainder != null) it.recipeRemainder(recipeRemainder) else it }
        .let { creator(it) }
}

val MIRAGE_FLOUR_TAG: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, MirageFairy2024.identifier("mirage_flour"))

val APPEARANCE_RATE_BONUS_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mirage_flour").toTranslationKey()}.appearance_rate_bonus" }, "Appearance Rate Bonus", "出現率ボーナス")
val MINA_DESCRIPTION_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("mina").toTranslationKey()}.description" }, "Can exchange for Minia with apostle's wand", "使徒のステッキでミーニャと両替可能")

context(ModContext)
fun initMaterialsModule() {
    MaterialCard.entries.forEach { card ->
        card.item.register(Registries.ITEM, card.identifier)
        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        card.item.registerGeneratedModelGeneration()
        card.item.enJa(EnJa(card.enName, card.jaName))
        if (card.poemList != null) {
            card.item.registerPoem(card.poemList)
            card.item.registerPoemGeneration(card.poemList)
        }
        if (card.fuelValue != null) card.item.registerFuel(card.fuelValue)
        if (card.soulStreamContainable) card.item.registerItemTagGeneration { SOUL_STREAM_CONTAINABLE_TAG }
    }

    APPEARANCE_RATE_BONUS_TRANSLATION.enJa()
    MINA_DESCRIPTION_TRANSLATION.enJa()
    DrinkItem.FLAMING_TRANSLATION.enJa()

    fun registerCompressionRecipeGeneration(low: MaterialCard, high: MaterialCard, noGroup: Boolean = false) {
        registerShapedRecipeGeneration(high.item) {
            pattern("###")
            pattern("###")
            pattern("###")
            input('#', low.item)
        }.noGroup(noGroup) on low.item from low.item
        registerShapelessRecipeGeneration(low.item, 9) {
            input(high.item)
        }.noGroup(noGroup) on high.item from high.item
    }
    registerCompressionRecipeGeneration(MaterialCard.TINY_MIRAGE_FLOUR, MaterialCard.MIRAGE_FLOUR, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR, MaterialCard.MIRAGE_FLOUR_OF_NATURE, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_NATURE, MaterialCard.MIRAGE_FLOUR_OF_EARTH, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_EARTH, MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD, MaterialCard.MIRAGE_FLOUR_OF_SKY, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_SKY, MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE, noGroup = true)
    registerCompressionRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE, MaterialCard.MIRAGE_FLOUR_OF_TIME, noGroup = true)

    // 紅天石
    MaterialCard.XARPITE.item.registerGrassDrop(0.03F, 1) // TODO 古代の遺構
    MaterialCard.XARPITE.item.registerMobDrop(EntityType.WITCH, onlyKilledByPlayer = true, dropRate = Pair(0.2F, 0.1F))

    // 蒼天石の棒
    registerShapedRecipeGeneration(MaterialCard.MIRANAGITE_ROD.item) {
        pattern("  #")
        pattern(" # ")
        pattern("#  ")
        input('#', MaterialCard.MIRANAGITE.item)
    } on MaterialCard.MIRANAGITE.item from MaterialCard.MIRANAGITE.item

    // 混沌の石
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.SIMPLE_DUNGEON_CHEST }, 10, 3..5)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.ABANDONED_MINESHAFT_CHEST }, 5)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.ANCIENT_CITY_CHEST }, 10, 1..5)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.DESERT_PYRAMID_CHEST }, 10)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.VILLAGE_DESERT_HOUSE_CHEST }, 3)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.DESERT_PYRAMID_ARCHAEOLOGY }, 1)
    MaterialCard.CHAOS_STONE.item.registerChestLoot({ LootTables.DESERT_WELL_ARCHAEOLOGY }, 1)

    // ミラージュの葉
    MaterialCard.MIRAGE_LEAVES.item.registerComposterInput(0.5F)

    // ミラージュの茎
    registerShapelessRecipeGeneration(MaterialCard.MIRAGE_STEM.item) {
        input(MaterialCard.MIRAGE_LEAVES.item)
    } on MaterialCard.MIRAGE_LEAVES.item
    MaterialCard.MIRAGE_STEM.item.registerComposterInput(0.5F)
    registerShapedRecipeGeneration(Items.STICK, 2) {
        pattern("#")
        pattern("#")
        input('#', MaterialCard.MIRAGE_STEM.item)
    } on MaterialCard.MIRAGE_STEM.item modId MirageFairy2024.MOD_ID from MaterialCard.MIRAGE_STEM.item

    // きらめきの束
    registerShapedRecipeGeneration(MaterialCard.FAIRY_GLASS_FIBER.item) {
        pattern("###")
        pattern("# #")
        pattern("###")
        input('#', MaterialCard.MIRAGE_STEM.item)
    } on MaterialCard.MIRAGE_STEM.item
    registerShapedRecipeGeneration(Items.STRING) {
        pattern("##")
        pattern("##")
        input('#', MaterialCard.FAIRY_GLASS_FIBER.item)
    } on MaterialCard.FAIRY_GLASS_FIBER.item modId MirageFairy2024.MOD_ID from MaterialCard.FAIRY_GLASS_FIBER.item

    // ミラジウム
    registerSmeltingRecipeGeneration(MaterialCard.MIRAGE_FLOUR_OF_NATURE.item, MaterialCard.MIRAGIUM_NUGGET.item) on MaterialCard.MIRAGE_FLOUR_OF_NATURE.item from MaterialCard.MIRAGE_FLOUR_OF_NATURE.item // TODO エルグ炉
    registerCompressionRecipeGeneration(MaterialCard.MIRAGIUM_NUGGET, MaterialCard.MIRAGIUM_INGOT)

    // ヴェロペダの葉
    MaterialCard.VEROPEDA_LEAF.item.registerComposterInput(0.5F)
    registerSmeltingRecipeGeneration(MaterialCard.VEROPEDA_LEAF.item, Items.IRON_NUGGET, 0.1) on MaterialCard.VEROPEDA_LEAF.item modId MirageFairy2024.MOD_ID from MaterialCard.VEROPEDA_LEAF.item
    registerBlastingRecipeGeneration(MaterialCard.VEROPEDA_LEAF.item, Items.IRON_NUGGET, 0.1) on MaterialCard.VEROPEDA_LEAF.item modId MirageFairy2024.MOD_ID from MaterialCard.VEROPEDA_LEAF.item

    // ヴェロペダの実
    MaterialCard.VEROPEDA_BERRIES.item.registerComposterInput(0.3F)

    // ハイメヴィスカの樹液→松明
    registerShapedRecipeGeneration(Items.TORCH) {
        pattern("#")
        pattern("S")
        input('#', MaterialCard.HAIMEVISKA_SAP.item)
        input('S', Items.STICK)
    } on MaterialCard.HAIMEVISKA_SAP.item modId MirageFairy2024.MOD_ID from MaterialCard.HAIMEVISKA_SAP.item

    // 妖精の木の涙→粘着ピストン
    registerShapedRecipeGeneration(Blocks.STICKY_PISTON.asItem()) {
        pattern("S")
        pattern("P")
        input('P', Blocks.PISTON)
        input('S', MaterialCard.HAIMEVISKA_ROSIN.item)
    } on MaterialCard.HAIMEVISKA_ROSIN.item modId MirageFairy2024.MOD_ID from MaterialCard.HAIMEVISKA_ROSIN.item

    // ミラージュの花粉
    MaterialCard.TINY_MIRAGE_FLOUR.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_NATURE.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_EARTH.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_UNDERWORLD.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_SKY.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_UNIVERSE.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }
    MaterialCard.MIRAGE_FLOUR_OF_TIME.item.registerItemTagGeneration { MIRAGE_FLOUR_TAG }

    // 妖精の鱗粉
    MaterialCard.FAIRY_SCALES.item.registerGrassDrop(0.1F, 1)

    // 磁鉄鉱
    registerSmeltingRecipeGeneration(MaterialCard.MAGNETITE.item, Items.IRON_NUGGET, 0.7) on MaterialCard.MAGNETITE.item modId MirageFairy2024.MOD_ID from MaterialCard.MAGNETITE.item

    // 蛍石→スフィアベース
    registerShapedRecipeGeneration(MaterialCard.SPHERE_BASE.item) {
        pattern(" S ")
        pattern("SFS")
        pattern(" S ")
        input('F', MaterialCard.FLUORITE.item)
        input('S', MaterialCard.FAIRY_SCALES.item)
    } on MaterialCard.FLUORITE.item from MaterialCard.FLUORITE.item

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
        val itemStacks = inventory.itemStacks.filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it.isOf(MaterialCard.APOSTLE_WAND.item) } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val itemStack = itemStacks.pull { true } ?: return@registerSpecialRecipe null // アイテム取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = itemStack.item as? MinaItem ?: return@registerSpecialRecipe null // そのアイテムはミナでなければならない
        object : SpecialRecipeResult {
            override fun craft() = MotifCard.MINA.createFairyItemStack(condensation = item.mina)
        }
    }
    registerSpecialRecipe("mina_from_minia", 1) { inventory ->
        val itemStacks = inventory.itemStacks.filter { it.isNotEmpty }.toMutableList()
        if (itemStacks.pull { it.isOf(MaterialCard.APOSTLE_WAND.item) } == null) return@registerSpecialRecipe null // 使徒のステッキ取得
        val fairyItemStack = itemStacks.pull { it.isOf(FairyCard.item) && it.getFairyMotif() == MotifCard.MINA } ?: return@registerSpecialRecipe null // ミーニャ取得
        if (itemStacks.isNotEmpty()) return@registerSpecialRecipe null // 余計なアイテムが入っている
        val item = when (fairyItemStack.getFairyCondensation()) {
            1 -> MaterialCard.MINA_1.item
            5 -> MaterialCard.MINA_5.item
            10 -> MaterialCard.MINA_10.item
            50 -> MaterialCard.MINA_50.item
            100 -> MaterialCard.MINA_100.item
            500 -> MaterialCard.MINA_500.item
            1000 -> MaterialCard.MINA_1000.item
            5000 -> MaterialCard.MINA_5000.item
            10000 -> MaterialCard.MINA_10000.item
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

    // 使徒のステッキ
    registerShapedRecipeGeneration(MaterialCard.APOSTLE_WAND.item) {
        pattern(" G")
        pattern("S ")
        input('S', MaterialCard.MIRAGE_STEM.item)
        input('G', Items.GOLD_INGOT)
    } on MaterialCard.MIRAGE_STEM.item

    // TODO 蒸留装置
    // ラム酒
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(Items.SUGAR_CANE), 16),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.RUM.item.createItemStack(),
        duration = 20 * 60 * 5,
    ) on Items.SUGAR_CANE
    FoodIngredientsRegistry.registry[MaterialCard.RUM.item] = FoodIngredients() + Items.SUGAR_CANE

    // シードル
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(Items.APPLE), 4),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.CIDRE.item.createItemStack(),
        duration = 20 * 60 * 1,
    ) on Items.APPLE
    FoodIngredientsRegistry.registry[MaterialCard.CIDRE.item] = FoodIngredients() + Items.APPLE

    // TODO 醸造樽で作れるのは原酒で、リキュールはマンドレイクを使ってクラフト
    // 妖精のリキュール
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(MaterialCard.HAIMEVISKA_SAP.item), 8),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.FAIRY_LIQUEUR.item.createItemStack(),
        duration = 20 * 60 * 5,
    ) on MaterialCard.HAIMEVISKA_SAP.item
    FoodIngredientsRegistry.registry[MaterialCard.FAIRY_LIQUEUR.item] = FoodIngredients() + MaterialCard.HAIMEVISKA_SAP.item

    // ヴェロペデリコラ
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(MaterialCard.VEROPEDA_BERRIES.item), 8),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.VEROPEDELIQUORA.item.createItemStack(),
        duration = 20 * 60 * 5,
    ) on MaterialCard.VEROPEDA_BERRIES.item
    FoodIngredientsRegistry.registry[MaterialCard.VEROPEDELIQUORA.item] = FoodIngredients() + MaterialCard.VEROPEDA_BERRIES.item

    // 毒薬
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(Items.PUFFERFISH), 1),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.POISON.item.createItemStack(),
        duration = 20 * 5,
    ) on Items.PUFFERFISH from Items.PUFFERFISH
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(Items.POISONOUS_POTATO), 4),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.POISON.item.createItemStack(),
        duration = 20 * 5,
    ) on Items.POISONOUS_POTATO from Items.POISONOUS_POTATO
    registerFermentationBarrelRecipeGeneration(
        input1 = Pair(Ingredient.ofItems(Items.GLASS_BOTTLE), 1),
        input2 = Pair(Ingredient.ofItems(Items.SPIDER_EYE), 4),
        input3 = Pair(WaterBottleIngredient.toVanilla(), 1),
        output = MaterialCard.POISON.item.createItemStack(),
        duration = 20 * 5,
    ) on Items.SPIDER_EYE from Items.SPIDER_EYE

}

context(ModContext)
private fun registerCompressionRecipeGeneration(lowerItem: Item, higherItem: Item, count: Int) {
    registerShapelessRecipeGeneration(higherItem, count = 1) {
        repeat(count) {
            input(lowerItem)
        }
    } on lowerItem from lowerItem
    registerShapelessRecipeGeneration(lowerItem, count = count) {
        input(higherItem)
    } on higherItem from higherItem
}

class MinaItem(val mina: Int, settings: Settings) : Item(settings)

class ApostleWandItem(settings: Settings) : Item(settings) {
    override fun hasRecipeRemainder() = true
    override fun getRecipeRemainder(stack: ItemStack) = stack.item.createItemStack()
}

class DrinkItem(settings: Settings, private val flaming: Int? = null) : Item(settings) {
    companion object {
        val FLAMING_TRANSLATION = Translation({ "item.${MirageFairy2024.identifier("drink").toTranslationKey()}.burning" }, "Flaming", "炎上")
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)

        run {
            val foodComponent = foodComponent ?: return@run
            foodComponent.statusEffects.forEach { entry ->
                var text = entry.first.effectType.name
                if (entry.first.amplifier > 0) text = text { text + " "() + (entry.first.amplifier + 1).toRomanText() }
                if (!entry.first.effectType.isInstant) text = text { text + " (${StringHelper.formatTicks(entry.first.duration)}"() + ")"() }
                if (entry.second != 1.0F) text = text { text + " (${entry.second * 100 formatAs "%.0f"}%)"() }
                text = if (entry.first.effectType.isBeneficial) text.blue else text.red
                tooltip += text
            }
        }

        if (flaming != null) tooltip += text { (FLAMING_TRANSLATION() + " (${StringHelper.formatTicks(flaming * 20)}"() + ")"()).red }
    }

    override fun finishUsing(stack: ItemStack, world: World, user: LivingEntity): ItemStack {
        super.finishUsing(stack, world, user)
        if (user is ServerPlayerEntity) Criteria.CONSUME_ITEM.trigger(user, stack)
        if (user is PlayerEntity) user.incrementStat(Stats.USED.getOrCreateStat(this))
        user.emitGameEvent(GameEvent.DRINK)
        if (!world.isClient) {
            if (flaming != null) user.setOnFireFor(flaming)
        }
        return if (stack.isEmpty) {
            Items.GLASS_BOTTLE.createItemStack()
        } else {
            if (user !is PlayerEntity || !user.abilities.creativeMode) user.obtain(Items.GLASS_BOTTLE.createItemStack())
            stack
        }
    }

    override fun getMaxUseTime(stack: ItemStack) = 32
    override fun getUseAction(stack: ItemStack) = UseAction.DRINK
    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> = ItemUsage.consumeHeldItem(world, user, hand)
}
