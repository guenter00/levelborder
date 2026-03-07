package de.guenter.levelborder.mixin;

import de.guenter.levelborder.LevelBorderMod;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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
                    final var overworld = player.getLevel().getServer().overworld();
                    if (overworld != null) {
                        player.teleportTo(overworld, spawn.x() + 0.5d, (double) spawn.y(), spawn.z() + 0.5d,
                                player.getYRot(), player.getXRot());
                    }
                }
            }
        } catch (Throwable t) {

        }
    }

    @Inject(method = "respawn(Lnet/minecraft/server/level/ServerPlayer;Z)Lnet/minecraft/server/level/ServerPlayer;", at = @At("RETURN"), require = 0)
    private void onRespawn(ServerPlayer player, boolean alive, CallbackInfoReturnable<ServerPlayer> cir) {
        final ServerPlayer newPlayer = cir.getReturnValue();
        if (newPlayer == null) return;

        try {
            if (LevelBorderMod.levelBorderHandler == null) return;

            newPlayer.getLevel().getServer().execute(() -> {
                try {
                    LevelBorderMod.levelBorderHandler.initBorder(newPlayer, newPlayer.getLevel().dimension() == Level.NETHER);

                    final var respawnPos = newPlayer.getRespawnPosition();
                    final boolean hadCustomRespawn = respawnPos != null;

                    boolean respawnPointOutsideBorder = false;
                    if (hadCustomRespawn) {
                        final var border = LevelBorderMod.levelBorderHandler.getBorderForPlayer(newPlayer);
                        if (border != null) {
                            final var respawnBox = new AABB(
                                    respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(),
                                    respawnPos.getX() + 1.0d, respawnPos.getY() + 2.0d, respawnPos.getZ() + 1.0d
                            );
                            respawnPointOutsideBorder = !border.isWithinBounds(respawnBox);
                        }
                    }

                    if (respawnPointOutsideBorder) {
                        newPlayer.sendSystemMessage(Component.literal(
                                "Spawn point outside border! Relocated to safe zone."));
                    }

                    if (!LevelBorderMod.levelBorderHandler.isWithinBorder(newPlayer)) {
                        final var pos = LevelBorderMod.levelBorderHandler.getRespawnPos();
                        final var overworld = newPlayer.getLevel().getServer().overworld();
                        if (overworld != null) {
                            newPlayer.teleportTo(overworld, pos.x() + 0.5d, (double) pos.y(), pos.z() + 0.5d,
                                    newPlayer.getYRot(), newPlayer.getXRot());
                        }
                    }
                } catch (Throwable inner) {

                }
            });
        } catch (Throwable t) {

        }
    }
}