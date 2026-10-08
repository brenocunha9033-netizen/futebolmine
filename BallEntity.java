package com.exemplo.futebolmod;

import net.minecraft.block.BlockState;
import net.minecraft.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BallEntity extends Entity {
	private static final double GRAVITY = 0.04;

	private Vec3d spawnPoint;
	private String lastKicker;
	private int goalCooldown;

	// só usado no cliente (rotação visual da bola rolando)
	public final Quaternionf orientation = new Quaternionf();
	private double lastX, lastZ;
	private boolean clientInit;

	public BallEntity(EntityType<? extends BallEntity> type, World world) {
		super(type, world);
	}

	@Override protected void initDataTracker(DataTracker.Builder builder) {}
	@Override protected void readCustomDataFromNbt(NbtCompound nbt) {}
	@Override protected void writeCustomDataToNbt(NbtCompound nbt) {}

	@Override public boolean canHit() { return true; }

	// ---------- Interações ----------

	/** Clique esquerdo = chute forte (depende da recarga do ataque e de correr). */
	@Override
	public boolean damage(DamageSource source, float amount) {
		if (!getWorld().isClient && source.getAttacker() instanceof PlayerEntity player) {
			double power = player.isSprinting() ? 1.35 : 1.0;
			power *= 0.5 + 0.5 * player.getAttackCooldownProgress(0.5f);
			kick(player, power, 0.18);
		}
		return true;
	}

	/** Clique direito = passe curto. Agachado + clique direito = pega a bola de volta. */
	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		if (!getWorld().isClient) {
			if (player.isSneaking()) {
				player.getInventory().offerOrDrop(new ItemStack(ModItems.SOCCER_BALL));
				discard();
			} else {
				kick(player, 0.45, 0.08);
			}
		}
		return ActionResult.success(getWorld().isClient);
	}

	private void kick(PlayerEntity player, double power, double lift) {
		Vec3d look = player.getRotationVec(1.0f);
		Vec3d flat = new Vec3d(look.x, 0, look.z).normalize();
		setVelocity(flat.x * power, lift + Math.max(0, look.y) * power * 0.7, flat.z * power);
		lastKicker = player.getName().getString();
		getWorld().playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_SLIME_BLOCK_HIT,
				SoundCategory.PLAYERS, 0.8f, 1.2f);
	}

	// ---------- Física ----------

	@Override
	public void tick() {
		super.tick();
		World world = getWorld();
		if (world.isClient) {
			tickClientRoll();
			return;
		}
		if (spawnPoint == null) spawnPoint = getPos();
		if (goalCooldown > 0) goalCooldown--;

		// Condução: encostar na bola empurra ela (correndo empurra mais)
		for (PlayerEntity p : world.getEntitiesByClass(PlayerEntity.class,
				getBoundingBox().expand(0.12), pl -> !pl.isSpectator())) {
			Vec3d away = getPos().subtract(p.getPos());
			Vec3d dir = new Vec3d(away.x, 0, away.z).normalize();
			double speed = p.isSprinting() ? 0.45 : 0.22;
			setVelocity(dir.x * speed, Math.max(getVelocity().y, 0.0), dir.z * speed);
			lastKicker = p.getName().getString();
		}

		Vec3d v = getVelocity().add(0, -GRAVITY, 0);
		move(MovementType.SELF, v);
		Vec3d after = getVelocity();
		double nx = after.x, ny = after.y, nz = after.z;

		if (verticalCollision) {
			if (v.y < -0.12) {
				ny = -v.y * 0.6; // quica
				world.playSound(null, getX(), getY(), getZ(), SoundEvents.BLOCK_SLIME_BLOCK_HIT,
						SoundCategory.NEUTRAL, Math.min(1f, (float) -v.y), 0.9f);
			} else {
				ny = 0;
			}
		}
		if (horizontalCollision) {
			if (Math.abs(after.x) < 1e-5 && Math.abs(v.x) > 0.03) nx = -v.x * 0.7;
			if (Math.abs(after.z) < 1e-5 && Math.abs(v.z) > 0.03) nz = -v.z * 0.7;
		}

		double drag = isOnGround() ? 0.96 : 0.995;
		if (isTouchingWater()) {
			drag = 0.9;
			ny = ny * 0.8 + 0.05; // boia
		}
		nx *= drag;
		nz *= drag;
		if (Math.abs(nx) < 0.004) nx = 0;
		if (Math.abs(nz) < 0.004) nz = 0;
		setVelocity(nx, ny, nz);

		// Caiu no void
		if (getY() < world.getBottomY() - 10) {
			resetToSpawn();
			return;
		}

		// Gol?
		if (goalCooldown == 0 && world instanceof ServerWorld sw) {
			BlockPos p1 = getBlockPos();
			BlockPos p2 = BlockPos.ofFloored(getX(), getY() + 0.2, getZ());
			BlockState s1 = world.getBlockState(p1);
			BlockState s2 = world.getBlockState(p2);
			if (s1.isOf(ModBlocks.GOAL_NET) || s2.isOf(ModBlocks.GOAL_NET)) {
				scoreGoal(sw);
			}
		}
	}

	private void scoreGoal(ServerWorld sw) {
		String who = lastKicker == null ? "???" : lastKicker;
		for (ServerPlayerEntity sp : sw.getPlayers(p -> p.squaredDistanceTo(this) < 64 * 64)) {
			sp.networkHandler.sendPacket(new TitleFadeS2CPacket(5, 50, 10));
			sp.networkHandler.sendPacket(new SubtitleS2CPacket(
					Text.literal("Gol de " + who).formatted(Formatting.YELLOW)));
			sp.networkHandler.sendPacket(new TitleS2CPacket(
					Text.literal("GOOOL!").formatted(Formatting.GOLD, Formatting.BOLD)));
		}
		sw.spawnParticles(ParticleTypes.FIREWORK, getX(), getY() + 0.5, getZ(), 60, 0.6, 0.6, 0.6, 0.12);
		sw.playSound(null, getBlockPos(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
				SoundCategory.PLAYERS, 1f, 1f);
		resetToSpawn();
		goalCooldown = 40;
	}

	private void resetToSpawn() {
		Vec3d s = spawnPoint != null ? spawnPoint : getPos();
		refreshPositionAndAngles(s.x, s.y, s.z, 0, 0);
		setVelocity(Vec3d.ZERO);
	}

	private void tickClientRoll() {
		if (!clientInit) {
			lastX = getX();
			lastZ = getZ();
			clientInit = true;
		}
		double dx = getX() - lastX, dz = getZ() - lastZ;
		double dist = Math.sqrt(dx * dx + dz * dz);
		if (dist > 1e-4) {
			Vector3f axis = new Vector3f((float) dz, 0, (float) -dx).normalize();
			orientation.premul(new Quaternionf().rotateAxis((float) Math.toRadians(dist * 200), axis));
			orientation.normalize();
		}
		lastX = getX();
		lastZ = getZ();
	}
}
