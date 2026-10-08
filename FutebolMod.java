package com.exemplo.futebolmod;

import net.fabricmc.api.ModInitializer;

public class FutebolMod implements ModInitializer {
	public static final String MOD_ID = "futebolmod";

	@Override
	public void onInitialize() {
		ModBlocks.register();
		ModEntities.register();
		ModItems.register();
	}
}
