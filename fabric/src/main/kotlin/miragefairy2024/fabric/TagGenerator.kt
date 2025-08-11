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
import java.util.concurrent.CompletableFuture

enum class TagGeneratorCard(val tagGenerator: TagGenerator<*>) {
    BLOCK(TagGenerator(DataGenerationEvents.onGenerateBlockTag, Registries.BLOCK) { element -> element.builtInRegistryHolder().key() }),
    ITEM(TagGenerator(DataGenerationEvents.onGenerateItemTag, Registries.ITEM) { element -> element.builtInRegistryHolder().key() }),
    BIOME(TagGenerator(DataGenerationEvents.onGenerateBiomeTag, Registries.BIOME)),
    STRUCTURE(TagGenerator(DataGenerationEvents.onGenerateStructureTag, Registries.STRUCTURE)),
    ENTITY_TYPE(TagGenerator(DataGenerationEvents.onGenerateEntityTypeTag, Registries.ENTITY_TYPE)),
    DAMAGE_TYPE(TagGenerator(DataGenerationEvents.onGenerateDamageTypeTag, Registries.DAMAGE_TYPE)),
    ENCHANTMENT(TagGenerator(DataGenerationEvents.onGenerateEnchantmentTag, Registries.ENCHANTMENT)),
}

class TagGenerator<T>(
    private val eventRegistry: InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>,
    private val registryKey: ResourceKey<out Registry<T>>,
    private val reverseLookupFunction: ((T) -> ResourceKey<T>)? = null,
) {
    fun createProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>): FabricTagProvider<T> {
        return object : FabricTagProvider<T>(output, registryKey, registriesFuture) {
            override fun reverseLookup(element: T) = if (reverseLookupFunction == null) super.reverseLookup(element) else reverseLookupFunction(element)
            override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
        }
    }
}
