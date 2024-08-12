package miragefairy2024.mod.magicplant

import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.text.Text
import net.minecraft.world.biome.Biome

class TraitSpawnSpec(val condition: TraitSpawnCondition, val rarity: TraitSpawnRarity, val level: Int)

interface TraitSpawnCondition {
    val description: Text
    fun canSpawn(biome: RegistryEntry<Biome>): Boolean
}

enum class TraitSpawnRarity {
    /** 必ず付与される。 */
    ALWAYS,

    /** 1%の確率で選ばれるC欠損テーブルに乗る。 */
    COMMON,

    /** 90%の確率で選ばれるN獲得テーブルに乗る。 */
    NORMAL,

    /** 8%の確率で選ばれるR獲得テーブルに乗る。 */
    RARE,

    /** 1%の確率で選ばれるSR獲得テーブルに乗る。 */
    S_RARE,
}
