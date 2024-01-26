package miragefairy2024.mod

import com.faux.customentitydata.api.playersaves.CustomPlayerSave
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.compound
import miragefairy2024.util.get
import miragefairy2024.util.string
import miragefairy2024.util.wrapper
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier


// Api

val extraPlayerDataCategoryRegistryKey: RegistryKey<Registry<ExtraPlayerDataCategory<*>>> = RegistryKey.ofRegistry(Identifier(MirageFairy2024.modId, "extra_player_data_loader"))
val extraPlayerDataCategoryRegistry: Registry<ExtraPlayerDataCategory<*>> = FabricRegistryBuilder.createSimple(extraPlayerDataCategoryRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface ExtraPlayerDataCategory<T> {
    fun castOrThrow(value: Any?): T
    fun toNbt(player: PlayerEntity, data: T): NbtCompound
    fun fromNbt(player: PlayerEntity, nbt: NbtCompound): T
}


// Util

val PlayerEntity.extraPlayerDataContainer: ExtraPlayerDataContainer get() = (this as ExtraPlayerDataContainerGetter).`mirageFairy2024$getExtraPlayerDataContainer`()


// Init

fun initExtraPlayerData() {
    object : CustomPlayerSave(Identifier(MirageFairy2024.modId, "extra_player_data")) {
        override fun savePlayer(player: PlayerEntity) = player.extraPlayerDataContainer.toNbt()
        override fun loadPlayer(player: PlayerEntity, saveData: NbtCompound) = player.extraPlayerDataContainer.fromNbt(saveData)
    }
}


// Mixin Impl

class ExtraPlayerDataContainer(private val player: PlayerEntity) {
    private val map = mutableMapOf<Identifier, Any?>()

    operator fun <T> get(loader: ExtraPlayerDataCategory<T>) = loader.castOrThrow(map[extraPlayerDataCategoryRegistry.getId(loader)])

    operator fun <T> set(loader: ExtraPlayerDataCategory<T>, data: T) = map.put(extraPlayerDataCategoryRegistry.getId(loader)!!, data)

    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        extraPlayerDataCategoryRegistry.entrySet.forEach { (key, loader) ->
            fun <T> f(loader: ExtraPlayerDataCategory<T>) {
                if (key.value !in map) return
                val value = map[key.value]
                val data = try {
                    loader.castOrThrow(value)
                } catch (e: ClassCastException) {
                    MirageFairy2024.logger.error("Failed to cast: ${value?.javaClass} as ${key.value} for ${player.name}(${player.uuid})", e)
                    return
                }
                nbt.wrapper[key.value.string].compound.set(loader.toNbt(player, data))
            }
            f(loader)
        }
        return nbt
    }

    fun fromNbt(nbt: NbtCompound) {
        map.clear()
        extraPlayerDataCategoryRegistry.entrySet.forEach { (key, loader) ->
            fun <T> f(loader: ExtraPlayerDataCategory<T>) {
                map[key.value] = loader.fromNbt(player, nbt.wrapper[key.value.string].compound.get() ?: NbtCompound())
            }
            f(loader)
        }
    }
}

interface ExtraPlayerDataContainerGetter {
    @Suppress("FunctionName")
    fun `mirageFairy2024$getExtraPlayerDataContainer`(): ExtraPlayerDataContainer
}
