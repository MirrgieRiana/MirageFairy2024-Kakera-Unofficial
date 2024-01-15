package miragefairy2024.client.util

import net.minecraft.client.util.math.MatrixStack

inline fun <T> MatrixStack.stack(block: () -> T): T {
    this.push()
    try {
        return block()
    } finally {
        this.pop()
    }
}
