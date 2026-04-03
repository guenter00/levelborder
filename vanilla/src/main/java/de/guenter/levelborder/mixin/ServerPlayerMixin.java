package de.guenter.levelborder.mixin;

import de.guenter.levelborder.LevelBorderMod;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Unique
    private int preActionLevel = -1;

    @Unique
    private int preActionTotalExp = -1;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTracker(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (this.preActionTotalExp != player.totalExperience) {
            this.preActionTotalExp = player.totalExperience;
            notifyExperienceChange();
        }

        if (this.preActionLevel != player.experienceLevel) {
            this.preActionLevel = player.experienceLevel;
            notifyLevelChange();
        }
    }

    private void notifyExperienceChange() {
        if (LevelBorderMod.levelBorderHandler != null) {
            LevelBorderMod.levelBorderHandler.onChangeExperience();
        }
    }

    private void notifyLevelChange() {
        if (LevelBorderMod.levelBorderHandler != null) {
            LevelBorderMod.levelBorderHandler.onChangeLevel((ServerPlayer) (Object) this);
        }
    }
}