package miragefairy2024.mod.magicplant

object NegativeTraitBitsRegistry {
    private val map: MutableMap<Trait, Int?> = mutableMapOf()

    fun get(trait: Trait): Int? = map.getOrDefault(trait, 0)

    fun set(trait: Trait, mask: Int?) {
        if (mask == 0) {
            map -= trait
        } else {
            map[trait] = mask
        }
    }
}
