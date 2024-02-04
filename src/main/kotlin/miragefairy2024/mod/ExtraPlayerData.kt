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

interface ExtraPlayerDataCategory<T : Any> {
    fun create(): T
    fun castOrThrow(value: Any): T
    val ioHandler: IoHandler<T>? get() = null

    interface IoHandler<T> {
        fun toNbt(player: PlayerEntity, data: T): NbtCompound
        fun fromNbt(player: PlayerEntity, nbt: NbtCompound): T
    }
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
    private val map = mutableMapOf<Identifier, Any>()

    /**
     * このコンテナに格納されているオブジェクトを取得します。
     * このコンテナにオブジェクトが格納されていない場合、nullを返します。
     */
    operator fun <T : Any> get(loader: ExtraPlayerDataCategory<T>): T? {
        val value = map[extraPlayerDataCategoryRegistry.getId(loader)!!] ?: return null
        return loader.castOrThrow(value)
    }

    fun <T : Any> getOrInit(loader: ExtraPlayerDataCategory<T>): T {
        val value = map[extraPlayerDataCategoryRegistry.getId(loader)!!]
        return if (value != null) {
            loader.castOrThrow(value)
        } else {
            val newValue = loader.create()
            map[extraPlayerDataCategoryRegistry.getId(loader)!!] = newValue
            newValue
        }
    }

    val entries: Set<Map.Entry<Identifier, Any>> get() = map.entries

    /**
     * このコンテナにオブジェクトを代入します。
     * [data]がnullだった場合、このコンテナに格納されているオブジェクトを削除します。
     */
    operator fun <T : Any> set(loader: ExtraPlayerDataCategory<T>, data: T?) {
        if (data == null) {
            map.remove(extraPlayerDataCategoryRegistry.getId(loader)!!)
        } else {
            map[extraPlayerDataCategoryRegistry.getId(loader)!!] = data
        }
    }

    /**
     * 登録されているすべての拡張プレイヤーデータカテゴリを書き込みます。
     * このコンテナにオブジェクトが格納されていない場合、データの書き込みはキャンセルされます。
     */
    fun toNbt(): NbtCompound {
        val nbt = NbtCompound()
        extraPlayerDataCategoryRegistry.entrySet.forEach { (key, loader) ->
            fun <T : Any> f(loader: ExtraPlayerDataCategory<T>) {
                val ioHandler = loader.ioHandler ?: return
                val value = map[key.value] ?: return
                val data = try {
                    loader.castOrThrow(value)
                } catch (e: ClassCastException) {
                    MirageFairy2024.logger.error("Failed to load: ${value.javaClass} as ${key.value} for ${player.name}(${player.uuid})", e)
                    return
                }
                nbt.wrapper[key.value.string].compound.set(ioHandler.toNbt(player, data))
            }
            f(loader)
        }
        return nbt
    }

    /**
     * 登録されているすべての拡張プレイヤーデータカテゴリを読み取ります。
     * [nbt]にデータが保存されていない場合、オブジェクトの読み取りはキャンセルされます。
     */
    fun fromNbt(nbt: NbtCompound) {
        map.clear()
        extraPlayerDataCategoryRegistry.entrySet.forEach { (key, loader) ->
            fun <T : Any> f(loader: ExtraPlayerDataCategory<T>) {
                val ioHandler = loader.ioHandler ?: return
                val data = nbt.wrapper[key.value.string].compound.get() ?: return
                map[key.value] = ioHandler.fromNbt(player, data)
            }
            f(loader)
        }
    }
}

interface ExtraPlayerDataContainerGetter {
    @Suppress("FunctionName")
    fun `mirageFairy2024$getExtraPlayerDataContainer`(): ExtraPlayerDataContainer
}
