package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.register
import miragefairy2024.util.registerEntityTypeTagGeneration
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.EntityTypeTags
import net.minecraft.sound.SoundCategory
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

context(ModContext)
fun initEntityModule() {
    AntimatterBoltCard.let { card ->
        card.entityType.register(Registries.ENTITY_TYPE, card.identifier)
        card.init()
    }
}

object AntimatterBoltCard {
    val spawnGroup = SpawnGroup.MISC
    val width = 0.5F
    val height = 0.5F
    fun createEntity(entityType: EntityType<AntimatterBoltEntity>, world: World) = AntimatterBoltEntity(entityType, world, 0F, 0.0)
    val identifier = MirageFairy2024.identifier("antimatter_bolt")
    val entityType = FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
        .dimensions(EntityDimensions.fixed(width, height))
        .build()

    context(ModContext)
    fun init() {
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.IMPACT_PROJECTILES }
    }
}

class AntimatterBoltEntity(entityType: EntityType<out AntimatterBoltEntity>, world: World, private val damage: Float, private var limitDistance: Double) : ProjectileEntity(entityType, world) {
    private var prevPos: Vec3d? = null
    override fun tick() {
        super.tick()

        // 距離判定
        val movingDistance = velocity.length()
        val stopped = if (movingDistance >= limitDistance) { // 10 >= 1 → velocity *= 0.1( = 1 / 10)
            if (movingDistance >= 0.001) velocity = velocity.multiply(limitDistance / movingDistance)
            true
        } else {
            false
        }
        limitDistance -= movingDistance

        // 衝突判定
        val hitResult = ProjectileUtil.getCollision(this) { canHit(it) }
        if (hitResult.type != HitResult.Type.MISS) onCollision(hitResult)
        if (isRemoved) return

        checkBlockCollision()
        if (isRemoved) return

        val vec3d = velocity
        val nextX = x + vec3d.x
        val nextY = y + vec3d.y
        val nextZ = z + vec3d.z

        updateRotation()

        // 水に触れると消滅
        if (isInsideWaterOrBubbleColumn) {
            discard()
            return
        }

        // パーティクル
        prevPos?.let { prevPos ->
            repeat(2) { i ->
                world.addParticle(
                    ParticleTypeCard.AURA.particleType,
                    prevPos.x + (x - prevPos.x) * (i / 3.0),
                    prevPos.y + (y - prevPos.y) * (i / 3.0),
                    prevPos.z + (z - prevPos.z) * (i / 3.0),
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                    (random.nextDouble() - 0.5) * 0.02,
                )
            }
        }

        // 距離判定
        if (stopped) {
            discard()
            return
        }

        // 位置更新
        prevPos = pos
        setPosition(nextX, nextY, nextZ)

    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        if (!world.isClient) {
            world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
            discard()
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        attack(entityHitResult.entity)
    }

    private fun attack(entity: Entity) {
        entity.damage(world.damageSources.mobProjectile(this, owner as? LivingEntity), damage)
    }

    override fun initDataTracker() = Unit

    override fun handleStatus(status: Byte) {
        super.handleStatus(status)
        if (status == EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES) {

            world.playSound(x, y, z, SoundEventCard.MAGIC_HIT.soundEvent, SoundCategory.NEUTRAL, 0.5F, 0.90F + (world.random.nextFloat() - 0.5F) * 0.3F, true)

            for (i in 0..7) {
                world.addParticle(
                    ParticleTypeCard.AURA.particleType,
                    x, y, z,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                    (random.nextDouble() - 0.5) * 0.3,
                )
            }
        }

    }
}