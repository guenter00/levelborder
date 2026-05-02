package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;

public class PaperLevelBorderHandler extends VanillaLevelBorderHandler {
    @Override
    protected MinecraftServer getServer() {
        return ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getServer();
    }
}