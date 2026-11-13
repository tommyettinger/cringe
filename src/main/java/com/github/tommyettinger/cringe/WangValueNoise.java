/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.badlogic.gdx.math.MathUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static com.github.tommyettinger.cringe.PointHasher.*;

/**
 * {@link ValueNoise Value Noise} adapted to use compatible edges for a Wang tiling or omni-tiling.
 * This should be used with {@link WangContinuousNoise}, not the regular {@link ContinuousNoise}. This only creates
 * tiling areas that are squares, cubes, or hypercubes; it can't make other tiling shapes.
 * <br>
 * You can request a set of 16 2D Wang tiles by giving this 16 sequential seeds and filling the
 * {@link WangContinuousNoise#getTileSize()} with 2D noise. You can use 3D noise or higher dimensions for this
 * case as well, as long as the extra dimensions aren't too close to 0 or the tile size (which would make them use
 * the same pattern for every texture). Unlike most Wang-tiling solutions, you can also request Wang-tiling cubes,
 * where the inside of each cube is very different but the faces are compatible with each other. You could even,
 * for some reason, request Wang hypercubes up to 6D. That would need 4096 6D cubes, though.
 * <br>
 * An alternative is to use omni-tiling or single-edge mode, with {@link WangContinuousNoise#setSingleEdge(boolean)}.
 * You can request up to 16 omni-tiling squares that all have compatible edges if you use 2D, up to 64 omni-tiling
 * cubes if you use 3D, and so on. You can also just get a 2D slice of the 3D cubes by using the same value for z
 * in every call (again, not too close to 0 or tileSize).
 * <br>
 * Internally, this works by changing the seed to an alternate one when the noise is close to an edge.
 * That makes the value used for edges at a specific position always the same (the alternate seed doesn't
 * change), unless it's a Wang tile edge that should be different.
 */
public class WangValueNoise extends WangRawNoise {

    public static final WangValueNoise instance = new WangValueNoise();

    public int seed = 0xD1CEBEEF;

    private transient final int[] altSeeds = new int[12];
    public transient int edge = 1;

    /**
     * Constructs a WangValueNoise with the seed 0xD1CEBEEF, or -774979857 .
     */
    public WangValueNoise() {
    }

    public WangValueNoise(int seed) {
        this.seed = seed;
    }

    @Override
    public String getTag() {
        return "WangValueNoise";
    }

    @Override
    public WangValueNoise copy() {
        return new WangValueNoise(seed);
    }

    public float valueNoise(float x, int seed) {
        final int STEPX = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        seed ^= seed >>> 17;
        return ((1 - x) * hashPart1024(xFloor, seed) + x * hashPart1024(xFloor + STEPX, seed))
                * 0x1.00401p-9f - 1f;
    }

