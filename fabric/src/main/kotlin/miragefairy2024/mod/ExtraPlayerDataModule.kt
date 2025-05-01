package miragefairy2024.mod

import com.faux.customentitydata.api.playersaves.CustomPlayerSave
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.Channel
import miragefairy2024.util.compound
import miragefairy2024.util.get
import miragefairy2024.util.sendToClient
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import miragefairy2024.util.wrapper
import mirrg.kotlin.slf4j.hydrogen.getLogger
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.fabricmc.fabric.api.event.registry.RegistryAttribute
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.nbt.CompoundTag as NbtCompound
import net.minecraft.server.level.ServerPlayer as ServerPlayerEntity
import net.minecraft.world.entity.player.Player as PlayerEntity


// Api

val extraPlayerDataCategoryRegistryKey: ResourceKey<Registry<ExtraPlayerDataCategory<*>>> = ResourceKey.createRegistryKey(MirageFairy2024.identifier("extra_player_data_loader"))
val extraPlayerDataCategoryRegistry: Registry<ExtraPlayerDataCategory<*>> = FabricRegistryBuilder.createSimple(extraPlayerDataCategoryRegistryKey).attribute(RegistryAttribute.SYNCED).buildAndRegister()

interface ExtraPlayerDataCategory<T : Any> {
    fun create(): T
    fun castOrThrow(value: Any): T
    val ioHandler: IoHandler<T>? get() = null

    interface IoHandler<T> {
        fun toNbt(data: T, registry: HolderLookup.Provider): NbtCompound
        fun fromNbt(nbt: NbtCompound, registry: HolderLookup.Provider): T
    }
}


// Util

val PlayerEntity.extraPlayerDataContainer: ExtraPlayerDataContainer get() = (this as ExtraPlayerDataContainerGetter).`mirageFairy2024$getExtraPlayerDataContainer`()


// Init

context(ModContext)
fun initExtraPlayerDataModule() {

    // インスタンス再生成時（死亡・一部のディメンション移動）にデータを維持
    ServerPlayerEvents.COPY_FROM.register { oldPlayer, newPlayer, _ ->
        newPlayer.extraPlayerDataContainer.fromNbt(oldPlayer.extraPlayerDataContainer.toNbt())
    }

    // ディメンション移動時、データ同期を要求
    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ ->
        player.extraPlayerDataContainer.isDirty = true
    }

    // ログイン時およびdirtyにされたとき、プレイヤーデータを同期
    ServerTickEvents.END_SERVER_TICK.register { server ->
        server.playerList.players.forEach { player ->
            if (player.extraPlayerDataContainer.isDirty) {
                player.extraPlayerDataContainer.isDirty = false
                extraPlayerDataCategoryRegistry.forEach { category ->
                    if (category.ioHandler != null) {
                        category.sync(player)
                    }
                }
            }
        }
    }

    // セーブ・ロードイベント登録
    ModEvents.onInitialize {
        object : CustomPlayerSave(MirageFairy2024.identifier("extra_player_data")) {
            override fun savePlayer(player: PlayerEntity) = player.extraPlayerDataContainer.toNbt()
            override fun loadPlayer(player: PlayerEntity, saveData: NbtCompound) = player.extraPlayerDataContainer.fromNbt(saveData)
        }
    }

}


// サーバーからクライアントへの同期

fun <T : Any> ExtraPlayerDataCategory<T>.sync(player: ServerPlayerEntity) {
    val value = player.extraPlayerDataContainer[this]
    ExtraPlayerDataSynchronizationChannel.sendToClient(player, ExtraPlayerDataSynchronizationPacket(this, value))
}

