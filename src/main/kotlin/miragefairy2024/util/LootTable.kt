package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.world.level.block.Block
import net.minecraft.data.loot.BlockLootSubProvider as BlockLootTableGenerator
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry as AlternativeEntry
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem as EmptyEntry
import net.minecraft.world.level.storage.loot.entries.EntryGroup as GroupEntry
import net.minecraft.world.level.storage.loot.entries.LootItem as ItemEntry
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer as LeafEntry
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer as LootPoolEntry
import net.minecraft.world.level.storage.loot.entries.SequentialEntry as SequenceEntry
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount as ApplyBonusLootFunction
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction as SetCountLootFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator as UniformLootNumberProvider
import net.minecraft.resources.ResourceLocation

inline fun <T> T.configure(block: T.() -> Unit) = this.apply(block)


@Suppress("FunctionName")
fun LootTable(vararg pools: LootPool.Builder, initializer: LootTable.Builder.() -> Unit = {}): LootTable.Builder = LootTable.lootTable().configure {
    pools.forEach {
        this.withPool(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun LootPool(vararg entries: LootPoolEntry.Builder<*>, initializer: LootPool.Builder.() -> Unit = {}): LootPool.Builder = LootPool.lootPool().configure {
    entries.forEach {
        this.add(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun EmptyLootPoolEntry(initializer: LeafEntry.Builder<*>.() -> Unit = {}): LeafEntry.Builder<*> = EmptyEntry.emptyItem().configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun ItemLootPoolEntry(item: Item, initializer: LeafEntry.Builder<*>.() -> Unit = {}): LeafEntry.Builder<*> = ItemEntry.lootTableItem(item).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun AlternativeLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: AlternativeEntry.Builder.() -> Unit = {}): AlternativeEntry.Builder = AlternativeEntry.alternatives(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun GroupLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: GroupEntry.Builder.() -> Unit = {}): GroupEntry.Builder = GroupEntry.list(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun SequenceLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: SequenceEntry.Builder.() -> Unit = {}): SequenceEntry.Builder = SequenceEntry.sequential(*children).configure {
    initializer.invoke(this)
}


context(ModContext)
fun Block.registerLootTableGeneration(initializer: (FabricBlockLootTableProvider) -> LootTable.Builder) = DataGenerationEvents.onGenerateBlockLootTable {
    it.add(this, initializer(it).setRandomSequence(this.lootTable))
}

context(ModContext)
fun Block.registerDefaultLootTableGeneration() = this.registerLootTableGeneration {
    it.createSingleItemTable(this)
}

context(ModContext)
fun registerChestLootTableGeneration(lootTableId: ResourceLocation, initializer: () -> LootTable.Builder) = DataGenerationEvents.onGenerateChestLootTable {
    it(lootTableId, initializer().setRandomSequence(lootTableId))
}

context(ModContext)
fun registerArchaeologyLootTableGeneration(lootTableId: ResourceLocation, initializer: () -> LootTable.Builder) = DataGenerationEvents.onGenerateArchaeologyLootTable {
    it(lootTableId, initializer().setRandomSequence(lootTableId))
}

context(ModContext)
fun EntityType<*>.registerLootTableGeneration(initializer: () -> LootTable.Builder) = DataGenerationEvents.onGenerateEntityLootTable {
    it(this, initializer().setRandomSequence(this.defaultLootTable))
}

enum class FortuneEffect {
    IGNORE,
    ORE,
    UNIFORM,
}

context(ModContext)
fun Block.registerOreLootTableGeneration(drop: Item, additionalCount: ClosedFloatingPointRange<Float>? = null, fortuneEffect: FortuneEffect = FortuneEffect.ORE) = this.registerLootTableGeneration {
    BlockLootTableGenerator.createSilkTouchDispatchTable(this, it.applyExplosionDecay(this, ItemLootPoolEntry(drop) {
        if (additionalCount != null) apply(SetCountLootFunction.setCount(UniformLootNumberProvider.between(additionalCount.start, additionalCount.endInclusive)))
        when (fortuneEffect) {
            FortuneEffect.IGNORE -> Unit
            FortuneEffect.ORE -> apply(ApplyBonusLootFunction.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
            FortuneEffect.UNIFORM -> apply(ApplyBonusLootFunction.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
        }
    }))
}
