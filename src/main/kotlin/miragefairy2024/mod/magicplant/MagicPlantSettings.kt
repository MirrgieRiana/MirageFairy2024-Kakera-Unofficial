package miragefairy2024.mod.magicplant

abstract class MagicPlantSettings<B : MagicPlantBlock> {
    abstract val blockPath: String
    abstract val blockEnName: String
    abstract val blockJaName: String
    abstract val itemPath: String
    abstract val itemEnName: String
    abstract val itemJaName: String
    abstract val tier: Int
    abstract val enPoem: String
    abstract val jaPoem: String
    abstract val enClassification: String
    abstract val jaClassification: String

    abstract fun createBlock(): B
}