    /**
     * Doesn't need anything special, makes a 10-bit hash.
     * @param x any int, doesn't need to be premultiplied.
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int s) {
        if(x <= 0) {s = altSeeds[0];}
        else if(x >= edge) {s = altSeeds[1]; x -= edge;}

        s += x * X1;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public float valueNoise(float x, float y, int seed)
    {
        final int STEPX = 1;
        final int STEPY = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (3 - (y + y)); // Won't go outside 0f to 1f range.
        return ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, seed))
                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, seed)))
                * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 3D version of R2, but we only use 2 of the 3 constants.
     * @param x should be premultiplied by 0xD1B55
     * @param y should be premultiplied by 0xABC99
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int y, int s) {
        int crossed = 0;
        if(x <= 0) {s = altSeeds[0]; ++crossed;}
        else if(x >= edge) {s = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {s = altSeeds[2]; ++crossed;}
        else if(y >= edge) {s = altSeeds[3]; ++crossed; y -= edge;}
        if(crossed > 1) s = -crossed;

        s += x * X2 ^ y * Y2;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public float valueNoise(float x, float y, float z, int seed)
    {
        final int STEPX = 1;
        final int STEPY = 1;
        final int STEPZ = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (3 - (y + y)); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (3 - (z + z)); // Won't go outside 0f to 1f range.
        return ((1 - z) *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, seed)))
                + z *
                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, seed))
                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed)))
        ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 4D version of R2, but we only use 3 of the 4 constants.
     * @param x should be premultiplied by 0xDB4F1
     * @param y should be premultiplied by 0xBBE05
     * @param z should be premultiplied by 0xA0F2F
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int y, int z, int s) {
        int crossed = 0;
        if(x <= 0) {s = altSeeds[0]; ++crossed;}
        else if(x >= edge) {s = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {s = altSeeds[2]; ++crossed;}
        else if(y >= edge) {s = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {s = altSeeds[4]; ++crossed;}
        else if(z >= edge) {s = altSeeds[5]; ++crossed; z -= edge;}
        if(crossed > 1) s = -crossed;

        s += x * X3 ^ y * Y3 ^ z * Z3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public float valueNoise(float x, float y, float z, float w, int seed)
    {
        final int STEPX = 1;
        final int STEPY = 1;
        final int STEPZ = 1;
        final int STEPW = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (3 - (y + y)); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (3 - (z + z)); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (3 - (w + w)); // Won't go outside 0f to 1f range.
        return ((1 - w) *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, seed))))
                + (w *
                ((1 - z) *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, seed)))
                        + z *
                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, seed))
                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed)))
                ))) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 5D version of R2, but we only use 4 of the 5 constants.
     * @param x should be premultiplied by 0xE19B1
     * @param y should be premultiplied by 0xC6D1D
     * @param z should be premultiplied by 0xAF36D
     * @param w should be premultiplied by 0x9A695
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int y, int z, int w, int s) {
        int crossed = 0;
        if(x <= 0) {s = altSeeds[0]; ++crossed;}
        else if(x >= edge) {s = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {s = altSeeds[2]; ++crossed;}
        else if(y >= edge) {s = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {s = altSeeds[4]; ++crossed;}
        else if(z >= edge) {s = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {s = altSeeds[6]; ++crossed;}
        else if(w >= edge) {s = altSeeds[7]; ++crossed; w -= edge;}
        if(crossed > 1) s = -crossed;

        s += x * X4 ^ y * Y4 ^ z * Z4 ^ w * W4;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public float valueNoise(float x, float y, float z, float w, float u, int seed)
    {
        final int STEPX = 1;
        final int STEPY = 1;
        final int STEPZ = 1;
        final int STEPW = 1;
        final int STEPU = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (3 - (y + y)); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (3 - (z + z)); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (3 - (w + w)); // Won't go outside 0f to 1f range.
        int uFloor = MathUtils.floor(u);
        u -= uFloor;
        u *= u * (3 - (u + u)); // Won't go outside 0f to 1f range.
        return ((1 - u) *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, seed)))
                        )))
                + (u *
                ((1 - w) *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, seed))))
                        + (w *
                        ((1 - z) *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, seed)))
                                + z *
                                ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed))
                                        + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, seed)))
                        ))))
        ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 6D version of R2, but we only use 5 of the 6 constants.
     * @param x should be premultiplied by 0xE60E3
     * @param y should be premultiplied by 0xCEBD7
     * @param z should be premultiplied by 0xB9C9B
     * @param w should be premultiplied by 0xA6F57
     * @param u should be premultiplied by 0x9609D
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int y, int z, int w, int u, int s) {
        int crossed = 0;
        if(x <= 0) {s = altSeeds[0]; ++crossed;}
        else if(x >= edge) {s = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {s = altSeeds[2]; ++crossed;}
        else if(y >= edge) {s = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {s = altSeeds[4]; ++crossed;}
        else if(z >= edge) {s = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {s = altSeeds[6]; ++crossed;}
        else if(w >= edge) {s = altSeeds[7]; ++crossed; w -= edge;}
        if(u <= 0) {s = altSeeds[8]; ++crossed;}
        else if(u >= edge) {s = altSeeds[9]; ++crossed; u -= edge;}
        if(crossed > 1) s = -crossed;

        s += x * X5 ^ y * Y5 ^ z * Z5 ^ w * W5 ^ u * U5;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public float valueNoise(float x, float y, float z, float w, float u, float v, int seed)
    {
        final int STEPX = 1;
        final int STEPY = 1;
        final int STEPZ = 1;
        final int STEPW = 1;
        final int STEPU = 1;
        final int STEPV = 1;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (3 - (x + x)); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (3 - (y + y)); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (3 - (z + z)); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (3 - (w + w)); // Won't go outside 0f to 1f range.
        int uFloor = MathUtils.floor(u);
        u -= uFloor;
        u *= u * (3 - (u + u)); // Won't go outside 0f to 1f range.
        int vFloor = MathUtils.floor(v);
        v -= vFloor;
        v *= v * (3 - (v + v)); // Won't go outside 0f to 1f range.
        return ((1 - v) *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, seed)))
                                )))))
                + (v *
                ((1 - u) *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, seed)))
                                )))
                        + (u *
                        ((1 - w) *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, seed))))
                                + (w *
                                ((1 - z) *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                        + z *
                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed))
                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, seed)))
                                ))))))
        ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 7D version of R2, but we only use 6 of the 7 constants.
     * @param x should be premultiplied by 0xE95E1
     * @param y should be premultiplied by 0xD4BC7
     * @param z should be premultiplied by 0xC1EDB
     * @param w should be premultiplied by 0xB0C8B
     * @param u should be premultiplied by 0xA127B
     * @param v should be premultiplied by 0x92E85
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private int hashPart1024(int x, int y, int z, int w, int u, int v, int s) {
        int crossed = 0;
        if(x <= 0) {s = altSeeds[0]; ++crossed;}
        else if(x >= edge) {s = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {s = altSeeds[2]; ++crossed;}
        else if(y >= edge) {s = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {s = altSeeds[4]; ++crossed;}
        else if(z >= edge) {s = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {s = altSeeds[6]; ++crossed;}
        else if(w >= edge) {s = altSeeds[7]; ++crossed; w -= edge;}
        if(u <= 0) {s = altSeeds[8]; ++crossed;}
        else if(u >= edge) {s = altSeeds[9]; ++crossed; u -= edge;}
        if(v <= 0) {s = altSeeds[10]; ++crossed;}
        else if(v >= edge) {s = altSeeds[11]; ++crossed; v -= edge;}
        if(crossed > 1) s = -crossed;

        s += x * X6 ^ y * Y6 ^ z * Z6 ^ w * W6 ^ u * U6 ^ v * V6;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    @Override
    public float getNoise(float x) {
        return valueNoise(x, seed);
    }

    @Override
    public float getNoise(float x, float y) {
        return valueNoise(x, y, seed);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return valueNoise(x, y, z, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return valueNoise(x, y, z, w, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return valueNoise(x, y, z, w, u, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return valueNoise(x, y, z, w, u, v, seed);
    }

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public float getNoiseWithSeed(float x, int seed) {
        return valueNoise(x, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        return valueNoise(x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        return valueNoise(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        return valueNoise(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        return valueNoise(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        return valueNoise(x, y, z, w, u, v, seed);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public WangValueNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static WangValueNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed =   MathSupport.intFromDec(data, 1, data.indexOf('`', 2));

        return new WangValueNoise(seed);
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(seed);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setSeed(in.readInt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WangValueNoise that = (WangValueNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "WangValueNoise{" +
                "seed=" + seed +
                '}';
    }

    public int getEdge() {
        return edge;
    }

    public void setEdge(int edge) {
        this.edge = edge;
    }

    public int[] getAltSeeds() {
        return altSeeds;
    }
}
