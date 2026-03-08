package de.guenter.levelborder;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

import java.lang.reflect.Field;
import java.util.*;

public abstract class VanillaLevelBorderHandler extends LevelBorderHandler<ServerPlayer, WorldBorder, MinecraftServer> {

    private final Map<UUID, Double> previousBorderSizes = new HashMap<>();
    private final Map<UUID, Double> startSizes = new HashMap<>();
    private final Map<UUID, Long> animationEndTimes = new HashMap<>();
    private final Map<UUID, Queue<Double>> pendingAnimations = new HashMap<>();
    private static final long ANIMATION_DURATION_MS = 2000L;

    @Override
    public void setMode(BorderMode mode) {
        var data = getServer().overworld().getDataStorage().computeIfAbsent(BorderModeSavedData.TYPE);
        data.borderMode = mode;
        data.setDirty();
        super.setMode(mode);
    }

    @Override
    protected BorderMode getMode() {
        return getServer().overworld().getDataStorage().computeIfAbsent(BorderModeSavedData.TYPE).borderMode;
    }

    @Override
    protected WorldBorder createWorldBorder(ServerPlayer player) {
        return new WorldBorder();
    }

    @Override
    protected void initBorder(ServerPlayer player, WorldBorder border, double size) {
        Pos2d center = getBorderCenter(player);
        setCenter(border, center.x(), center.z());
        border.setSize(size);
        UUID id = player.getUUID();
        previousBorderSizes.put(id, size);
        animationEndTimes.remove(id);
        pendingAnimations.remove(id);
        player.connection.send(new ClientboundInitializeBorderPacket(border));
    }

    @Override
    protected void interpolateBorder(ServerPlayer player, WorldBorder border, double size, long time) {
        UUID id = player.getUUID();
        long now = System.currentTimeMillis();
        Long animEndTime = animationEndTimes.get(id);

        if (animEndTime != null && now < animEndTime) {
            pendingAnimations.computeIfAbsent(id, k -> new LinkedList<>()).add(size);
            border.setSize(size);
            return;
        }
        startAnimation(player, border, size, now);
    }

    private void startAnimation(ServerPlayer player, WorldBorder border, double targetSize, long now) {
        UUID id = player.getUUID();
        double fromSize = previousBorderSizes.getOrDefault(id, border.getSize());
        previousBorderSizes.put(id, targetSize);
        startSizes.put(id, fromSize);
        animationEndTimes.put(id, now + ANIMATION_DURATION_MS);
        sendLerpPacket(player, fromSize, targetSize, 40L);
    }

    private void sendLerpPacket(ServerPlayer player, double fromSize, double toSize, long durationTicks) {
        try {
            
            WorldBorder dummy = new WorldBorder();
            dummy.setSize(fromSize);
            ClientboundSetBorderLerpSizePacket packet = new ClientboundSetBorderLerpSizePacket(dummy);

            int doubleCount = 0;
            for (Field f : ClientboundSetBorderLerpSizePacket.class.getDeclaredFields()) {
                f.setAccessible(true);
                if (f.getType() == double.class) {
                    f.setDouble(packet, doubleCount++ == 0 ? fromSize : toSize);
                } else if (f.getType() == long.class) {
                    f.setLong(packet, durationTicks);
                }
            }
            player.connection.send(packet);
        } catch (Exception ex) {
            
            WorldBorder temp = new WorldBorder();
            temp.setSize(toSize);
            player.connection.send(new ClientboundSetBorderSizePacket(temp));
        }
    }

    @Override
    protected void tickBorder(ServerPlayer player, WorldBorder border) {
        border.tick();
        UUID id = player.getUUID();
        Long endTime = animationEndTimes.get(id);
        long now = System.currentTimeMillis();
        boolean justFinished = false;

        if (endTime != null) {
            if (now < endTime) {
                double start = startSizes.getOrDefault(id, border.getSize());
                double end = previousBorderSizes.getOrDefault(id, border.getSize());
                double progress = Mth.clamp((double) (ANIMATION_DURATION_MS - (endTime - now)) / ANIMATION_DURATION_MS, 0.0, 1.0);
                border.setSize(start + (end - start) * progress);
                return;
            } else {
                double end = previousBorderSizes.getOrDefault(id, border.getSize());
                if (border.getSize() != end) border.setSize(end);
                justFinished = true;
            }
        }

        Queue<Double> queue = pendingAnimations.get(id);
        if (queue != null && !queue.isEmpty()) {
            Double targetSize = null;
            while (!queue.isEmpty()) targetSize = queue.poll();
            if (targetSize != null) startAnimation(player, border, targetSize, now);
        } else if (justFinished) {
            animationEndTimes.remove(id);
            startSizes.remove(id);
        }
    }

    @Override
    protected void onPlayerLeave(ServerPlayer player) {
        UUID id = player.getUUID();
        previousBorderSizes.remove(id);
        startSizes.remove(id);
        animationEndTimes.remove(id);
        pendingAnimations.remove(id);
    }

