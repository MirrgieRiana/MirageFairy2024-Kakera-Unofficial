package miragefairy2024.util

import net.minecraft.world.entity.Entity
import net.minecraft.network.syncher.EntityDataAccessor as TrackedData
import kotlin.reflect.KProperty

context(E)
operator fun <E : Entity, T> TrackedData<T>.getValue(entity: E, property: KProperty<*>): T = entity.entityData.get(this)

context(E)
operator fun <E : Entity, T> TrackedData<T>.setValue(entity: E, property: KProperty<*>, value: T) = entity.entityData.set(this, value)
