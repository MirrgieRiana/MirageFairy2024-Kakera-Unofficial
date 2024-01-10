package miragefairy2024.util

import net.minecraft.client.util.math.MatrixStack

inline fun <T> MatrixStack.pushAndPop(block: () -> T): T {
    this.push()
    try {
        return block()
    } finally {
        this.pop()
    }
}
