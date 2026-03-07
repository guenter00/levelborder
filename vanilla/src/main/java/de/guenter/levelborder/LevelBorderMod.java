package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;

public class LevelBorderMod {
    public static LevelBorderHandler<ServerPlayer, WorldBorder, MinecraftServer> levelBorderHandler;
}