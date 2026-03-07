package de.guenter.levelborder;

public record Pos3i(int x, int y, int z) {
    public Pos3i div(int d) {
        return new Pos3i(x / d, y / d, z / d);
    }
}
