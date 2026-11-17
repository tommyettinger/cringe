/*
 * Copyright (c) 2026 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.tommyettinger.cringe;

/**
 * A utility class that allows finding the appropriate index of Wang-tiling or omni-tiling regions to place on a grid.
 * This allows an int seed to produce many possible tilings for the same region in space. It can lay out Wang tiling
 * square areas with {@link #select(int, int, int)}, cubes with {@link #select(int, int, int, int)}, and so on up to 6D.
 * It can lay out single-edge (omni-tiling) square areas with {@link #selectSingleEdge(int, int, int)}, but can also use
 * {@link #selectSingleEdgeSeparated(int, int, int)} or {@link #selectSingleEdgeAlternate(int, int, int)} in 2D. In 3D
 * and higher dimensions, you can use {@link #selectSingleEdge(int, int, int, int)} and its overloads.
 */
public final class WangTiler {
    private WangTiler() {
    }

    /**
     * Allows determining which index in a Wang-tiling set of squares, from 0 to 15 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile2D(int, WangRawNoise)} uses as well. This considers positive
     * x to be a movement to the east (rightward), and positive y to be a movement to the south (downward).
     *
     * @param x horizontal grid position to place a Wang-tiling square
     * @param y vertical grid position to place a Wang-tiling square; uses y-down by default
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 15, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile2D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int seed){
        if((x + y & 1) == 0){
            return PointHasher.hash256(x, y, seed) & 15;
        }
        int W = PointHasher.hash256(x - 1, y, seed) + 1 & 2;
        int E = PointHasher.hash256(x + 1, y, seed) & 2;
        int S = PointHasher.hash256(x, y - 1, seed) + 4 & 8;
        int N = PointHasher.hash256(x, y + 1, seed) & 8;

        return W | S | (W ^ E) >>> 1 | (N ^ S) >>> 1;
    }

    /**
     * Allows determining which index in a Wang-tiling set of cubes, from 0 to 63 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile3D(int, WangRawNoise)} uses as well.
     *
     * @param x x grid position to place a Wang-tiling cube
     * @param y y grid position to place a Wang-tiling cube
     * @param z z grid position to place a Wang-tiling cube
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile3D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int z, int seed){
        if((x + y + z & 1) == 0){
            return PointHasher.hash256(x, y, z, seed) & 63;
        }
        int W = PointHasher.hash256(x - 1, y, z, seed) + 1 & 2;
        int E = PointHasher.hash256(x + 1, y, z, seed) & 2;
        int S = PointHasher.hash256(x, y - 1, z, seed) + 4 & 8;
        int N = PointHasher.hash256(x, y + 1, z, seed) & 8;
        int D = PointHasher.hash256(x, y, z - 1, seed) + 16 & 32;
        int U = PointHasher.hash256(x, y, z + 1, seed) & 32;

        return W | S | D | (W ^ E) >>> 1 | (N ^ S) >>> 1 | (U ^ D) >>> 1;
    }

    /**
     * Allows determining which index in a Wang-tiling set of hypercubes, from 0 to 255 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile4D(int, WangRawNoise)} uses as well.
     *
     * @param x x grid position to place a Wang-tiling hypercube
     * @param y y grid position to place a Wang-tiling hypercube
     * @param z z grid position to place a Wang-tiling hypercube
     * @param w w grid position to place a Wang-tiling hypercube
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 255, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile4D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int z, int w, int seed){
        if((x + y + z + w & 1) == 0){
            return PointHasher.hash256(x, y, z, w, seed);
        }
        int W = PointHasher.hash256(x - 1, y, z, w, seed) + 1 & 2; // west
        int E = PointHasher.hash256(x + 1, y, z, w, seed) & 2; // east
        int S = PointHasher.hash256(x, y - 1, z, w, seed) + 4 & 8; // south
        int N = PointHasher.hash256(x, y + 1, z, w, seed) & 8; // north
        int D = PointHasher.hash256(x, y, z - 1, w, seed) + 16 & 32; // down
        int U = PointHasher.hash256(x, y, z + 1, w, seed) & 32; // up
        int K = PointHasher.hash256(x, y, z, w - 1, seed) + 64 & 128; // kata
        int A = PointHasher.hash256(x, y, z, w + 1, seed) & 128; // ana

        return W | S | D | K | (W ^ E) >>> 1 | (N ^ S) >>> 1 | (U ^ D) >>> 1 | (A ^ K) >>> 1;
    }

    /**
     * Allows determining which index in a Wang-tiling set of hypercubes, from 0 to 1023 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile5D(int, WangRawNoise)} uses as well.
     *
     * @param x x grid position to place a Wang-tiling hypercube
     * @param y y grid position to place a Wang-tiling hypercube
     * @param z z grid position to place a Wang-tiling hypercube
     * @param w w grid position to place a Wang-tiling hypercube
     * @param u u grid position to place a Wang-tiling hypercube
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 1023, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile5D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int z, int w, int u, int seed){
        if((x + y + z + w + u & 1) == 0){
            return PointHasher.hashAll(x, y, z, w, u, seed) & 1023;
        }
        int XL = PointHasher.hashAll(x - 1, y, z, w, u, seed) + 1 & 2;
        int XH = PointHasher.hashAll(x + 1, y, z, w, u, seed) & 2;
        int YL = PointHasher.hashAll(x, y - 1, z, w, u, seed) + 4 & 8;
        int YH = PointHasher.hashAll(x, y + 1, z, w, u, seed) & 8;
        int ZL = PointHasher.hashAll(x, y, z - 1, w, u, seed) + 16 & 32;
        int ZH = PointHasher.hashAll(x, y, z + 1, w, u, seed) & 32;
        int WL = PointHasher.hashAll(x, y, z, w - 1, u, seed) + 64 & 128;
        int WH = PointHasher.hashAll(x, y, z, w + 1, u, seed) & 128;
        int UL = PointHasher.hashAll(x, y, z, w, u - 1, seed) + 256 & 512;
        int UH = PointHasher.hashAll(x, y, z, w, u + 1, seed) & 512;

        return XL | YL | ZL | WL | UL |
                (XL ^ XH) >>> 1 | (YH ^ YL) >>> 1 | (ZH ^ ZL) >>> 1 | (WH ^ WL) >>> 1 | (UH ^ UL) >>> 1;
    }

    /**
     * Allows determining which index in a Wang-tiling set of hypercubes, from 0 to 4095 inclusive, to place at a given
     * point on a grid with a configurable seed. This expects the noise indices to match what WangNoiseGenerator uses,
     * that is, what {@link WangContinuousNoise#setWangTile6D(int, WangRawNoise)} uses as well.
     *
     * @param x x grid position to place a Wang-tiling hypercube
     * @param y y grid position to place a Wang-tiling hypercube
     * @param z z grid position to place a Wang-tiling hypercube
     * @param w w grid position to place a Wang-tiling hypercube
     * @param u u grid position to place a Wang-tiling hypercube
     * @param v v grid position to place a Wang-tiling hypercube
     * @param seed can be any int, but should be the same for a single tiling space
     * @return an index between 0 and 4095, inclusive, referring to what index does in {@link WangContinuousNoise#setWangTile6D(int, WangRawNoise)}
     */
    public static int select(int x, int y, int z, int w, int u, int v, int seed){
        if((x + y + z + w + u + v & 1) == 0){
            return PointHasher.hashAll(x, y, z, w, u, v, seed) & 4095;
        }
        int XL = PointHasher.hashAll(x - 1, y, z, w, u, v, seed) + 1 & 2;
        int XH = PointHasher.hashAll(x + 1, y, z, w, u, v, seed) & 2;
        int YL = PointHasher.hashAll(x, y - 1, z, w, u, v, seed) + 4 & 8;
        int YH = PointHasher.hashAll(x, y + 1, z, w, u, v, seed) & 8;
        int ZL = PointHasher.hashAll(x, y, z - 1, w, u, v, seed) + 16 & 32;
        int ZH = PointHasher.hashAll(x, y, z + 1, w, u, v, seed) & 32;
        int WL = PointHasher.hashAll(x, y, z, w - 1, u, v, seed) + 64 & 128;
        int WH = PointHasher.hashAll(x, y, z, w + 1, u, v, seed) & 128;
        int UL = PointHasher.hashAll(x, y, z, w, u - 1, v, seed) + 256 & 512;
        int UH = PointHasher.hashAll(x, y, z, w, u + 1, v, seed) & 512;
        int VL = PointHasher.hashAll(x, y, z, w, u, v - 1, seed) + 1024 & 2048;
        int VH = PointHasher.hashAll(x, y, z, w, u, v + 1, seed) & 2048;

        return XL | YL | ZL | WL | UL | VL |
                (XL ^ XH) >>> 1 | (YH ^ YL) >>> 1 | (ZH ^ ZL) >>> 1 | (WH ^ WL) >>> 1 | (UH ^ UL) >>> 1 | (VH ^ VL) >>> 1;
    }

