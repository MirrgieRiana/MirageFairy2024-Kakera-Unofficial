package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.TagGenerator
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
