package miragefairy2024.util

import miragefairy2024.InitializationContext
import miragefairy2024.MirageFairy2024DataGenerator
import miragefairy2024.util.FortuneEffect.IGNORE
import miragefairy2024.util.FortuneEffect.ORE
import miragefairy2024.util.FortuneEffect.UNIFORM
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider
import net.minecraft.block.Block
import net.minecraft.data.server.loottable.BlockLootTableGenerator
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Item
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.entry.AlternativeEntry
import net.minecraft.loot.entry.EmptyEntry
import net.minecraft.loot.entry.GroupEntry
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.entry.LeafEntry
import net.minecraft.loot.entry.LootPoolEntry
import net.minecraft.loot.entry.SequenceEntry
import net.minecraft.loot.function.ApplyBonusLootFunction
import net.minecraft.loot.function.SetCountLootFunction
import net.minecraft.loot.provider.number.UniformLootNumberProvider

inline fun <T> T.configure(block: T.() -> Unit) = this.apply(block)


@Suppress("FunctionName")
fun LootTable(vararg pools: LootPool.Builder, initializer: LootTable.Builder.() -> Unit = {}): LootTable.Builder = LootTable.builder().configure {
    pools.forEach {
        this.pool(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun LootPool(vararg entries: LootPoolEntry.Builder<*>, initializer: LootPool.Builder.() -> Unit = {}): LootPool.Builder = LootPool.builder().configure {
    entries.forEach {
        this.with(it)
    }
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun EmptyLootPoolEntry(initializer: LeafEntry.Builder<*>.() -> Unit = {}): LeafEntry.Builder<*> = EmptyEntry.builder().configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun ItemLootPoolEntry(item: Item, initializer: LeafEntry.Builder<*>.() -> Unit = {}): LeafEntry.Builder<*> = ItemEntry.builder(item).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun AlternativeLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: AlternativeEntry.Builder.() -> Unit = {}): AlternativeEntry.Builder = AlternativeEntry.builder(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun GroupLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: GroupEntry.Builder.() -> Unit = {}): GroupEntry.Builder = GroupEntry.create(*children).configure {
    initializer.invoke(this)
}

@Suppress("FunctionName")
fun SequenceLootPoolEntry(vararg children: LootPoolEntry.Builder<*>, initializer: SequenceEntry.Builder.() -> Unit = {}): SequenceEntry.Builder = SequenceEntry.create(*children).configure {
    initializer.invoke(this)
}


context(InitializationContext)
fun Block.registerLootTableGeneration(initializer: (FabricBlockLootTableProvider) -> LootTable.Builder) = MirageFairy2024DataGenerator.blockLootTableGenerators {
    it.addDrop(this, initializer(it))
}

context(InitializationContext)
fun Block.registerDefaultLootTableGeneration() = this.registerLootTableGeneration {
    it.drops(this)
}

enum class FortuneEffect {
    IGNORE,
    ORE,
    UNIFORM,
}

context(InitializationContext)
fun Block.registerOreLootTableGeneration(drop: Item, additionalCount: ClosedFloatingPointRange<Float>? = null, fortuneEffect: FortuneEffect = ORE) = this.registerLootTableGeneration {
    BlockLootTableGenerator.dropsWithSilkTouch(this, it.applyExplosionDecay(this, ItemLootPoolEntry(drop) {
        if (additionalCount != null) apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(additionalCount.start, additionalCount.endInclusive)))
        when (fortuneEffect) {
            IGNORE -> Unit
            ORE -> apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
            UNIFORM -> apply(ApplyBonusLootFunction.uniformBonusCount(Enchantments.FORTUNE))
        }
    }))
}
