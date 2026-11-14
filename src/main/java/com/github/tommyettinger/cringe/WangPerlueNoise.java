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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static com.badlogic.gdx.math.MathUtils.floor;
import static com.badlogic.gdx.math.MathUtils.lerp;
import static com.github.tommyettinger.cringe.GradientVectors.*;
import static com.github.tommyettinger.cringe.PointHasher.*;

/**
 * {@link PerlueNoise Perlue Noise}, adapted to use compatible edges for a Wang tiling or omni-tiling. Perlue is a mix
 * of {@link PerlinNoise "Classic" Perlin noise}, written by Ken Perlin before he created Simplex Noise, with value
 * noise calculated at the same time.
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
 * That makes the gradient/value used for edges at a specific position always the same (the alternate seed doesn't
 * change), unless it's a Wang tile edge that should be different.
 */
public class WangPerlueNoise extends WangRawNoise {
    public static final WangPerlueNoise instance = new WangPerlueNoise();

    public static final float SCALE2 = 1.41421330f; //towardsZero(1f/ (float) Math.sqrt(2f / 4f));
    public static final float SCALE3 = 1.15470030f; //towardsZero(1f/ (float) Math.sqrt(3f / 4f));
    public static final float SCALE4 = 0.99999990f; //towardsZero(1f)                            ;
    public static final float SCALE5 = 0.89442706f; //towardsZero(1f/ (float) Math.sqrt(5f / 4f));
    public static final float SCALE6 = 0.81649643f; //towardsZero(1f/ (float) Math.sqrt(6f / 4f));

    public static final float EQ_ADD_2 = 1.0f / 0.85f;
    public static final float EQ_ADD_3 = 0.8f / 0.85f;
    public static final float EQ_ADD_4 = 0.6f / 0.85f;
    public static final float EQ_ADD_5 = 0.4f / 0.85f;
    public static final float EQ_ADD_6 = 0.2f / 0.85f;

    public static final float EQ_MUL_2 = 1.2535664f;
    public static final float EQ_MUL_3 = 1.2071217f;
    public static final float EQ_MUL_4 = 1.1588172f;
    public static final float EQ_MUL_5 = 1.1084094f;
    public static final float EQ_MUL_6 = 1.0555973f;

    public int seed;

    private transient final int[] altSeeds = new int[12];
    public transient int edge = 1;

    public WangPerlueNoise() {
        this(0x1337BEEF);
    }

    public WangPerlueNoise(final int seed) {
        this.seed = seed;
    }

