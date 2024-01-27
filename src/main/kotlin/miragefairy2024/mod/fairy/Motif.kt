package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.BlockMaterialCard
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.world.biome.Biome

val motifRegistryKey: RegistryKey<Registry<Motif>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "motif"))
val motifRegistry: Registry<Motif> = FabricRegistryBuilder.createSimple(motifRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

fun Motif.getIdentifier() = motifRegistry.getId(this)

interface Motif {
    val displayName: Text
    val skinColor: Int
    val frontColor: Int
    val backColor: Int
    val hairColor: Int
    val rare: Int
}


enum class MotifCard(
    path: String,
    override val rare: Int,
    enName: String,
    jaName: String,
    override val skinColor: Int,
    override val frontColor: Int,
    override val backColor: Int,
    override val hairColor: Int,
    val recipe: Recipes,
) : Motif {
    AIR(
        "air", 0, "Airia", "空気精アイリャ", 0xFFBE80, 0xDEFFFF, 0xDEFFFF, 0xB0FFFF,
        Recipes().always + Blocks.AIR,
    ),
    LIGHT(
        "light", 3, "Lightia", "光精リグチャ", 0xFFFFD8, 0xFFFFD8, 0xFFFFC5, 0xFFFF00,
        Recipes().overworld,
    ),
    VACUUM_DECAY(
        "vacuum_decay", 13, "Vacuume Decia", "真空崩壊精ヴァツーメデーツャ", 0x00003B, 0x000012, 0x000012, 0x000078,
        Recipes().always + BlockMaterialCard.LOCAL_VACUUM_DECAY.block,
    ),
    SUN(
        "sun", 10, "Sunia", "太陽精スーニャ", 0xff2f00, 0xff972b, 0xff7500, 0xffe7b2,
        Recipes().overworld,
    ),
    FIRE(
        "fire", 2, "Firia", "火精フィーリャ", 0xFF6C01, 0xF9DFA4, 0xFF7324, 0xFF4000,
        Recipes().nether + Blocks.FIRE,
    ),
    WATER(
        "water", 1, "Wateria", "水精ワテーリャ", 0x5469F2, 0x5985FF, 0x172AD3, 0x2D40F4,
        Recipes().overworld + Blocks.WATER,
    ),
    DIRT(
        "dirt", 1, "Dirtia", "土精ディルチャ", 0xB87440, 0xB9855C, 0x593D29, 0x914A18,
        Recipes().overworld + BlockTags.DIRT,
    ),
    IRON(
        "iron", 4, "Ironia", "鉄精イローニャ", 0xA0A0A0, 0xD8D8D8, 0x727272, 0xD8AF93,
        Recipes().overworld + Blocks.IRON_BLOCK + Items.IRON_INGOT,
    ),
    DIAMOND(
        "diamond", 7, "Diamondia", "金剛石精ディアモンジャ", 0x97FFE3, 0xD1FAF3, 0x70FFD9, 0x30DBBD,
        Recipes().overworld + Blocks.DIAMOND_BLOCK + Items.DIAMOND,
    ),
    PLAYER(
        "player", 5, "Playeria", "人精プライェーリャ", 0xB58D63, 0x00AAAA, 0x322976, 0x4B3422,
        Recipes().always + EntityType.PLAYER,
    ),
    ENDERMAN(
        "enderman", 6, "Endermania", "終界人精エンデルマーニャ", 0x000000, 0x161616, 0x161616, 0xEF84FA,
        Recipes().overworld.nether.end + EntityType.ENDERMAN,
    ),
    WITHER(
        "wither", 8, "Witheria", "枯精ウィテーリャ", 0x181818, 0x3C3C3C, 0x141414, 0x557272,
        Recipes().nether + EntityType.WITHER,
    ),
    CARROT(
        "carrot", 4, "Carrotia", "人参精ツァッローチャ", 0xF98D10, 0xFD7F11, 0xE3710F, 0x248420,
        Recipes().overworld + Blocks.CARROTS + Items.CARROT,
    ),
    CAKE(
        "cake", 4, "Cakia", "蛋麭精ツァーキャ", 0xCC850C, 0xF5F0DC, 0xD3D0BF, 0xDE3334,
        Recipes().always + Blocks.CAKE + Items.CAKE + BlockTags.CANDLE_CAKES,
    ),
    MAGENTA_GLAZED_TERRACOTTA(
        "magenta_glazed_terracotta", 3, "Magente Glazede Terracottia", "赤紫釉陶精マゲンテグラゼデテッラツォッチャ",
        0xFFFFFF, 0xF4B5CB, 0xCB58C2, 0x9D2D95,
        Recipes().always + Blocks.MAGENTA_GLAZED_TERRACOTTA,
    ),
    BEACON(
        "beacon", 11, "Beaconia", "信標精ベアツォーニャ", 0x97FFE3, 0x6029B3, 0x2E095E, 0xD4EAE6,
        Recipes().always + Blocks.BEACON,
    ),
    TIME(
        "time", 14, "Timia", "時精ティーミャ", 0xCDFFBF, 0xD5DEBC, 0xD8DEA7, 0x8DD586,
        Recipes().always,
    ),
    GRAVITY(
        "gravity", 12, "Gravitia", "重力精グラヴィーチャ", 0xC2A7F2, 0x3600FF, 0x2A00B1, 0x110047,
        Recipes().always + Items.APPLE,
    ),
    ANTI_ENTROPY(
        "anti_entropy", 13, "Ante Entropia", "秩序精アンテエントローピャ", 0xD4FCFF, 0x9EECFF, 0x9EECFF, 0x54C9FF,
        Recipes().always,
    ),
    ;

    val identifier = Identifier(MirageFairy2024.modId, path)
    val translation = Translation({ "miragefairy2024.motif.${identifier.toTranslationKey()}" }, enName, jaName)
    override val displayName = translation()

    class Recipes {
        val recipes = mutableListOf<(MotifCard) -> Unit>()
        fun onInit(recipe: (MotifCard) -> Unit): Recipes {
            recipes += recipe
            return this
        }
    }

}

private fun MotifCard.Recipes.common(biome: TagKey<Biome>? = null) = this.onInit { card ->
    COMMON_MOTIF_RECIPES += CommonMotifRecipe(card, biome)
}


private val MotifCard.Recipes.always get() = this.common()
private val MotifCard.Recipes.overworld get() = this.common(ConventionalBiomeTags.IN_OVERWORLD)
private val MotifCard.Recipes.nether get() = this.common(ConventionalBiomeTags.IN_NETHER)
private val MotifCard.Recipes.end get() = this.common(ConventionalBiomeTags.IN_THE_END)

private operator fun MotifCard.Recipes.plus(item: Item) = this.onInit { FairyDreamRecipes.ITEM.register(item, it) }
private operator fun MotifCard.Recipes.plus(block: Block) = this.onInit { FairyDreamRecipes.BLOCK.register(block, it) }
private operator fun MotifCard.Recipes.plus(entityType: EntityType<*>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.register(entityType, it) }

@JvmName("plusItem")
private operator fun MotifCard.Recipes.plus(tag: TagKey<Item>) = this.onInit { FairyDreamRecipes.ITEM.registerFromTag(tag, it) }

@JvmName("plusBlock")
private operator fun MotifCard.Recipes.plus(tag: TagKey<Block>) = this.onInit { FairyDreamRecipes.BLOCK.registerFromTag(tag, it) }

@JvmName("plusEntityType")
private operator fun MotifCard.Recipes.plus(tag: TagKey<EntityType<*>>) = this.onInit { FairyDreamRecipes.ENTITY_TYPE.registerFromTag(tag, it) }


fun initMotif() {
    MotifCard.entries.forEach { card ->
        card.register(motifRegistry, card.identifier)
        card.translation.enJa()
        card.recipe.recipes.forEach {
            it(card)
        }
    }
}
