package miragefairy2024.util

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType

operator fun <T> AttachmentTarget.get(type: AttachmentType<T>) = AttachmentDelegate(this, type)

class AttachmentDelegate<T>(val value: AttachmentTarget, val type: AttachmentType<T>)

fun <T> AttachmentDelegate<T>.get(): T? {
    return this.value.getAttached(this.type)
}

fun <T> AttachmentDelegate<T>.getOrDefault(): T {
    return this.value.getAttached(this.type) ?: this.type.initializer()!!.get()
}

fun <T> AttachmentDelegate<T>.getOrCreate(): T {
    return this.value.getAttachedOrCreate(this.type)
}

fun <T> AttachmentDelegate<T>.set(data: T?) {
    this.value.setAttached(this.type, data)
}

fun <T> AttachmentDelegate<T>.modify(block: (T?) -> T?) {
    val data = this.get()
    val data2 = block(data)
    this.set(data2)
}

fun <T> AttachmentDelegate<T>.mutate(block: (T) -> Unit) {
    val data = this.get() ?: this.type.initializer()!!.get()
    block(data)
    this.set(data)
}