    /**
     * Gets a pseudo-random index from 0 to 63, inclusive, for single-edge (omni-tiling) 2D noise.
     * This is likely to choose some index more than once given 9 or more tiles.
     * @param x horizontal grid position to place a Wang-tiling square
     * @param y vertical grid position to place a Wang-tiling square; y-up or y-down doesn't matter here
     * @param seed all bits are used; should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive
     */
    public static int selectSingleEdge(int x, int y, int seed) {
        return PointHasher.hash256(x, y, seed) & 63;
    }

    /**
     * Gets a pseudo-random index from 0 to 63, inclusive, for single-edge (omni-tiling) 3D noise.
     * This is likely to choose some index more than once given 9 or more tiles.
     * @param x x grid position to place a Wang-tiling square
     * @param y y grid position to place a Wang-tiling square
     * @param z z grid position to place a Wang-tiling square
     * @param seed all bits are used; should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive
     */
    public static int selectSingleEdge(int x, int y, int z, int seed) {
        return PointHasher.hash256(x, y, z, seed) & 63;
    }

    /**
     * Gets a pseudo-random index from 0 to 255, inclusive, for single-edge (omni-tiling) 4D noise.
     * This is likely to choose some index more than once given 17 or more tiles.
     * @param x x grid position to place a Wang-tiling square
     * @param y y grid position to place a Wang-tiling square
     * @param z z grid position to place a Wang-tiling square
     * @param w w grid position to place a Wang-tiling square
     * @param seed all bits are used; should be the same for a single tiling space
     * @return an index between 0 and 255, inclusive
     */
    public static int selectSingleEdge(int x, int y, int z, int w, int seed) {
        return PointHasher.hash256(x, y, z, w, seed);
    }