object ExtraPlayerDataSynchronizationChannel : Channel<ExtraPlayerDataSynchronizationPacket<*>>(MirageFairy2024.identifier("extra_player_data_synchronization")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: ExtraPlayerDataSynchronizationPacket<*>) {
        buf.writeUtf(extraPlayerDataCategoryRegistry.getKey(packet.category)!!.string)
        fun <T : Any> f(packet: ExtraPlayerDataSynchronizationPacket<T>) {
            buf.writeBoolean(packet.value != null)
            if (packet.value != null) buf.writeNbt(packet.category.ioHandler!!.toNbt(packet.value, buf.registryAccess()))
        }
        f(packet)
    }

    override fun readFromBuf(buf: RegistryFriendlyByteBuf): ExtraPlayerDataSynchronizationPacket<*> {
        val identifier = buf.readUtf().toIdentifier()
        val category = extraPlayerDataCategoryRegistry[identifier]!!
        fun <T : Any> f(category: ExtraPlayerDataCategory<T>): ExtraPlayerDataSynchronizationPacket<T> {
            val hasValue = buf.readBoolean()
            val value = if (hasValue) category.ioHandler!!.fromNbt(buf.readNbt()!!, buf.registryAccess()) else null
            return ExtraPlayerDataSynchronizationPacket(category, value)
        }
        return f(category)
    }
}

class ExtraPlayerDataSynchronizationPacket<T : Any>(val category: ExtraPlayerDataCategory<T>, val value: T?)


// Mixin Impl

class ExtraPlayerDataContainer(private val player: PlayerEntity) {
    private val map = mutableMapOf<ResourceLocation, Any>()

    /**
     * このコンテナに格納されているオブジェクトを取得します。
     * このコンテナにオブジェクトが格納されていない場合、nullを返します。
     */
    operator fun <T : Any> get(loader: ExtraPlayerDataCategory<T>): T? {
        val value = map[extraPlayerDataCategoryRegistry.getKey(loader)!!] ?: return null
        return loader.castOrThrow(value)
    }

    fun <T : Any> getOrInit(loader: ExtraPlayerDataCategory<T>): T {
        val value = map[extraPlayerDataCategoryRegistry.getKey(loader)!!]
        return if (value != null) {
            loader.castOrThrow(value)
        } else {
            val newValue = loader.create()
            map[extraPlayerDataCategoryRegistry.getKey(loader)!!] = newValue
            newValue
        }
    }

    val entries: Set<Map.Entry<ResourceLocation, Any>> get() = map.entries

    /**
     * このコンテナにオブジェクトを代入します。
     * [data]がnullだった場合、このコンテナに格納されているオブジェクトを削除します。
     */
    operator fun <T : Any> set(loader: ExtraPlayerDataCategory<T>, data: T?) {
        if (data == null) {
            map.remove(extraPlayerDataCategoryRegistry.getKey(loader)!!)
        } else {
            map[extraPlayerDataCategoryRegistry.getKey(loader)!!] = data
        }
    }

    /**
     * 登録されているすべての拡張プレイヤーデータカテゴリを書き込みます。
     * このコンテナにオブジェクトが格納されていない場合、データの書き込みはキャンセルされます。
     */
    fun toNbt(): NbtCompound {
        val registries: HolderLookup.Provider = player.level().registryAccess()

        val nbt = NbtCompound()
        extraPlayerDataCategoryRegistry.entrySet().forEach { (key, loader) ->
            fun <T : Any> f(loader: ExtraPlayerDataCategory<T>) {
                val ioHandler = loader.ioHandler ?: return
                val value = map[key.location()] ?: return
                val data = try {
                    loader.castOrThrow(value)
                } catch (e: ClassCastException) {
                    getLogger(ExtraPlayerDataContainer::class.java).error("Failed to load: ${value.javaClass} as ${key.location()} for ${player.name}(${player.uuid})", e)
                    return
                }
                nbt.wrapper[key.location().string].compound.set(ioHandler.toNbt(data, registries))
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
        val registries: HolderLookup.Provider = player.level().registryAccess()

        map.clear()
        extraPlayerDataCategoryRegistry.entrySet().forEach { (key, loader) ->
            fun <T : Any> f(loader: ExtraPlayerDataCategory<T>) {
                val ioHandler = loader.ioHandler ?: return
                val data = nbt.wrapper[key.location().string].compound.get() ?: return
                map[key.location()] = ioHandler.fromNbt(data, registries)
            }
            f(loader)
        }
    }

    var isDirty = true
}

interface ExtraPlayerDataContainerGetter {
    @Suppress("FunctionName")
    fun `mirageFairy2024$getExtraPlayerDataContainer`(): ExtraPlayerDataContainer
}
