package miragefairy2024.mod

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.tool.MagicDamageTypeCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.ItemLootPoolEntry
import miragefairy2024.util.LootPool
import miragefairy2024.util.LootTable
import miragefairy2024.util.Model
import miragefairy2024.util.configure
import miragefairy2024.util.enJa
import miragefairy2024.util.getValue
import miragefairy2024.util.isServer
import miragefairy2024.util.register
import miragefairy2024.util.registerEntityTypeTagGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.setValue
import miragefairy2024.util.times
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityStatuses
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.ai.goal.MeleeAttackGoal
import net.minecraft.entity.ai.goal.RevengeGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.item.Item
import net.minecraft.item.SpawnEggItem
import net.minecraft.loot.condition.KilledByPlayerLootCondition
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.EntityTypeTags
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import org.joml.Quaternionf

context(ModContext)
fun initEntityModule() {
    AntimatterBoltCard.init()
    ChaosCubeCard.init()
}

object AntimatterBoltCard {
    val spawnGroup = SpawnGroup.MISC
    val width = 0.5F
    val height = 0.5F
    fun createEntity(entityType: EntityType<AntimatterBoltEntity>, world: World) = AntimatterBoltEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("antimatter_bolt")
    val entityType: EntityType<AntimatterBoltEntity> = FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
        .dimensions(EntityDimensions.fixed(width, height))
        .build()

    context(ModContext)
    fun init() {
        entityType.register(Registries.ENTITY_TYPE, identifier)
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.IMPACT_PROJECTILES }
    }
}

class AntimatterBoltEntity(entityType: EntityType<out AntimatterBoltEntity>, world: World) : ProjectileEntity(entityType, world) {
    companion object {
        val DAMAGE: TrackedData<Float> = DataTracker.registerData(AntimatterBoltEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
        val MAX_DISTANCE: TrackedData<Float> = DataTracker.registerData(AntimatterBoltEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
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
        if (world.isServer) {
            world.sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES)
            discard()
        }
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        attack(entityHitResult.entity)
    }

    private fun attack(entity: Entity) {
        entity.damage(world.damageSources.create(MagicDamageTypeCard.registryKey, this, owner as? LivingEntity), damage)
    }

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

object ChaosCubeCard {
    val spawnGroup = SpawnGroup.MONSTER
    val width = 0.8F
    val height = 1.5F
    fun createEntity(entityType: EntityType<ChaosCubeEntity>, world: World) = ChaosCubeEntity(entityType, world)
    val identifier = MirageFairy2024.identifier("chaos_cube")
    val name = EnJa("Chaos Cube", "混沌のキューブ")
    val entityType: EntityType<ChaosCubeEntity> = FabricEntityTypeBuilder.create(spawnGroup) { entityType, world -> createEntity(entityType, world) }
        .dimensions(EntityDimensions.fixed(width, height))
        .build()
    val spawnEggItem = SpawnEggItem(entityType, 0xB36235, 0xFFC21D, Item.Settings())

    context(ModContext)
    fun init() {
        entityType.register(Registries.ENTITY_TYPE, identifier)
        val attributes = HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0)
        FabricDefaultAttributeRegistry.register(entityType, attributes)
        entityType.enJa(name)
        entityType.registerEntityTypeTagGeneration { EntityTypeTags.FALL_DAMAGE_IMMUNE }
        entityType.registerLootTableGeneration {
            LootTable(
                LootPool(ItemLootPoolEntry(MaterialCard.CHAOS_STONE.item)).configure {
                    conditionally(KilledByPlayerLootCondition.builder())
                    conditionally(RandomChanceWithLootingLootCondition.builder(0.3F, 0.1F))
                },
            )
        }

        spawnEggItem.register(Registries.ITEM, identifier * "_egg")
        spawnEggItem.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)
        spawnEggItem.registerModelGeneration(Model(Identifier("minecraft", "item/template_spawn_egg")))
        spawnEggItem.enJa(EnJa("${name.en} Spawn Egg", "${name.ja}のスポーンエッグ"))
    }
}

class ChaosCubeEntity(entityType: EntityType<out ChaosCubeEntity>, world: World) : HostileEntity(entityType, world) {

    class Segment(val partIndex: Int) {
        val prevRotation = Quaternionf()
        val rotation = Quaternionf()
    }

    private var isFirstClientTick = true
    val segments = (0 until 8).map { Segment(it) }.toTypedArray()

    private val animator = sequence {

        while (true) {
            fun swap(a: Int, b: Int, c: Int, d: Int) {
                val tmp1 = segments[a]
                segments[a] = segments[b]
                segments[b] = segments[c]
                segments[c] = segments[d]
                segments[d] = tmp1
            }

            val duration = 40

            // ++++ ----    vv   vv       <==>
            // 0123 4567  0132 4576  0132 6754
            // 0145 2367  0154 2376  0154 6732
            // 0246 1357  0264 1375  0264 5731

            repeat(duration) {
                segments[0].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalX(90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalX(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 3, 2)
            swap(6, 7, 5, 4)

            repeat(duration) {
                segments[0].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalY(90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalY(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 1, 5, 4)
            swap(6, 7, 3, 2)

            repeat(duration) {
                segments[0].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[1].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[2].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[3].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[4].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[5].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                segments[6].rotation.rotateLocalZ(90F / duration / 180F * MathHelper.PI)
                segments[7].rotation.rotateLocalZ(-90F / duration / 180F * MathHelper.PI)
                yield(Unit)
            }
            swap(0, 2, 6, 4)
            swap(5, 7, 3, 1)

        }

    }.iterator()


    init {
        experiencePoints = 10
    }

    override fun initGoals() {
        goalSelector.add(4, MeleeAttackGoal(this, 1.0, false))
        goalSelector.add(7, WanderAroundFarGoal(this, 0.5, 0.0F))
        targetSelector.add(1, RevengeGoal(this).setGroupRevenge())
        targetSelector.add(2, ActiveTargetGoal(this, PlayerEntity::class.java, true))
    }


    override fun tickMovement() {
        super.tickMovement()

        if (!isOnGround && velocity.y < 0.0) velocity = velocity.multiply(1.0, 0.6, 1.0)

        if (world.isClient) {

            if (isFirstClientTick) {
                isFirstClientTick = false
                animator.next()
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
            } else {
                segments.forEach {
                    it.prevRotation.x = it.rotation.x
                    it.prevRotation.y = it.rotation.y
                    it.prevRotation.z = it.rotation.z
                    it.prevRotation.w = it.rotation.w
                }
                animator.next()
            }

            if (world.random.nextInt(10) == 0) {
                world.addParticle(
                    ParticleTypeCard.CHAOS_STONE.particleType,
                    x + width / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    y + 1.0,
                    z + width / 2.0 * (2.0 * random.nextDouble() - 1.0) * 0.8,
                    0.0,
                    -0.05,
                    0.0,
                )
            }

        }

    }

    override fun mobTick() {

        val livingEntity = target
        if (livingEntity != null && livingEntity.eyeY > eyeY && canTarget(livingEntity)) {
            val vec3d = velocity
            velocity = velocity.add(0.0, (0.3 - vec3d.y) * 0.3, 0.0)
            velocityDirty = true
        }

        super.mobTick()
    }

}
