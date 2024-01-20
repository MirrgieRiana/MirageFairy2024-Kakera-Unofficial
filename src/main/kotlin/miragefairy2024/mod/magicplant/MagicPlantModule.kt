package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.PoemList
import miragefairy2024.mod.magicplant.magicplants.MirageFlowerCard
import miragefairy2024.mod.magicplant.magicplants.initMirageFlower
import miragefairy2024.mod.magicplant.magicplants.initVeropeda
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.en
import miragefairy2024.util.enJa
import miragefairy2024.util.ja
import miragefairy2024.util.register
import miragefairy2024.util.text
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos

val TRAIT_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.trait" }, "Trait", "特性")
val CREATIVE_ONLY_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.creativeOnly" }, "Creative Only", "クリエイティブ専用")
val INVALID_TRANSLATION = Translation({ "item.miragefairy2024.magicplant.invalid" }, "Invalid", "無効")

val magicPlantSeedItemGroup: RegistryKey<ItemGroup> = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier(MirageFairy2024.modId, "magic_plant_seeds"))

fun initMagicPlantModule() {

    TraitEffectKeyCard.entries.forEach { card ->
        card.traitEffectKey.register(traitEffectKeyRegistry, card.identifier)
        card.traitEffectKey.enJa(card.enName, card.jaName)
    }

    TraitCard.entries.forEach { card ->
        card.trait.register(traitRegistry, card.identifier)
        card.trait.enJa(card.enName, card.jaName)
    }

    worldGenTraitGenerations += RecipeWorldGenTraitGeneration()

    TRAIT_TRANSLATION.enJa()
    CREATIVE_ONLY_TRANSLATION.enJa()
    INVALID_TRANSLATION.enJa()

    val itemGroup = FabricItemGroup.builder()
        .icon { MirageFlowerCard.item.createItemStack() }
        .displayName(text { translate("itemGroup.magic_plant_seeds") })
        .build()
    itemGroup.register(Registries.ITEM_GROUP, magicPlantSeedItemGroup.value)

    en { "itemGroup.magic_plant_seeds" to "Magic Plant Seeds" }
    ja { "itemGroup.magic_plant_seeds" to "魔法植物の種子" }


    initMirageFlower()
    initVeropeda()

}

abstract class MagicPlantCard<B : MagicPlantBlock, BE : BlockEntity>(
    blockPath: String,
    val blockEnName: String,
    val blockJaName: String,
    itemPath: String,
    val itemEnName: String,
    val itemJaName: String,
    val seedPoemList: PoemList,
    blockCreator: () -> B,
    blockEntityCreator: (BlockPos, BlockState) -> BE,
) {
    companion object {
        fun createCommonSettings(): FabricBlockSettings = FabricBlockSettings.create().noCollision().ticksRandomly().pistonBehavior(PistonBehavior.DESTROY)
    }

    val blockIdentifier = Identifier(MirageFairy2024.modId, blockPath)
    val itemIdentifier = Identifier(MirageFairy2024.modId, itemPath)
    val block = blockCreator()
    val blockEntityType = BlockEntityType(blockEntityCreator, setOf(block), null)
    val item = MagicPlantSeedItem(block, Item.Settings())
}


val magicPlantCropNotations = mutableListOf<MagicPlantCropNotation>()

class MagicPlantCropNotation(val seed: ItemStack, val crops: List<ItemStack>)

fun registerMagicPlantDropNotation(seed: Item, vararg drops: Item) {
    magicPlantCropNotations += MagicPlantCropNotation(seed.createItemStack(), drops.map { it.createItemStack() })
}
