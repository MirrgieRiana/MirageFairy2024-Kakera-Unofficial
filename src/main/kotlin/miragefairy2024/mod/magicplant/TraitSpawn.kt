package miragefairy2024.mod.magicplant

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
