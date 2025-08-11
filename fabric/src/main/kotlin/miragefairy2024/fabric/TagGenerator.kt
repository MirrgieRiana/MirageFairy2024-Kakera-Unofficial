package miragefairy2024.fabric

import miragefairy2024.DataGenerationEvents
import miragefairy2024.InitializationEventRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import java.util.concurrent.CompletableFuture

enum class TagGeneratorCard {
    BLOCK,
    ITEM,
    BIOME,
    STRUCTURE,
    ENTITY_TYPE,
    DAMAGE_TYPE,
    ENCHANTMENT,
}

fun TagGeneratorCard.createTagGenerator(): TagGenerator<*> {
    return when (this) {
        TagGeneratorCard.BLOCK -> TagGenerator(DataGenerationEvents.onGenerateBlockTag) { output, registriesFuture, adder ->
            object : FabricTagProvider<Block>(output, Registries.BLOCK, registriesFuture) {
                override fun reverseLookup(element: Block) = element.builtInRegistryHolder().key()
                override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
            }
        }

        TagGeneratorCard.ITEM -> TagGenerator(DataGenerationEvents.onGenerateItemTag) { output, registriesFuture, adder ->
            object : FabricTagProvider<Item>(output, Registries.ITEM, registriesFuture) {
                override fun reverseLookup(element: Item) = element.builtInRegistryHolder().key()
                override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
            }
        }

        TagGeneratorCard.BIOME -> SimpleTagGenerator(DataGenerationEvents.onGenerateBiomeTag, Registries.BIOME)
        TagGeneratorCard.STRUCTURE -> SimpleTagGenerator(DataGenerationEvents.onGenerateStructureTag, Registries.STRUCTURE)
        TagGeneratorCard.ENTITY_TYPE -> SimpleTagGenerator(DataGenerationEvents.onGenerateEntityTypeTag, Registries.ENTITY_TYPE)
        TagGeneratorCard.DAMAGE_TYPE -> SimpleTagGenerator(DataGenerationEvents.onGenerateDamageTypeTag, Registries.DAMAGE_TYPE)
        TagGeneratorCard.ENCHANTMENT -> SimpleTagGenerator(DataGenerationEvents.onGenerateEnchantmentTag, Registries.ENCHANTMENT)
    }
}

abstract class TagGenerator<T>(private val eventRegistry: InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>) {
    fun createProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>): FabricTagProvider<T> {
        val adder: ((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit = { builderFactory ->
            eventRegistry.fire { listener -> listener(builderFactory) }
        }
        return createProviderImpl(output, registriesFuture, adder)
    }

    abstract fun createProviderImpl(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>, adder: ((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit): FabricTagProvider<T>
}

private fun <T> TagGenerator(
    eventRegistry: InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>,
    factory: (FabricDataOutput, CompletableFuture<HolderLookup.Provider>, ((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit) -> FabricTagProvider<T>,
): TagGenerator<T> {
    return object : TagGenerator<T>(eventRegistry) {
        override fun createProviderImpl(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>, adder: ((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit): FabricTagProvider<T> {
            return factory(output, registriesFuture, adder)
        }
    }
}

private fun <T> SimpleTagGenerator(
    eventRegistry: InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>,
    registryKey: ResourceKey<out Registry<T>>,
    reverseLookup: ((T) -> ResourceKey<T>)? = null,
): TagGenerator<T> {
    return TagGenerator(eventRegistry) { output, registriesFuture, adder ->
        object : FabricTagProvider<T>(output, registryKey, registriesFuture) {
            override fun reverseLookup(element: T) = if (reverseLookup == null) super.reverseLookup(element) else reverseLookup(element)
            override fun addTags(arg: HolderLookup.Provider) = adder { tag -> getOrCreateTagBuilder(tag) }
        }
    }
}
