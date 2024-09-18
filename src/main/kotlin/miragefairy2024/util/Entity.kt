package miragefairy2024.util

import net.minecraft.entity.Entity
import net.minecraft.entity.data.TrackedData
import kotlin.reflect.KProperty

context(E)
operator fun <E : Entity, T> TrackedData<T>.getValue(entity: E, property: KProperty<*>): T = entity.dataTracker.get(this)

context(E)
operator fun <E : Entity, T> TrackedData<T>.setValue(entity: E, property: KProperty<*>, value: T) = entity.dataTracker.set(this, value)
