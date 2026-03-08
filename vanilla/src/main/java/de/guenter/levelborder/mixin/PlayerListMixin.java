package de.guenter.levelborder.mixin;

import de.guenter.levelborder.LevelBorderMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    
    private static final Map<UUID, ServerPlayer.RespawnConfig> clearedRespawnConfigs = new ConcurrentHashMap<>();

    @Inject(method = "sendLevelInfo(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/level/ServerLevel;)V", at = @At("RETURN"))
    private void onPlacePlayer(ServerPlayer player, ServerLevel world, CallbackInfo ci) {
        try {
            if (LevelBorderMod.levelBorderHandler == null) return;

            boolean isNether = world.dimension() == Level.NETHER;
            LevelBorderMod.levelBorderHandler.initBorder(player, isNether);

            if (world.dimension() == Level.OVERWORLD) {
                if (!LevelBorderMod.levelBorderHandler.isWithinBorder(player)) {
                    final var spawn = LevelBorderMod.levelBorderHandler.getRespawnPos();
                    final var overworld = player.level().getServer().overworld();
                    if (overworld != null) {
                        player.teleportTo(overworld, spawn.x() + 0.5d, (double) spawn.y(), spawn.z() + 0.5d,
                                java.util.Collections.emptySet(), player.getYRot(), player.getXRot(), false);
                    }
                }
            }
        } catch (Throwable t) {

        }
    }

    @Inject(method = "respawn", at = @At("HEAD"), require = 0)
    private void beforeRespawn(ServerPlayer player, boolean alive, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> ci) {
        try {
            if (LevelBorderMod.levelBorderHandler == null) return;
            if (alive) return;

            var config = player.getRespawnConfig();
            if (config == null) return;

            var respawnDim = config.respawnData().dimension();
            if (respawnDim == Level.OVERWORLD) return;
            
            LevelBorderMod.levelBorderHandler.initBorder(player, respawnDim == Level.NETHER);
            
            var anchorPos = config.respawnData().pos();
            double oldX = player.getX();
            double oldY = player.getY();
            double oldZ = player.getZ();
            player.setPosRaw(anchorPos.getX() + 0.5d, anchorPos.getY(), anchorPos.getZ() + 0.5d);

            boolean anchorOutsideBorder = !LevelBorderMod.levelBorderHandler.isWithinBorder(player);

            player.setPosRaw(oldX, oldY, oldZ);

            if (anchorOutsideBorder) {
                clearedRespawnConfigs.put(player.getUUID(), config);
                player.setRespawnPosition(null, false);
            }
        } catch (Throwable t) {

        }
    }

    @Inject(method = "respawn", at = @At("RETURN"), require = 0)
    private void onRespawnWithReason(ServerPlayer player, boolean alive, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        final ServerPlayer newPlayer = cir.getReturnValue();
        if (newPlayer == null) return;

        try {
            if (LevelBorderMod.levelBorderHandler == null) return;

            final boolean inNether = newPlayer.level().dimension() == Level.NETHER;
            LevelBorderMod.levelBorderHandler.initBorder(newPlayer, inNether);

            ServerPlayer.RespawnConfig savedConfig = clearedRespawnConfigs.remove(newPlayer.getUUID());
            if (savedConfig != null) {
                newPlayer.setRespawnPosition(savedConfig, false);
                if (newPlayer.getRespawnConfig() != null) {
                    newPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "Spawn point outside border! Relocated to safe zone."));
                }
            }

            if (savedConfig == null && !alive && newPlayer.level().dimension() == Level.OVERWORLD) {
                if (!LevelBorderMod.levelBorderHandler.isWithinBorder(newPlayer)) {
                    boolean hadCustomRespawn = newPlayer.getRespawnConfig() != null;
                    if (hadCustomRespawn) {
                        newPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "Spawn point outside border! Relocated to safe zone."));
                    }
                }
            }
        } catch (Throwable t) {

        }
    }
}