package de.guenter.levelborder;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.border.WorldBorder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public class LevelBorderPlugin extends JavaPlugin implements Listener {
    private LevelBorderHandler<ServerPlayer, WorldBorder, ?> levelBorderHandler;

    @Override
    public void onLoad() {
        levelBorderHandler = new PaperLevelBorderHandler();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        VanillaLevelBorderCommand.register(
                ((CraftServer) Bukkit.getServer()).getServer().getCommands().getDispatcher(),
                () -> levelBorderHandler
        );
    }

    private ServerPlayer toVanillaPlayer(Player player) {
        return ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle();
    }

    private boolean isInNether(Player player) {
        return player.getWorld().getEnvironment() == World.Environment.NETHER;
    }

    @EventHandler
    public void onPlayerTick(ServerTickStartEvent event) {
        for (ServerPlayer player : ((CraftServer) Bukkit.getServer()).getServer().getPlayerList().getPlayers()) {
            levelBorderHandler.checkOutsideBorder(player);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        World respawnWorld = event.getRespawnLocation().getWorld();
        if (respawnWorld == null) return;

        ServerPlayer serverPlayer = toVanillaPlayer(event.getPlayer());
        WorldBorder border = levelBorderHandler.getBorderForPlayer(serverPlayer);
        if (border == null) return;

        Location target = event.getRespawnLocation();
        double x = target.getX();
        double z = target.getZ();

        boolean outsideBorder = x < border.getMinX() || x > border.getMaxX() ||
                                z < border.getMinZ() || z > border.getMaxZ();
        if (outsideBorder) {
            boolean hadCustomRespawn = event.isBedSpawn() || event.isAnchorSpawn();
            final var pos = levelBorderHandler.getRespawnPos();

            World overworld = Bukkit.getWorlds().stream()
                    .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                    .findFirst()
                    .orElse(respawnWorld);

            event.setRespawnLocation(new Location(overworld, pos.x() + 0.5d, pos.y(), pos.z() + 0.5d));

            if (hadCustomRespawn) {
                event.getPlayer().sendMessage("Spawn point outside border! Relocated to safe zone.");
            }
        }

        Bukkit.getScheduler().runTaskLater(this, () ->
            levelBorderHandler.initBorder(toVanillaPlayer(event.getPlayer())), 20L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            final var serverPlayer = toVanillaPlayer(event.getPlayer());
            levelBorderHandler.initBorder(serverPlayer, isInNether(event.getPlayer()));

            if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL
                    && !levelBorderHandler.isWithinBorder(serverPlayer)) {
                final var pos = levelBorderHandler.getRespawnPos();
                World overworld = Bukkit.getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                        .findFirst()
                        .orElse(event.getPlayer().getWorld());
                event.getPlayer().teleport(new Location(overworld, pos.x() + 0.5d, pos.y(), pos.z() + 0.5d));
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTaskLater(this, () ->
            levelBorderHandler.onLeave(toVanillaPlayer(event.getPlayer())), 20L);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        levelBorderHandler.initBorder(toVanillaPlayer(event.getPlayer()), isInNether(event.getPlayer()));
}

    @EventHandler
    public void onChangeLevel(PlayerLevelChangeEvent event) {
        levelBorderHandler.onChangeLevel(toVanillaPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onChangePoints(PlayerExpChangeEvent event) {
        levelBorderHandler.onChangeExperience();
    }
}