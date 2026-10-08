package com.exemplo.futebolmod;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class BallEntityRenderer extends EntityRenderer<BallEntity> {
	private final ItemRenderer itemRenderer;
	private final ItemStack stack = new ItemStack(ModItems.SOCCER_BALL);

	public BallEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
		this.itemRenderer = ctx.getItemRenderer();
		this.shadowRadius = 0.2f;
	}

	@Override
	public Identifier getTexture(BallEntity entity) {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}

	@Override
	public void render(BallEntity entity, float yaw, float tickDelta, MatrixStack matrices,
					   VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();
		matrices.translate(0, 0.2, 0);
		matrices.multiply(entity.orientation);
		itemRenderer.renderItem(stack, ModelTransformationMode.GROUND, light, OverlayTexture.DEFAULT_UV,
				matrices, vertexConsumers, entity.getWorld(), entity.getId());
		matrices.pop();
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
	}
}
