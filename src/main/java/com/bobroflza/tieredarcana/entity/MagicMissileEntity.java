package com.bobroflza.tieredarcana.entity;

import com.bobroflza.tieredarcana.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** A gravity-free bolt of arcane energy. Drawn entirely with particles. */
public class MagicMissileEntity extends ThrowableProjectile {
    private static final DustParticleOptions TRAIL = new DustParticleOptions(new Vector3f(0.8f, 0.45f, 1f), 1.0f);
    private float damage = 5f;

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level) {
        super(type, level);
    }

    public MagicMissileEntity(Level level, LivingEntity owner, float damage) {
        super(ModEntities.MAGIC_MISSILE.get(), owner, level);
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected float getGravity() {
        return 0f;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            Vec3 m = getDeltaMovement();
            for (int i = 0; i < 3; i++) {
                double f = i / 3.0;
                level().addParticle(TRAIL, getX() - m.x * f, getY() - m.y * f, getZ() - m.z * f, 0, 0, 0);
            }
            if (random.nextInt(3) == 0) level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
        } else if (tickCount > 80) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity owner = getOwner();
        result.getEntity().hurt(damageSources().indirectMagic(this, owner), damage);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.WITCH, getX(), getY(), getZ(), 12, 0.15, 0.15, 0.15, 0.05);
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("Damage");
    }
}
