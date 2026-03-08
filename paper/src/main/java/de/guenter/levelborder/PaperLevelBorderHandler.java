package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;

public class PaperLevelBorderHandler extends VanillaLevelBorderHandler {
    @Override
    protected WorldBorder createWorldBorder(ServerPlayer player) {
        return super.createWorldBorder(player);
    }

    @Override
    protected MinecraftServer getServer() {
        return ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getServer();
    }
}