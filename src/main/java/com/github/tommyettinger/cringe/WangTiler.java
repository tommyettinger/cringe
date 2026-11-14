package com.github.tommyettinger.cringe;

/**
 * A utility class that allows finding the appropriate index of Wang-tiling regions to place on a grid. This allows an
 * int seed to produce many possible tilings for the same region in space.
 */
public final class WangTiler {
    private WangTiler() {
    }

    /**
     * Allows determining which index in a Wang-tiling set of squares, from 0 to 15 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile2D(int, WangRawNoise)} uses as well. This considers positive
     * x to be a movement to the east (rightward), and positive y to be a movement to the north (upward).
     *
     * @param x horizontal grid position to place a Wang-tiling square
     * @param y vertical grid position to place a Wang-tiling square
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 15, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile2D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int seed){
        if((x + y & 1) == 0){
            return PointHasher.hashAll(x, y, seed) & 15;
        }
        int w = PointHasher.hashAll(x - 1, y, seed) + 1 & 2;
        int e = PointHasher.hashAll(x + 1, y, seed) & 2;
        int s = PointHasher.hashAll(x, y - 1, seed) + 4 & 8;
        int n = PointHasher.hashAll(x, y + 1, seed) & 8;

        return w | s | (w & e) >>> 1 | (n & s) >>> 1;
    }

    /**
     * Allows determining which index in a Wang-tiling set of cubes, from 0 to 63 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile3D(int, WangRawNoise)} uses as well.
     *
     * @param x x grid position to place a Wang-tiling square
     * @param y y grid position to place a Wang-tiling square
     * @param z z grid position to place a Wang-tiling square
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile3D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int z, int seed){
        if((x + y + z & 1) == 0){
            return PointHasher.hashAll(x, y, z, seed) & 63;
        }
        int w = PointHasher.hashAll(x - 1, y, z, seed) + 1 & 2;
        int e = PointHasher.hashAll(x + 1, y, z, seed) & 2;
        int s = PointHasher.hashAll(x, y - 1, z, seed) + 4 & 8;
        int n = PointHasher.hashAll(x, y + 1, z, seed) & 8;
        int d = PointHasher.hashAll(x, y, z - 1, seed) + 16 & 32;
        int u = PointHasher.hashAll(x, y, z + 1, seed) & 32;

        return w | s | d | (w & e) >>> 1 | (n & s) >>> 1 | (u & d) >>> 1;
    }
}