    public WangPerlueNoise(WangPerlueNoise other) {
        this.seed = other.seed;
        System.arraycopy(other.altSeeds, 0, this.altSeeds, 0, other.altSeeds.length);
        this.edge = other.edge;
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 1.
     *
     * @return the minimum supported dimension, 1
     */
    @Override
    public int getMinDimension() {
        return 2;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, 6
     */
    @Override
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true because this generator can be seeded with {@link #setSeed(int)} and retrieved with
     * {@link #getSeed()}.
     *
     * @return true
     */
    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    /**
     * Sets the seed to the given int.
     * @param seed an int seed, with no restrictions
     */
    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * Gets the current seed of the generator, as an int.
     *
     * @return the current seed, as an int
     */
    @Override
    public int getSeed() {
        return seed;
    }

    /**
     * Returns the constant String {@code "WangPerlueNoise"} that identifies this in serialized Strings.
     *
     * @return a short String constant that identifies this RawNoise type, {@code "WangPerlueNoise"}
     */
    @Override
    public String getTag() {
        return "WangPerlueNoise";
    }

    /**
     * Creates a copy of this WangPerlueNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     *
     * @return a copy of this WangPerlueNoise
     */
    @Override
    public WangPerlueNoise copy() {
        return new WangPerlueNoise(this.seed);
    }

    protected float gradCoord2D(int seed, int x, int y,
                                       float xd, float yd) {
        int crossed = 0;
        if(x <= 0) {seed = altSeeds[0]; ++crossed;}
        else if(x >= edge) {seed = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {seed = altSeeds[2]; ++crossed;}
        else if(y >= edge) {seed = altSeeds[3]; ++crossed; y -= edge;}
        if(crossed > 1) seed = -crossed;

        final int h = hashAll(x, y, seed);
        final int hash = h & (255 << 1);
        return (h * 0x1p-32f) + xd * GRADIENTS_2D[hash] + yd * GRADIENTS_2D[hash + 1];
    }
    protected float gradCoord3D(int seed, int x, int y, int z,
                                float xd, float yd, float zd) {
        int crossed = 0;
        if(x <= 0) {seed = altSeeds[0]; ++crossed;}
        else if(x >= edge) {seed = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {seed = altSeeds[2]; ++crossed;}
        else if(y >= edge) {seed = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {seed = altSeeds[4]; ++crossed;}
        else if(z >= edge) {seed = altSeeds[5]; ++crossed; z -= edge;}
        if(crossed > 1) seed = -crossed;

        final int h = hashAll(x, y, z, seed);
        final int hash = h & (255 << 2);
        return (h * 0x1p-32f) + xd * GRADIENTS_3D[hash] + yd * GRADIENTS_3D[hash + 1] + zd * GRADIENTS_3D[hash + 2];
    }
    protected float gradCoord4D(int seed, int x, int y, int z, int w,
                                       float xd, float yd, float zd, float wd) {
        int crossed = 0;
        if(x <= 0) {seed = altSeeds[0]; ++crossed;}
        else if(x >= edge) {seed = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {seed = altSeeds[2]; ++crossed;}
        else if(y >= edge) {seed = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {seed = altSeeds[4]; ++crossed;}
        else if(z >= edge) {seed = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {seed = altSeeds[6]; ++crossed;}
        else if(w >= edge) {seed = altSeeds[7]; ++crossed; w -= edge;}
        if(crossed > 1) seed = -crossed;

        final int h = hashAll(x, y, z, w, seed);
        final int hash = h & (255 << 2);
        return (h * 0x1p-32f) + xd * GRADIENTS_4D[hash] + yd * GRADIENTS_4D[hash + 1] + zd * GRADIENTS_4D[hash + 2] + wd * GRADIENTS_4D[hash + 3];
    }
    protected float gradCoord5D(int seed, int x, int y, int z, int w, int u,
                                       float xd, float yd, float zd, float wd, float ud) {
        int crossed = 0;
        if(x <= 0) {seed = altSeeds[0]; ++crossed;}
        else if(x >= edge) {seed = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {seed = altSeeds[2]; ++crossed;}
        else if(y >= edge) {seed = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {seed = altSeeds[4]; ++crossed;}
        else if(z >= edge) {seed = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {seed = altSeeds[6]; ++crossed;}
        else if(w >= edge) {seed = altSeeds[7]; ++crossed; w -= edge;}
        if(u <= 0) {seed = altSeeds[8]; ++crossed;}
        else if(u >= edge) {seed = altSeeds[9]; ++crossed; u -= edge;}
        if(crossed > 1) seed = -crossed;

        final int h = hashAll(x, y, z, w, u, seed);
        final int hash = h & (255 << 3);
        return (h * 0x1p-32f) + xd * GRADIENTS_5D[hash] + yd * GRADIENTS_5D[hash + 1] + zd * GRADIENTS_5D[hash + 2]
                + wd * GRADIENTS_5D[hash + 3] + ud * GRADIENTS_5D[hash + 4];
    }
    protected float gradCoord6D(int seed, int x, int y, int z, int w, int u, int v,
                                       float xd, float yd, float zd, float wd, float ud, float vd) {
        int crossed = 0;
        if(x <= 0) {seed = altSeeds[0]; ++crossed;}
        else if(x >= edge) {seed = altSeeds[1]; ++crossed; x -= edge;}
        if(y <= 0) {seed = altSeeds[2]; ++crossed;}
        else if(y >= edge) {seed = altSeeds[3]; ++crossed; y -= edge;}
        if(z <= 0) {seed = altSeeds[4]; ++crossed;}
        else if(z >= edge) {seed = altSeeds[5]; ++crossed; z -= edge;}
        if(w <= 0) {seed = altSeeds[6]; ++crossed;}
        else if(w >= edge) {seed = altSeeds[7]; ++crossed; w -= edge;}
        if(u <= 0) {seed = altSeeds[8]; ++crossed;}
        else if(u >= edge) {seed = altSeeds[9]; ++crossed; u -= edge;}
        if(v <= 0) {seed = altSeeds[10]; ++crossed;}
        else if(v >= edge) {seed = altSeeds[11]; ++crossed; v -= edge;}
        if(crossed > 1) seed = -crossed;

        final int h = hashAll(x, y, z, w, u, v, seed);
        final int hash = h & (255 << 3);
        return (h * 0x1p-32f) + xd * GRADIENTS_6D[hash] + yd * GRADIENTS_6D[hash + 1] + zd * GRADIENTS_6D[hash + 2]
                + wd * GRADIENTS_6D[hash + 3] + ud * GRADIENTS_6D[hash + 4] + vd * GRADIENTS_6D[hash + 5];
    }

    /**
     * Given inputs as {@code x} in the range -1.0 to 1.0 that are too biased towards 0.0, this "squashes" the range
     * softly to widen it and spread it away from 0.0 without increasing bias anywhere else.
     * <br>
     * This starts with a common sigmoid function, {@code x / sqrt(x * x + add)}, but instead of approaching -1 and 1
     * but never reaching them, this multiplies the result so the line crosses -1 when x is -1, and crosses 1 when x is
     * 1. It has a smooth derivative, if that matters to you.
     *
     * @param x a float between -1 and 1
     * @param add if greater than 1, this will have nearly no effect; the lower this goes below 1, the more this will
     *           separate results near the center of the range. This must be greater than or equal to 0.0
     * @param mul typically the result of calling {@code (float) Math.sqrt(add + 1f)}
     * @return a float with a slightly different distribution from {@code x}, but still between -1 and 1
     */
    public static float equalize(float x, float add, float mul) {
        return x * mul / (float) Math.sqrt(x * x + add);
    }

    @Override
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, final int seed) {
        final int
                x0 = floor(x),
                y0 = floor(y);

        final float xf = x - x0, yf = y - y0;

        final float xa = xf * xf * (3 - (xf + xf));
        final float ya = yf * yf * (3 - (yf + yf));
        return
                equalize(lerp(lerp(gradCoord2D(seed, x0, y0, xf, yf), gradCoord2D(seed, x0+1, y0, xf - 1, yf), xa),
                        lerp(gradCoord2D(seed, x0, y0+1, xf, yf-1), gradCoord2D(seed, x0+1, y0+1, xf - 1, yf - 1), xa),
                        ya) * SCALE2, EQ_ADD_2, EQ_MUL_2);
    }

    @Override
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, final int seed) {
        final int
                x0 = floor(x),
                y0 = floor(y),
                z0 = floor(z);

        final float xf = x - x0, yf = y - y0, zf = z - z0;

        final float xa = xf * xf * (3 - (xf + xf));
        final float ya = yf * yf * (3 - (yf + yf));
        final float za = zf * zf * (3 - (zf + zf));
        return
                equalize(
                        lerp(
                                lerp(
                                        lerp(
                                                gradCoord3D(seed, x0, y0, z0, xf, yf, zf),
                                                gradCoord3D(seed, x0+1, y0, z0, xf - 1, yf, zf),
                                                xa),
                                        lerp(
                                                gradCoord3D(seed, x0, y0+1, z0, xf, yf-1, zf),
                                                gradCoord3D(seed, x0+1, y0+1, z0, xf - 1, yf - 1, zf),
                                                xa),
                                        ya),
                                lerp(
                                        lerp(
                                                gradCoord3D(seed, x0, y0, z0+1, xf, yf, zf-1),
                                                gradCoord3D(seed, x0+1, y0, z0+1, xf - 1, yf, zf-1),
                                                xa),
                                        lerp(
                                                gradCoord3D(seed, x0, y0+1, z0+1, xf, yf-1, zf-1),
                                                gradCoord3D(seed, x0+1, y0+1, z0+1, xf - 1, yf - 1, zf-1),
                                                xa),
                                        ya),
                                za) * SCALE3, EQ_ADD_3, EQ_MUL_3); // 1.0625f
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        final int
                x0 = floor(x),
                y0 = floor(y),
                z0 = floor(z),
                w0 = floor(w);

        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0;

        final float xa = xf * xf * (3 - (xf + xf));
        final float ya = yf * yf * (3 - (yf + yf));
        final float za = zf * zf * (3 - (zf + zf));
        final float wa = wf * wf * (3 - (wf + wf));
        return
                equalize(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0, xf, yf, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0, xf - 1, yf, zf, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0, xf, yf-1, zf, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0, xf - 1, yf - 1, zf, wf),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0, xf, yf, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0, xf - 1, yf, zf-1, wf),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0, xf, yf-1, zf-1, wf),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0, xf - 1, yf - 1, zf-1, wf),
                                                        xa),
                                                ya),
                                        za),
                                lerp(
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0, w0+1, xf, yf, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0, w0+1, xf - 1, yf, zf, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0, w0+1, xf, yf-1, zf, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0, w0+1, xf - 1, yf - 1, zf, wf - 1),
                                                        xa),
                                                ya),
                                        lerp(
                                                lerp(
                                                        gradCoord4D(seed, x0, y0, z0+1, w0+1, xf, yf, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0, z0+1, w0+1, xf - 1, yf, zf-1, wf - 1),
                                                        xa),
                                                lerp(
                                                        gradCoord4D(seed, x0, y0+1, z0+1, w0+1, xf, yf-1, zf-1, wf - 1),
                                                        gradCoord4D(seed, x0+1, y0+1, z0+1, w0+1, xf - 1, yf - 1, zf-1, wf - 1),
                                                        xa),
                                                ya),
                                        za),
                                wa) * SCALE4, EQ_ADD_4, EQ_MUL_4);//0.555f);
    }


    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        final int
                x0 = floor(x),
                y0 = floor(y),
                z0 = floor(z),
                w0 = floor(w),
                u0 = floor(u);

        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0;