    @Override
    protected void setCenter(WorldBorder border, double centerX, double centerZ) {
        border.setCenter(centerX, centerZ);
    }

    @Override
    protected Collection<ServerPlayer> getPlayers() {
        return getServer().getPlayerList().getPlayers();
    }

    @Override
    protected double getDistance(ServerPlayer player, WorldBorder border) {
        double dist = border.getDistanceToBorder(player);
        if (player.level().dimension() == Level.NETHER && dist < 0.0d && dist > -0.3d) {
            return 0.0d;
        }
        return dist;
    }
    
    @Override
    protected Pos3i sharedOverworldSpawn() {
        ServerLevel overworld = getServer().overworld();
        BlockPos pos = overworld.getLevelData().getRespawnData().pos();
        return new Pos3i(pos.getX(), pos.getY(), pos.getZ());
    }
    
    private Pos2d getBorderCenter(ServerPlayer player) {
        Pos3i spawn = sharedOverworldSpawn();
        var dim = player.level().dimension();

        if (dim == Level.NETHER) {

            return new Pos2d(spawn.x() / 8.0d + 0.5d, spawn.z() / 8.0d + 0.5d);
        } else if (dim == Level.END) {
            
            return new Pos2d(0.5d, 0.5d);
        }
        
        return new Pos2d(spawn.x() + 0.5d, spawn.z() + 0.5d);
    }

    @Override
    public Pos3i getRespawnPos() {
        ServerLevel overworld = getServer().overworld();
        Pos3i spawn = sharedOverworldSpawn();

        double centerX = spawn.x() + 0.5d;
        double centerZ = spawn.z() + 0.5d;

        double borderSize = getCurrentBorderSize();
        double halfSize = borderSize / 2.0d;

        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;

        int blockMinX = (int) Math.floor(minX);
        int blockMaxX = (int) Math.floor(maxX);
        int blockMinZ = (int) Math.floor(minZ);
        int blockMaxZ = (int) Math.floor(maxZ);

        List<BlockPos> validBlocks = new ArrayList<>();

        for (int blockX = blockMinX; blockX <= blockMaxX; blockX++) {
            for (int blockZ = blockMinZ; blockZ <= blockMaxZ; blockZ++) {

                double playerX = blockX + 0.5d;
                double playerZ = blockZ + 0.5d;

                if (playerX < minX || playerX > maxX || playerZ < minZ || playerZ > maxZ) {
                    continue;
                }

                if (isSafeSpawnBlock(overworld, blockX, blockZ)) {
                    int y = overworld.getHeight(
                        net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        blockX, blockZ);
                    validBlocks.add(new BlockPos(blockX, y, blockZ));
                }
            }
        }

        if (!validBlocks.isEmpty()) {

            Random random = new Random();
            BlockPos selected = validBlocks.get(random.nextInt(validBlocks.size()));
            return new Pos3i(selected.getX(), selected.getY(), selected.getZ());
        }

        int fallbackY = overworld.getHeight(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            spawn.x(), spawn.z());
        return new Pos3i(spawn.x(), fallbackY, spawn.z());
    }

    private boolean isSafeSpawnBlock(ServerLevel level, int x, int z) {
        int surfaceY = level.getHeight(
            net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        BlockPos groundPos = new BlockPos(x, surfaceY - 1, z);
        BlockPos feetPos = new BlockPos(x, surfaceY, z);
        BlockPos headPos = new BlockPos(x, surfaceY + 1, z);

        var groundState = level.getBlockState(groundPos);
        var feetState = level.getBlockState(feetPos);
        var headState = level.getBlockState(headPos);

        boolean groundSolid = !groundState.getCollisionShape(level, groundPos).isEmpty();
        boolean groundNotFluid = groundState.getFluidState().isEmpty();
        boolean feetPassable = feetState.getCollisionShape(level, feetPos).isEmpty();
        boolean headPassable = headState.getCollisionShape(level, headPos).isEmpty();

        return groundSolid && groundNotFluid && feetPassable && headPassable;
    }

    private double getCurrentBorderSize() {
        BorderMode mode = getMode();
        if (mode == BorderMode.SUM) {
            int sum = getPlayers().stream().map(this::getExperienceLevel).reduce(0, Integer::sum);
            return Math.max(sum * 2.0D, 1.0D);
        }
        return getPlayers().stream()
            .mapToDouble(p -> Math.max(getExperienceLevel(p) * 2.0D, 1.0D))
            .max()
            .orElse(1.0D);
    }

    @Override protected int getTotalExperience(ServerPlayer player) { return player.totalExperience; }
    @Override protected int getExperienceLevel(ServerPlayer player) { return player.experienceLevel; }
    @Override protected UUID getUUID(ServerPlayer player) { return player.getUUID(); }
    @Override protected void hurt(ServerPlayer player, float damage) { player.hurt(player.damageSources().inWall(), damage); }

    @Override
    protected void copyExperience(ServerPlayer player, ServerPlayer other) {
        player.totalExperience = other.totalExperience;
        player.experienceProgress = other.experienceProgress;
        player.experienceLevel = other.experienceLevel;
    }
}