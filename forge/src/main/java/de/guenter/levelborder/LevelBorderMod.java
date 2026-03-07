package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("levelborder")
public class LevelBorderMod {
    public static LevelBorderHandler<ServerPlayer, WorldBorder, MinecraftServer> levelBorderHandler;

    public LevelBorderMod() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        levelBorderHandler = new ModLevelBorderHandler(event.getServer());
    }
}