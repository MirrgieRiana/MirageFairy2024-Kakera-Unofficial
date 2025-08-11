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

context(ModContext) fun (() -> Block).registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = TagGenerator.BLOCK.eventRegistry { it(tagProvider()).add(this()) }
context(ModContext) fun TagKey<Block>.registerBlockTagGeneration(tagProvider: () -> TagKey<Block>) = TagGenerator.BLOCK.eventRegistry { it(tagProvider()).addOptionalTag(this) }
context(ModContext) fun (() -> Item).registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = TagGenerator.ITEM.eventRegistry { it(tagProvider()).add(this()) }
context(ModContext) fun TagKey<Item>.registerItemTagGeneration(tagProvider: () -> TagKey<Item>) = TagGenerator.ITEM.eventRegistry { it(tagProvider()).addOptionalTag(this) }
context(ModContext) fun ResourceLocation.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = TagGenerator.BIOME.eventRegistry { it(tagProvider()).add(this) }
context(ModContext) fun ResourceLocation.registerStructureTagGeneration(tagProvider: () -> TagKey<Structure>) = TagGenerator.STRUCTURE.eventRegistry { it(tagProvider()).add(this) }
context(ModContext) fun TagKey<Biome>.registerBiomeTagGeneration(tagProvider: () -> TagKey<Biome>) = TagGenerator.BIOME.eventRegistry { it(tagProvider()).addOptionalTag(this) }
context(ModContext) fun (() -> EntityType<*>).registerEntityTypeTagGeneration(tagProvider: () -> TagKey<EntityType<*>>) = TagGenerator.ENTITY_TYPE.eventRegistry { it(tagProvider()).add(this()) }
context(ModContext) fun TagKey<EntityType<*>>.registerEntityTypeTagGeneration(tagProvider: () -> TagKey<EntityType<*>>) = TagGenerator.ENTITY_TYPE.eventRegistry { it(tagProvider()).addOptionalTag(this) }
context(ModContext) fun ResourceLocation.registerDamageTypeTagGeneration(tagProvider: () -> TagKey<DamageType>) = TagGenerator.DAMAGE_TYPE.eventRegistry { it(tagProvider()).add(this) }
context(ModContext) fun TagKey<DamageType>.registerDamageTypeTagGeneration(tagProvider: () -> TagKey<DamageType>) = TagGenerator.DAMAGE_TYPE.eventRegistry { it(tagProvider()).addOptionalTag(this) }
context(ModContext) fun ResourceKey<Enchantment>.registerEnchantmentTagGeneration(tagProvider: () -> TagKey<Enchantment>) = TagGenerator.ENCHANTMENT.eventRegistry { it(tagProvider()).add(this) }
context(ModContext) fun TagKey<Enchantment>.registerEnchantmentTagGeneration(tagProvider: () -> TagKey<Enchantment>) = TagGenerator.ENCHANTMENT.eventRegistry { it(tagProvider()).addOptionalTag(this) }

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
