package de.guenter.levelborder;

import java.util.*;

public abstract class LevelBorderHandler<Player, WorldBorder, Server> {
    private final Map<UUID, WorldBorder> borders = new HashMap<>();

    private double calculateSize(Player player) {
        final int experience = switch (getMode()) {
            case OWN, SHARED -> getExperienceLevel(player);
            case SUM -> getPlayers().stream()
                .map(this::getExperienceLevel).reduce(0, Integer::sum);
        };
        return Math.max(experience * 2.0D, 1.0D);
    }

    private double getEffectiveSize(Player player) {
        double size = calculateSize(player);
        if (isDisableBorderShrink()) {
            double maxSize = getMaxBorderSize(player);
            if (size > maxSize) {
                setMaxBorderSize(player, size);
            } else {
                size = maxSize;
            }
        }
        return size;
    }

    final public void initBorder(Player player) {
        if (getMode() == BorderMode.SHARED) {
            shareExperience();
        }

        final var border = createWorldBorder(player);
        initBorder(player, border, getEffectiveSize(player));
        borders.put(getUUID(player), border);

        for (Player onlinePlayer : getPlayers()) {
            if (onlinePlayer != player) {
                updateWorldBorder(onlinePlayer);
            }
        }
    }

    final public void onChangeExperience() {
        final var mode = getMode();
        if (mode == BorderMode.SHARED) {
            shareExperience();
        }
        if (mode != BorderMode.OWN) {
            updateForAll();
        }
    }

    final public void onChangeLevel(Player player) {
        final var mode = getMode();
        if (mode != BorderMode.OWN) {
            if (mode == BorderMode.SHARED) {
                shareExperience();
            }
            updateForAll();
        } else {
            updateWorldBorder(player);
        }
    }

    final public void updateWorldBorder(Player player) {
        final var border = borders.get(getUUID(player));
        if (border != null) {
            interpolateBorder(player, border, getEffectiveSize(player), 2L * 1000L);
        }
    }

    final protected WorldBorder getBorderForPlayer(Player player) {
        return borders.get(getUUID(player));
    }

    final public void checkOutsideBorder(Player player) {
        final var border = borders.get(getUUID(player));
        if (border != null) {
            tickBorder(player, border);
            if (getDistance(player, border) + 5.0d < 0) {
                hurt(player, 1f);
            }
        }
    }

    final public boolean isWithinBorder(Player player) {
        final var border = borders.get(getUUID(player));
        return border == null || getDistance(player, border) >= 0;
    }

    public void onLeave(Player player) {
        borders.remove(getUUID(player));
        onPlayerLeave(player);
        updateForAll();
    }

    protected void onPlayerLeave(Player player) {
    }

    public Pos3i getRespawnPos() {
        return sharedOverworldSpawn();
    }

    private void shareExperience() {
        final var maxPlayer = getPlayers().stream()
            .max(Comparator.comparingInt(this::getTotalExperience))
            .orElseThrow();
        for (Player player : getPlayers()) {
            if (player != maxPlayer) {
                copyExperience(player, maxPlayer);
            }
        }
    }

    private void updateForAll() {
        getPlayers().forEach(this::updateWorldBorder);
    }

    public void setMode(BorderMode mode) {
        if (mode == BorderMode.SHARED) {
            shareExperience();
        }
        updateForAll();
    }

    public void setDisableBorderShrink(boolean value) {
        updateForAll();
    }

    final public boolean isDisableBorderShrinkPublic() {
        return isDisableBorderShrink();
    }

    abstract protected boolean isDisableBorderShrink();
    abstract protected double getMaxBorderSize(Player player);
    abstract protected void setMaxBorderSize(Player player, double size);

    abstract protected BorderMode getMode();

    abstract protected WorldBorder createWorldBorder(Player player);
    abstract protected void setCenter(WorldBorder border, double centerX, double centerZ);

    abstract protected void initBorder(Player player, WorldBorder border, double size);
    abstract protected void interpolateBorder(Player player, WorldBorder border, double size, long time);

    abstract protected Server getServer();
    abstract protected Collection<Player> getPlayers();

    abstract protected double getDistance(Player player, WorldBorder border);

    abstract protected Pos3i sharedOverworldSpawn();

    abstract protected int getTotalExperience(Player player);
    abstract protected int getExperienceLevel(Player player);

    abstract protected void copyExperience(Player player, Player other);

    abstract protected UUID getUUID(Player player);
    abstract protected void hurt(Player player, float damage);

    abstract protected void tickBorder(Player player, WorldBorder border);
}
