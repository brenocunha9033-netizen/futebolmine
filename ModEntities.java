package com.exemplo.futebolmod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
	public static final EntityType<BallEntity> BALL = Registry.register(
			Registries.ENTITY_TYPE,
			Identifier.of(FutebolMod.MOD_ID, "soccer_ball"),
			EntityType.Builder.<BallEntity>create(BallEntity::new, SpawnGroup.MISC)
					.dimensions(0.4f, 0.4f)
					.maxTrackingRange(10)
					.trackingTickInterval(1)
					.build("soccer_ball"));

	public static void register() {}
}