    /**
     * Gets a pseudo-random index from 0 to 1023, inclusive, for single-edge (omni-tiling) 5D noise.
     * This is likely to choose some index more than once given 33 or more tiles.
     * @param x x grid position to place a Wang-tiling square
     * @param y y grid position to place a Wang-tiling square
     * @param z z grid position to place a Wang-tiling square
     * @param w w grid position to place a Wang-tiling square
     * @param u u grid position to place a Wang-tiling square
     * @param seed all bits are used; should be the same for a single tiling space
     * @return an index between 0 and 1023, inclusive
     */
    public static int selectSingleEdge(int x, int y, int z, int w, int u, int seed) {
        return PointHasher.hashAll(x, y, z, w, u, seed) & 1023;
    }

    /**
     * Gets a pseudo-random index from 0 to 4095, inclusive, for single-edge (omni-tiling) 6D noise.
     * This is likely to choose some index more than once given 65 or more tiles.
     * @param x x grid position to place a Wang-tiling square
     * @param y y grid position to place a Wang-tiling square
     * @param z z grid position to place a Wang-tiling square
     * @param w w grid position to place a Wang-tiling square
     * @param u u grid position to place a Wang-tiling square
     * @param v v grid position to place a Wang-tiling square
     * @param seed all bits are used; should be the same for a single tiling space
     * @return an index between 0 and 4095, inclusive
     */
    public static int selectSingleEdge(int x, int y, int z, int w, int u, int v, int seed) {
        return PointHasher.hashAll(x, y, z, w, u, v, seed) & 4095;
    }

    /**
     * Gets a less-random index from 0 to 63, inclusive, for single-edge (omni-tiling) noise.
     * This won't repeat in the first 8x8 grid of tiles, which can be useful to reduce the appearance
     * of duplicates. This only can take x and y from 0 to 65535 meaningfully.
     * Only the low 12 bits of seed are used, so seeds from 0 to 4095 will all work, but higher ones will be duplicates.
     * This uses the Rosenberg-Strong pairing function, which lines up indices in concentric squares.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a> for more on that.
     *
     * @param x non-negative horizontal grid position to place a Wang-tiling square
     * @param y non-negative vertical grid position to place a Wang-tiling square; y-up or y-down doesn't matter here
     * @param seed only the low 12 bits are used; should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive
     */
    public static int selectSingleEdgeSeparated(int x, int y, int seed){
        x &= 0xFFFF;
        y &= 0xFFFF;
        final int m = Math.max(x, y);
        return (m * m + m + y - x ^ seed ^ seed >>> 6) & 63;
    }

    /**
     * Gets a less-random index from 0 to 63, inclusive, for single-edge (omni-tiling) noise.
     * This shouldn't repeat in the first 6x6 grid of tiles, which can be useful to reduce the appearance
     * of duplicates. This only can take x and y from 0 to 65535 meaningfully.
     * Only the low 12 bits of seed are used, so seeds from 0 to 4095 will all work, but higher ones will be duplicates.
     * This uses the Cantor pairing function, which lines up indices in right-triangle shapes.
     * <a href="https://hbfs.wordpress.com/2018/07/31/moeud/">See Steven Pigeon's blog</a> for more on that.
     *
     * @param x non-negative horizontal grid position to place a Wang-tiling square
     * @param y non-negative vertical grid position to place a Wang-tiling square; y-up or y-down doesn't matter here
     * @param seed only the low 12 bits are used; should be the same for a single tiling space
     * @return an index between 0 and 63, inclusive
     */
    public static int selectSingleEdgeAlternate(int x, int y, int seed){
        x &= 0xFFFF;
        y &= 0xFFFF;
        return (y + ((x + y) * (x + y + 1) >>> 1) ^ seed ^ seed >>> 6) & 63;
    }
}
