package com.exemplo.futebolmod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;

public class FutebolModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModEntities.BALL, BallEntityRenderer::new);
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.GOAL_NET, RenderLayer.getCutout());
	}
}
