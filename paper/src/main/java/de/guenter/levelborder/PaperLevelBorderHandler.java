package de.guenter.levelborder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;

public class PaperLevelBorderHandler extends VanillaLevelBorderHandler {
    @Override
    protected WorldBorder createWorldBorder(ServerPlayer player) {
        WorldBorder border = new WorldBorder();
        border.world = (ServerLevel) player.level();
        return border;
    }

    @Override
    protected WorldBorder toPacketBorder(ServerPlayer player, WorldBorder serverBorder) {
        WorldBorder packetBorder = super.toPacketBorder(player, serverBorder);
        packetBorder.world = (ServerLevel) player.level();
        return packetBorder;
    }

    @Override
    protected MinecraftServer getServer() {
        return ((org.bukkit.craftbukkit.CraftServer) Bukkit.getServer()).getServer();
    }

    protected Pos2d getPaperBorderCenter(ServerPlayer player) {
        Pos3i spawn = sharedOverworldSpawn();
        var dim = player.level().dimension();

        if (dim == Level.NETHER) {
            Pos3i netherSpawn = spawn.div(8);
            return new Pos2d(netherSpawn.x() + 0.5d, netherSpawn.z() + 0.5d);
        } else if (dim == Level.END) {
            return new Pos2d(0.5d, 0.5d);
        }
        return new Pos2d(spawn.x() + 0.5d, spawn.z() + 0.5d);
    }

    @Override
    protected double getDistance(ServerPlayer player, WorldBorder border) {
        
        return border.getDistanceToBorder(player);
    }
}