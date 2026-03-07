package de.guenter.levelborder.mixin;

import com.mojang.brigadier.CommandDispatcher;
import de.guenter.levelborder.LevelBorderMod;
import de.guenter.levelborder.VanillaLevelBorderCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Shadow @Final private CommandDispatcher<CommandSourceStack> dispatcher;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onRegister(CallbackInfo ci) {
        VanillaLevelBorderCommand.register(dispatcher, () -> LevelBorderMod.levelBorderHandler);
    }
}