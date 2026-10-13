package com.bobroflza.tieredarcana.registry;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.entity.MagicMissileEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TieredArcana.MODID);

    public static final RegistryObject<EntityType<MagicMissileEntity>> MAGIC_MISSILE = ENTITIES.register("magic_missile",
            () -> EntityType.Builder.<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10).build("magic_missile"));

    private ModEntities() {}
}
