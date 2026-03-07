package de.guenter.levelborder.mixin;

import de.guenter.levelborder.LevelBorderMod;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {

    @Inject(method = "giveExperiencePoints", at = @At("RETURN"))
    private void onGivePoints(CallbackInfo ci) {
        notifyExperienceChange();
    }

    @Inject(method = "setExperiencePoints", at = @At("RETURN"))
    private void onSetPoints(CallbackInfo ci) {
        notifyExperienceChange();
    }

    @Inject(method = "giveExperienceLevels", at = @At("RETURN"))
    private void onAddLevel(CallbackInfo ci) {
        notifyLevelChange();
    }

    @Inject(method = "setExperienceLevels", at = @At("RETURN"))
    private void onSetLevel(CallbackInfo ci) {
        notifyLevelChange();
    }

    @Inject(method = "onEnchantmentPerformed", at = @At("RETURN"))
    private void onApplyEnchantmentCost(CallbackInfo ci) {
        notifyLevelChange();
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