        final float xa = xf * xf * (3 - (xf + xf));
        final float ya = yf * yf * (3 - (yf + yf));
        final float za = zf * zf * (3 - (zf + zf));
        final float wa = wf * wf * (3 - (wf + wf));
        final float ua = uf * uf * (3 - (uf + uf));
        return
                equalize(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0, w0, u0, xf, yf, zf, wf, uf),
                                                                gradCoord5D(seed, x0 + 1, y0, z0, w0, u0, xf - 1, yf, zf, wf, uf),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0, w0, u0, xf, yf - 1, zf, wf, uf),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0, w0, u0, xf - 1, yf - 1, zf, wf, uf),
                                                                xa),
                                                        ya),
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0 + 1, w0, u0, xf, yf, zf - 1, wf, uf),
                                                                gradCoord5D(seed, x0 + 1, y0, z0 + 1, w0, u0, xf - 1, yf, zf - 1, wf, uf),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0 + 1, w0, u0, xf, yf - 1, zf - 1, wf, uf),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0, xf - 1, yf - 1, zf - 1, wf, uf),
                                                                xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0, w0 + 1, u0, xf, yf, zf, wf - 1, uf),
                                                                gradCoord5D(seed, x0 + 1, y0, z0, w0 + 1, u0, xf - 1, yf, zf, wf - 1, uf),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0, w0 + 1, u0, xf, yf - 1, zf, wf - 1, uf),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0, xf - 1, yf - 1, zf, wf - 1, uf),
                                                                xa),
                                                        ya),
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0 + 1, w0 + 1, u0, xf, yf, zf - 1, wf - 1, uf),
                                                                gradCoord5D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0, xf - 1, yf, zf - 1, wf - 1, uf),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0, xf, yf - 1, zf - 1, wf - 1, uf),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0, xf - 1, yf - 1, zf - 1, wf - 1, uf),
                                                                xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0, w0, u0 + 1, xf, yf, zf, wf, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0, z0, w0, u0 + 1, xf - 1, yf, zf, wf, uf - 1),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0, w0, u0 + 1, xf, yf - 1, zf, wf, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0, w0, u0 + 1, xf - 1, yf - 1, zf, wf, uf - 1),
                                                                xa),
                                                        ya),
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0 + 1, w0, u0 + 1, xf, yf, zf - 1, wf, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0, z0 + 1, w0, u0 + 1, xf - 1, yf, zf - 1, wf, uf - 1),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0 + 1, w0, u0 + 1, xf, yf - 1, zf - 1, wf, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0 + 1, w0, u0 + 1, xf - 1, yf - 1, zf - 1, wf, uf - 1),
                                                                xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0, w0 + 1, u0 + 1, xf, yf, zf, wf - 1, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0, z0, w0 + 1, u0 + 1, xf - 1, yf, zf, wf - 1, uf - 1),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0, w0 + 1, u0 + 1, xf, yf - 1, zf, wf - 1, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0, w0 + 1, u0 + 1, xf - 1, yf - 1, zf, wf - 1, uf - 1),
                                                                xa),
                                                        ya),
                                                lerp(
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0, z0 + 1, w0 + 1, u0 + 1, xf, yf, zf - 1, wf - 1, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0, z0 + 1, w0 + 1, u0 + 1, xf - 1, yf, zf - 1, wf - 1, uf - 1),
                                                                xa),
                                                        lerp(
                                                                gradCoord5D(seed, x0, y0 + 1, z0 + 1, w0 + 1, u0 + 1, xf, yf - 1, zf - 1, wf - 1, uf - 1),
                                                                gradCoord5D(seed, x0 + 1, y0 + 1, z0 + 1, w0 + 1, u0 + 1, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1),
                                                                xa),
                                                        ya),
                                                za),
                                        wa),
                                ua) * SCALE5, EQ_ADD_5, EQ_MUL_5);//0.7777777f);
    }

    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        final int
                x0 = floor(x),
                y0 = floor(y),
                z0 = floor(z),
                w0 = floor(w),
                u0 = floor(u),
                v0 = floor(v);

        final float xf = x - x0, yf = y - y0, zf = z - z0, wf = w - w0, uf = u - u0, vf = v - v0;
        final float xa = xf * xf * (3 - (xf + xf));
        final float ya = yf * yf * (3 - (yf + yf));
        final float za = zf * zf * (3 - (zf + zf));
        final float wa = wf * wf * (3 - (wf + wf));
        final float ua = uf * uf * (3 - (uf + uf));
        final float va = vf * vf * (3 - (vf + vf));
        return equalize(
                lerp(
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0, xf, yf, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0, v0, xf - 1, yf, zf, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0, v0, xf, yf - 1, zf, wf, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0, v0, xf - 1, yf - 1, zf, wf, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0, v0, xf, yf, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0, v0, xf - 1, yf, zf - 1, wf, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0, v0, xf, yf - 1, zf - 1, wf, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0, v0, xf - 1, yf - 1, zf - 1, wf, uf, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0, v0, xf, yf, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0, v0, xf - 1, yf, zf, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0, v0, xf, yf - 1, zf, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0, v0, xf - 1, yf - 1, zf, wf - 1, uf, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0, v0, xf, yf, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0, v0, xf - 1, yf, zf - 1, wf - 1, uf, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0, v0, xf, yf - 1, zf - 1, wf - 1, uf, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0+1, v0, xf, yf, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0+1, v0, xf - 1, yf, zf, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0+1, v0, xf, yf - 1, zf, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0+1, v0, xf - 1, yf - 1, zf, wf, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0+1, v0, xf, yf, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0+1, v0, xf - 1, yf, zf - 1, wf, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0+1, v0, xf, yf - 1, zf - 1, wf, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0+1, v0, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0+1, v0, xf, yf, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0+1, v0, xf - 1, yf, zf, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0+1, v0, xf, yf - 1, zf, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0+1, v0, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0+1, v0, xf, yf, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0+1, v0, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0+1, v0, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, v0, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        lerp(
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0, v0+1, xf, yf, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0, v0+1, xf - 1, yf, zf, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0, v0+1, xf, yf - 1, zf, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0, v0+1, xf - 1, yf - 1, zf, wf, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0, v0+1, xf, yf, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0, v0+1, xf - 1, yf, zf - 1, wf, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0, v0+1, xf, yf - 1, zf - 1, wf, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0, v0+1, xf - 1, yf - 1, zf - 1, wf, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0, v0+1, xf, yf, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0, v0+1, xf - 1, yf, zf, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0, v0+1, xf, yf - 1, zf, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0, v0+1, xf - 1, yf - 1, zf, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0, v0+1, xf, yf, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0, v0+1, xf - 1, yf, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0, v0+1, xf, yf - 1, zf - 1, wf - 1, uf, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0, v0+1, xf - 1, yf - 1, zf - 1, wf - 1, uf, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                lerp(
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0, u0+1, v0+1, xf, yf, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0, u0+1, v0+1, xf - 1, yf, zf, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0, u0+1, v0+1, xf, yf - 1, zf, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0, u0+1, v0+1, xf - 1, yf - 1, zf, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0, u0+1, v0+1, xf, yf, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0, u0+1, v0+1, xf - 1, yf, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0, u0+1, v0+1, xf, yf - 1, zf - 1, wf, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0, u0+1, v0+1, xf - 1, yf - 1, zf - 1, wf, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        lerp(
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0, w0+1, u0+1, v0+1, xf, yf, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0, w0+1, u0+1, v0+1, xf - 1, yf, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0, w0+1, u0+1, v0+1, xf, yf - 1, zf, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0, w0+1, u0+1, v0+1, xf - 1, yf - 1, zf, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                lerp(
                                                        lerp(gradCoord6D(seed, x0, y0, z0+1, w0+1, u0+1, v0+1, xf, yf, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0, z0+1, w0+1, u0+1, v0+1, xf - 1, yf, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        lerp(gradCoord6D(seed, x0, y0+1, z0+1, w0+1, u0+1, v0+1, xf, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1),
                                                                gradCoord6D(seed, x0+1, y0+1, z0+1, w0+1, u0+1, v0+1, xf - 1, yf - 1, zf - 1, wf - 1, uf - 1, vf - 1), xa),
                                                        ya),
                                                za),
                                        wa),
                                ua),
                        va) * SCALE6, EQ_ADD_6, EQ_MUL_6);//1.61f);
    }

    /**
     * Produces a String that describes everything needed to recreate this RawNoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another RawNoise.
     * @return a String that describes this WangPerlueNoise for serialization
     */
    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this WangPerlueNoise to have the
     * described state from the given String. The serialized String must have been produced by a WangPerlueNoise.
     *
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this WangPerlueNoise, after being modified (if possible)
     */
    @Override
    public WangPerlueNoise stringDeserialize(String data) {
        seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 1));
        return this;
    }

    public static WangPerlueNoise recreateFromString(String data) {
        return new WangPerlueNoise(MathSupport.intFromDec(data, 1, data.indexOf('`', 1)));
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

        WangPerlueNoise that = (WangPerlueNoise) o;

        return (seed == that.seed);
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "WangPerlueNoise{seed=" + seed + '}';
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
