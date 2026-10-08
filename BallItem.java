package com.exemplo.futebolmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BallItem extends Item {
	public BallItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext ctx) {
		World world = ctx.getWorld();
		if (world.isClient) return ActionResult.SUCCESS;

		BlockPos pos = ctx.getBlockPos().offset(ctx.getSide());
		BallEntity ball = ModEntities.BALL.create(world);
		if (ball == null) return ActionResult.FAIL;

		ball.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
		world.spawnEntity(ball);

		PlayerEntity player = ctx.getPlayer();
		if (player == null || !player.isCreative()) ctx.getStack().decrement(1);
		return ActionResult.CONSUME;
	}
}
