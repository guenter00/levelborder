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

@Mixin(PlayerList.class)
public class PlayerListMixin {

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
                                player.getYRot(), player.getXRot(), false);
                    }
                }
            }
        } catch (Throwable t) {

        }
    }

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;ZLnet/minecraft/world/entity/Entity$RemovalReason;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"), require = 0)
    private void onRespawnWithReason(ServerPlayer player, boolean alive, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        final ServerPlayer newPlayer = cir.getReturnValue();
        if (newPlayer == null) return;

        try {
            if (LevelBorderMod.levelBorderHandler == null) return;

            newPlayer.level().getServer().execute(() -> {
                try {
                    final boolean inNether = newPlayer.level().dimension() == Level.NETHER;
                    LevelBorderMod.levelBorderHandler.initBorder(newPlayer, inNether);

                    if (!LevelBorderMod.levelBorderHandler.isWithinBorder(newPlayer)) {
                        boolean hadCustomRespawn = newPlayer.getRespawnPosition() != null;
                        final var pos = LevelBorderMod.levelBorderHandler.getRespawnPos();
                        final var overworld = newPlayer.level().getServer().overworld();
                        if (overworld != null) {
                            newPlayer.teleportTo(overworld, pos.x() + 0.5d, (double) pos.y(), pos.z() + 0.5d,
                                    newPlayer.getYRot(), newPlayer.getXRot(), false);

                            if (hadCustomRespawn) {
                                newPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                        "Spawn point outside border! Relocated to safe zone."));
                            }
                        }
                    }
                } catch (Throwable inner) {

                }
            });
        } catch (Throwable t) {

        }
    }
}