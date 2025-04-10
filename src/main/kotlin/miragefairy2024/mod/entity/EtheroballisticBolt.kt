package miragefairy2024.mod.entity

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.MaterialCard
import miragefairy2024.mod.SoundEventCard
import miragefairy2024.mod.tool.PhysicalMagicDamageTypeCard
import miragefairy2024.util.getValue
import miragefairy2024.util.isServer
import miragefairy2024.util.register
import miragefairy2024.util.registerEntityTypeTagGeneration
import miragefairy2024.util.setValue
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.EntityTypeTags
import net.minecraft.sound.SoundCategory
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

object EtheroballisticBoltCard {
    val spawnGroup = SpawnGroup.MISC
    val width = 0.5F
    val height = 0.5F
    fun createEntity(entityType: EntityType<EtheroballisticBoltEntity>, world: World) = EtheroballisticBoltEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("etheroballistic_bolt")
    val entityType: EntityType<EtheroballisticBoltEntity> = FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
        .dimensions(EntityDimensions.fixed(width, height))
        .build()

    context(ModContext)
    fun init() {
        entityType.register(Registries.ENTITY_TYPE, identifier)
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.IMPACT_PROJECTILES }
    }
}

class EtheroballisticBoltEntity(entityType: EntityType<out EtheroballisticBoltEntity>, world: World) : ProjectileEntity(entityType, world) {
    companion object {
        val DAMAGE: TrackedData<Float> = DataTracker.registerData(EtheroballisticBoltEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
        val MAX_DISTANCE: TrackedData<Float> = DataTracker.registerData(EtheroballisticBoltEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }


    var damage by DAMAGE
    var maxDistance by MAX_DISTANCE

    override fun initDataTracker() {
        dataTracker.startTracking(DAMAGE, 0F)
        dataTracker.startTracking(MAX_DISTANCE, 0F)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        nbt.putFloat("Damage", damage)
        nbt.putFloat("MaxDistance", maxDistance)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        damage = nbt.getFloat("Damage")
        maxDistance = nbt.getFloat("MaxDistance")
    }


    private var prevPos: Vec3d? = null
    override fun tick() {
        super.tick()

        // 距離判定
        val movingDistance = velocity.length().toFloat()
        val stopped = if (movingDistance >= maxDistance) { // 10 >= 1 → velocity *= 0.1( = 1 / 10)
            if (movingDistance >= 0.001F) velocity = velocity.multiply((maxDistance / movingDistance).toDouble())
            true
        } else {
            false
        }
        maxDistance -= movingDistance

        // 衝突判定
        val hitResult = ProjectileUtil.getCollision(this) { (owner == null || it.type != owner!!.type) && it.canHit() }
        if (hitResult.type != HitResult.Type.MISS) onCollision(hitResult)
        if (isRemoved) return

        checkBlockCollision()
        if (isRemoved) return

        val vec3d = velocity
        val nextX = x + vec3d.x
        val nextY = y + vec3d.y
        val nextZ = z + vec3d.z

        updateRotation()

        // 距離判定
        if (stopped) {
            discard()
            return
        }

        // 位置更新
        prevPos = pos
        setPosition(nextX, nextY, nextZ)

        // 向き更新
        if (prevPitch == 0F && prevYaw == 0F) {
            yaw = (MathHelper.atan2(vec3d.x, vec3d.z) * 180F / MathHelper.PI).toFloat()
            pitch = (MathHelper.atan2(vec3d.y, vec3d.horizontalLength()) * 180F / MathHelper.PI).toFloat()
            prevYaw = yaw
            prevPitch = pitch
        }

        // TODO 飛翔体パーティクル

    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (world.isServer) {
            world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
            discard()
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        entityHitResult.entity.damage(world.damageSources.create(PhysicalMagicDamageTypeCard.registryKey, this, owner as? LivingEntity), damage)
        if (world.random.nextInt(5) == 0) dropItem(MaterialCard.ETHEROBALLISTIC_BOLT_FRAGMENT.item)
    }

    override fun handleStatus(status: Byte) {
        super.handleStatus(status)
        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {
            world.playSound(x, y, z, SoundEventCard.ENTITY_ETHEROBALLISTIC_BOLT_HIT.soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.90F + (world.random.nextFloat() - 0.5F) * 0.3F, true)
            // TODO パーティクル
        }
    }

}
