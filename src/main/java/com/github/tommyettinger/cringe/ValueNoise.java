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

/**
 * A RawNoise type that produces blocky noise from 1D to 7D. Value noise is really only meant to be used as a building
 * block for better noise, be that with multiple octaves of value noise (and it takes more, typically, to get good
 * results), or by doing what things like {@link HoneyNoise} and {@link FoamNoise} do, mixing value noise with a
 * different algorithm or in a complex way. It is, however, quite fast, especially in low dimensions.
 */
public class ValueNoise extends RawNoise {

    public static final ValueNoise instance = new ValueNoise();

    public int seed = 0xD1CEBEEF;

    /**
     * Constructs a ValueNoise with the seed 0xD1CEBEEF, or -774979857 .
     */
    public ValueNoise() {
    }

    public ValueNoise(int seed) {
        this.seed = seed;
    }

    @Override
    public String getTag() {
        return "ValueNoise";
    }

    @Override
    public ValueNoise copy() {
        return new ValueNoise(seed);
    }

    public static float valueNoise(float x, int seed) {
        final int STEPX = 0x9E377;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
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
    private static int hashPart1024(final int x, int s) {
//        s *= ((x ^ x >>> 12) | 1);
//        s += (x ^ x >>> 16) * 0xAC451;
//        return (s >>> 3 ^ s >>> 10) & 0x3FF;
        s += x;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, int seed)
    {
        final int STEPX = 0xD1B55;
        final int STEPY = 0xABC99;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
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
    private static int hashPart1024(final int x, final int y, int s) {
        s += x ^ y;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, int seed)
    {
        final int STEPX = 0xDB4F1;
        final int STEPY = 0xBBE05;
        final int STEPZ = 0xA0F2F;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
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
    private static int hashPart1024(final int x, final int y, final int z, int s) {
        s += x ^ y ^ z;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, int seed)
    {
        final int STEPX = 0xE19B1;
        final int STEPY = 0xC6D1D;
        final int STEPZ = 0xAF36D;
        final int STEPW = 0x9A695;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
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
    private static int hashPart1024(final int x, final int y, final int z, final int w, int s) {
        s += x ^ y ^ z ^ w;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, int seed)
    {
        final int STEPX = 0xE60E3;
        final int STEPY = 0xCEBD7;
        final int STEPZ = 0xB9C9B;
        final int STEPW = 0xA6F57;
        final int STEPU = 0x9609D;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); // Won't go outside 0f to 1f range.
        int uFloor = MathUtils.floor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
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
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, int s) {
        s += x ^ y ^ z ^ w ^ u;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, float v, int seed)
    {
        final int STEPX = 0xE95E1;
        final int STEPY = 0xD4BC7;
        final int STEPZ = 0xC1EDB;
        final int STEPW = 0xB0C8B;
        final int STEPU = 0xA127B;
        final int STEPV = 0x92E85;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); // Won't go outside 0f to 1f range.
        int uFloor = MathUtils.floor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); // Won't go outside 0f to 1f range.
        int vFloor = MathUtils.floor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        vFloor *= STEPV;
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
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    public static float valueNoise(float x, float y, float z, float w, float u, float v, float m, int seed)
    {
        final int STEPX = 0xEBEDF;
        final int STEPY = 0xD96EB;
        final int STEPZ = 0xC862B;
        final int STEPW = 0xB8ACD;
        final int STEPU = 0xAA323;
        final int STEPV = 0x9CDA5;
        final int STEPM = 0x908E3;
        int xFloor = MathUtils.floor(x);
        x -= xFloor;
        x *= x * (1 - x - x + 2); // Won't go outside 0f to 1f range.
        int yFloor = MathUtils.floor(y);
        y -= yFloor;
        y *= y * (1 - y - y + 2); // Won't go outside 0f to 1f range.
        int zFloor = MathUtils.floor(z);
        z -= zFloor;
        z *= z * (1 - z - z + 2); // Won't go outside 0f to 1f range.
        int wFloor = MathUtils.floor(w);
        w -= wFloor;
        w *= w * (1 - w - w + 2); // Won't go outside 0f to 1f range.
        int uFloor = MathUtils.floor(u);
        u -= uFloor;
        u *= u * (1 - u - u + 2); // Won't go outside 0f to 1f range.
        int vFloor = MathUtils.floor(v);
        v -= vFloor;
        v *= v * (1 - v - v + 2); // Won't go outside 0f to 1f range.
        int mFloor = MathUtils.floor(m);
        m -= mFloor;
        m *= m * (1 - m - m + 2); // Won't go outside 0f to 1f range.
        xFloor *= STEPX;
        yFloor *= STEPY;
        zFloor *= STEPZ;
        wFloor *= STEPW;
        uFloor *= STEPU;
        vFloor *= STEPV;
        mFloor *= STEPM;
        return
                ((1 - m) *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor, seed)))
                                                )))))))
                        + (m *
                        ((1 - v) *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor, mFloor + STEPM, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor, mFloor + STEPM, seed)))
                                                )))))
                                + (v *
                                ((1 - u) *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor, vFloor + STEPV, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor, vFloor + STEPV, mFloor + STEPM, seed)))
                                                )))
                                        + (u *
                                        ((1 - w) *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))))
                                                + (w *
                                                ((1 - z) *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                        + z *
                                                        ((1 - y) * ((1 - x) * hashPart1024(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed))
                                                                + y * ((1 - x) * hashPart1024(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed) + x * hashPart1024(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, uFloor + STEPU, vFloor + STEPV, mFloor + STEPM, seed)))
                                                ))))))))
                ) * 0x1.00401p-9f - 1f;
    }

    /**
     * Constants are the most significant 20 bits of constants from QuasiRandomTools, incremented if even.
     * They should normally be used for the 8D version of R2, but we only use 7 of the 8 constants.
     * @param x should be premultiplied by 0xEBEDF
     * @param y should be premultiplied by 0xD96EB
     * @param z should be premultiplied by 0xC862B
     * @param w should be premultiplied by 0xB8ACD
     * @param u should be premultiplied by 0xAA323
     * @param v should be premultiplied by 0x9CDA5
     * @param m should be premultiplied by 0x908E3
     * @param s state, any int
     * @return a mediocre 10-bit hash
     */
    private static int hashPart1024(final int x, final int y, final int z, final int w, final int u, final int v, final int m, int s) {
        s += x ^ y ^ z ^ w ^ u ^ v ^ m;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 22;
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 7;
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

    public float getNoise(float x, float y, float z, float w, float u, float v, float m) {
        return valueNoise(x, y, z, w, u, v, m, seed);
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

    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, float m, int seed) {
        return valueNoise(x, y, z, w, u, v, m, seed);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public ValueNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static ValueNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed =   MathSupport.intFromDec(data, 1, data.indexOf('`', 2));

        return new ValueNoise(seed);
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

        ValueNoise that = (ValueNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "ValueNoise{" +
                "seed=" + seed +
                '}';
    }
}
