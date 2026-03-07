package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod("levelborder")
public class LevelBorderMod {
    public static LevelBorderHandler<ServerPlayer, WorldBorder, MinecraftServer> levelBorderHandler;

    public LevelBorderMod() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        levelBorderHandler = new ModLevelBorderHandler(event.getServer());
    }
}