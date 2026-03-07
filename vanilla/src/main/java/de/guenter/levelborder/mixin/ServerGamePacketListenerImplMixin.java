package de.guenter.levelborder.mixin;

import de.guenter.levelborder.LevelBorderMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.logging.Logger;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    private static final Logger LOGGER = Logger.getLogger("LevelBorder");

    @Shadow public ServerPlayer player;

    @Inject(method = "onDisconnect", at = @At("TAIL"))
    private void onRemovePlayer(CallbackInfo ci) {
        try {
            if (LevelBorderMod.levelBorderHandler != null) {
                LevelBorderMod.levelBorderHandler.onLeave(player);
            }
        } catch (Throwable t) {
            LOGGER.warning("[LevelBorder] Error in onRemovePlayer: " + t);
        }
    }
}