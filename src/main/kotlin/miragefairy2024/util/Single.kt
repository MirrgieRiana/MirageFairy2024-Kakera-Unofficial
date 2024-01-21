package miragefairy2024.util

import java.io.Serializable

// TODO mirrg
data class Single<out A>(val first: A) : Serializable {
    override fun toString() = "($first)"
}
