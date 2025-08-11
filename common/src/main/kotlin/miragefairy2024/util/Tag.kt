package miragefairy2024.util

import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.structure.Structure
import java.util.concurrent.CompletableFuture

fun <T> ResourceLocation.toTag(registry: ResourceKey<Registry<T>>): TagKey<T> = TagKey.create(registry, this)
fun ResourceLocation.toItemTag(): TagKey<Item> = this.toTag(Registries.ITEM)
fun ResourceLocation.toBlockTag(): TagKey<Block> = this.toTag(Registries.BLOCK)
fun ResourceLocation.toBiomeTag(): TagKey<Biome> = this.toTag(Registries.BIOME)
fun ResourceLocation.toDamageTypeTag(): TagKey<DamageType> = this.toTag(Registries.DAMAGE_TYPE)
fun ResourceLocation.toStructureTag(): TagKey<Structure> = this.toTag(Registries.STRUCTURE)

val TagKey<Block>.generator @JvmName("getBlockGenerator") get() = Pair(this, TagGenerator.BLOCK)
val TagKey<Item>.generator @JvmName("getItemGenerator") get() = Pair(this, TagGenerator.ITEM)
val TagKey<Biome>.generator @JvmName("getBiomeGenerator") get() = Pair(this, TagGenerator.BIOME)
val TagKey<Structure>.generator @JvmName("getStructureGenerator") get() = Pair(this, TagGenerator.STRUCTURE)
val TagKey<EntityType<*>>.generator @JvmName("getEntityTypeGenerator") get() = Pair(this, TagGenerator.ENTITY_TYPE)
val TagKey<DamageType>.generator @JvmName("getDamageTypeGenerator") get() = Pair(this, TagGenerator.DAMAGE_TYPE)
val TagKey<Enchantment>.generator @JvmName("getEnchantmentGenerator") get() = Pair(this, TagGenerator.ENCHANTMENT)

context(ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(elementGetter: () -> T) {
    this.second.eventRegistry {
        it(this.first).add(elementGetter())
    }
}

context(ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(resourceLocation: ResourceLocation) {
    this.second.eventRegistry {
        it(this.first).add(resourceLocation)
    }
}

context(ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(resourceKey: ResourceKey<T>) {
    this.second.eventRegistry {
        it(this.first).add(resourceKey)
    }
}

context(ModContext)
fun <T> Pair<TagKey<T>, TagGenerator<T>>.registerChild(tag: TagKey<T>) {
    this.second.eventRegistry {
        it(this.first).addOptionalTag(tag)
    }
}

class TagGenerator<T>(
    private val registryKey: ResourceKey<out Registry<T>>,
    private val reverseLookupFunction: ((T) -> ResourceKey<T>)? = null,
) {
    companion object {
        val entries = mutableListOf<TagGenerator<*>>()
        private operator fun <T> TagGenerator<T>.not() = this.also { entries.add(it) }

        val BLOCK = !TagGenerator(Registries.BLOCK) { element -> element.builtInRegistryHolder().key() }
        val ITEM = !TagGenerator(Registries.ITEM) { element -> element.builtInRegistryHolder().key() }
        val BIOME = !TagGenerator(Registries.BIOME)
        val STRUCTURE = !TagGenerator(Registries.STRUCTURE)
        val ENTITY_TYPE = !TagGenerator(Registries.ENTITY_TYPE)
        val DAMAGE_TYPE = !TagGenerator(Registries.DAMAGE_TYPE)
        val ENCHANTMENT = !TagGenerator(Registries.ENCHANTMENT)
    }

    val eventRegistry = InitializationEventRegistry<((TagKey<T>) -> FabricTagProvider<T>.FabricTagBuilder) -> Unit>()

    fun createProvider(output: FabricDataOutput, registriesFuture: CompletableFuture<HolderLookup.Provider>): FabricTagProvider<T> {
        return object : FabricTagProvider<T>(output, registryKey, registriesFuture) {
            override fun reverseLookup(element: T) = if (reverseLookupFunction == null) super.reverseLookup(element) else reverseLookupFunction(element)
            override fun addTags(arg: HolderLookup.Provider) = eventRegistry.fire { it { tag -> getOrCreateTagBuilder(tag) } }
        }
    }
}
