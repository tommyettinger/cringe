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

import com.badlogic.gdx.utils.NumberUtils;

import static com.github.tommyettinger.cringe.ValueNoise.valueNoise;

/**
 * A RawNoise type that produces more-natural, less-uniform noise.
 * <br>
 * This is a very-smooth type of noise that can work well using fewer octaves than simplex noise or value noise.
 * It also tends to look similar in lower dimensions and higher dimensions, where some other kinds of noise (such as
 * SimplexNoise) change their appearance quite a bit in 2D vs. 6D.
 * <br>
 * This tends to be a fairly slow noise to generate, but isn't the slowest here. It also tends to look more like a
 * natural "thing" than a geometric shape, even with just one octave.
 */
public class FoamNoise extends RawNoise {

    /**
     * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
     */
    protected int seed;

    public static final FoamNoise instance = new FoamNoise();

    public FoamNoise() {
        this(1234567890);
    }

    public FoamNoise(int seed)
    {
        this.seed = seed;
    }

    // 1D SECTION

    /**
     * Gets 1D noise with using this generator's {@link #getSeed() seed}.
     * Delegates to {@link LineWobble#smoothWobble(float, int)}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x) {
        return LineWobble.smoothWobble(x, seed);
    }

    /**
     * Gets 1D noise with a specific seed.
     * This delegates to {@link LineWobble#smoothWobble(float, int)}.
     *
     * @param x    x position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, int seed) {
        return LineWobble.smoothWobble(x, seed);
    }

    // 2D SECTION

    /**
     * Gets 2D noise using {@link #getSeed()}.
     * @param x x coordinate
     * @param y y coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    /**
     * Gets 2D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param seed can be any int
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        final float p0 = x;
        final float p1 = x * -0.5f + y * 0.8660254037844386f;
        final float p2 = x * -0.5f + y * -0.8660254037844387f;

        float xin = p1;
        float yin = p2;
        final float a = 0.5f * valueNoise(xin, yin, seed);
        xin = p2;
        yin = p0;
        final float b = 0.5f * valueNoise(xin + a, yin, seed ^ 0x2B53AEEB);
        xin = p0;
        yin = p1;
        final float c = 0.5f * valueNoise(xin + b, yin, seed ^ 0xEDB6A8EF);
        final float result = (a + b + c) * 0.3333333333333333f + 0.5f;
        // Barron spline
        final float sharp = 0.75f * 2.2f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final int sign = NumberUtils.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }
    
    // 3D SECTION

    /**
     * Gets 3D noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    /**
     * Gets 3D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param seed can be any int
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        final float p0 = x;
        final float p1 = x * -0.3333333333333333f + y * 0.9428090415820634f;
        final float p2 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * 0.816496580927726f;
        final float p3 = x * -0.3333333333333333f + y * -0.4714045207910317f + z * -0.816496580927726f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        final float a = 0.5f * valueNoise(xin, yin, zin, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        final float b = 0.5f * valueNoise(xin + a, yin, zin, seed ^ 0x2B53AEEB);
        xin = p0;
        yin = p1;
        zin = p3;
        final float c = 0.5f * valueNoise(xin + b, yin, zin, seed ^ 0xEDB6A8EF);
        xin = p0;
        yin = p1;
        zin = p2;
        final float d = 0.5f * valueNoise(xin + c, yin, zin, seed ^ 0x51D00B65);
        final float result = (a + b + c + d) * 0.25f + 0.5f;
        // Barron spline
        final float sharp = 0.75f * 3.3f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final int sign = NumberUtils.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    // 4D SECTION

    /**
     * Gets 4D foam noise using {@link #getSeed()}.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate
     * @return noise between -1 and 1, inclusive
     */
    public float getNoise(final float x, final float y, final float z, final float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    /**
     * Gets 4D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param seed can be any int
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        final float p0 = x;
        final float p1 = x * -0.25f + y *  0.9682458365518543f;
        final float p2 = x * -0.25f + y * -0.3227486121839514f + z * 0.9128709291752769f;
        final float p3 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * 0.7905694150420949f;
        final float p4 = x * -0.25f + y * -0.3227486121839514f + z * -0.45643546458763834f + w * -0.7905694150420947f;
        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        final float a = 0.5f * valueNoise(xin, yin, zin, win, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        final float b = 0.5f * valueNoise(xin + a, yin, zin, win, seed ^ 0x2B53AEEB);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        final float c = 0.5f * valueNoise(xin + b, yin, zin, win, seed ^ 0xEDB6A8EF);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        final float d = 0.5f * valueNoise(xin + c, yin, zin, win, seed ^ 0x51D00B65);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        final float e = 0.5f * valueNoise(xin + d, yin, zin, win, seed ^ 0x6F88983F);

        final float result = (a + b + c + d + e) * 0.2f + 0.5f;
        // Barron spline
        final float sharp = 0.75f * 4.4f; // increase to sharpen, decrease to soften
        final float diff = 0.5f - result;
        final int sign = NumberUtils.floatToIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    // 5D SECTION

    /**
     * Gets 5D noise with {@link #getSeed()}.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    /**
     * Gets 5D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param seed any int
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        final float p0 = x *  0.8157559148337911f + y *  0.5797766823136037f;
        final float p1 = x * -0.7314923478726791f + y *  0.6832997137249108f;
        final float p2 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * 0.9486832980505138f;
        final float p3 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w *   0.8944271909999159f;
        final float p4 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u *  0.7745966692414833f;
        final float p5 = x * -0.0208603044412437f + y * -0.3155296974329846f + z * -0.316227766016838f + w * -0.44721359549995804f + u * -0.7745966692414836f;

        float xin = p1;
        float yin = p2;
        float zin = p3;
        float win = p4;
        float uin = p5;
        final float a = 0.5f * valueNoise(xin, yin, zin, win, uin, seed);
        xin = p0;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p5;
        final float b = 0.5f * valueNoise(xin + a, yin, zin, win, uin, seed ^ 0x2B53AEEB);
        xin = p0;
        yin = p1;
        zin = p3;
        win = p4;
        uin = p5;
        final float c = 0.5f * valueNoise(xin + b, yin, zin, win, uin, seed ^ 0xEDB6A8EF);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p4;
        uin = p5;
        final float d = 0.5f * valueNoise(xin + c, yin, zin, win, uin, seed ^ 0x51D00B65);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p5;
        final float e = 0.5f * valueNoise(xin + d, yin, zin, win, uin, seed ^ 0x6F88983F);
        xin = p0;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        final float f = 0.5f * valueNoise(xin + e, yin, zin, win, uin, seed ^ 0xB7D03F7B);

        final float result = (a + b + c + d + e + f) * 0.16666666666666666f + 0.5f;
        final float sharp = 0.75f * 5.5f;
        final float diff = 0.5f - result;
        final int sign = NumberUtils.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    // 6D SECTION

    /**
     * Gets 6D noise with a default or pre-set seed.
     *
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    /**
     * Gets 6D noise with a specific seed.
     *
     * @param x    x position; can be any finite float
     * @param y    y position; can be any finite float
     * @param z    z position; can be any finite float
     * @param w    w position; can be any finite float
     * @param u    u position; can be any finite float
     * @param v    v position; can be any finite float
     * @param seed any int
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        final float p0 = x;
        final float p1 = x * -0.16666666666666666f + y *  0.98601329718326940f;
        final float p2 = x * -0.16666666666666666f + y * -0.19720265943665383f + z *  0.96609178307929590f;
        final float p3 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w *  0.93541434669348530f;
        final float p4 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u *  0.8819171036881969f;
        final float p5 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v *  0.7637626158259734f;
        final float p6 = x * -0.16666666666666666f + y * -0.19720265943665383f + z * -0.24152294576982394f + w * -0.31180478223116176f + u * -0.4409585518440984f + v * -0.7637626158259732f;
        float xin = p0;
        float yin = p5;
        float zin = p3;
        float win = p6;
        float uin = p1;
        float vin = p4;
        final float a = 0.5f * valueNoise(xin, yin, zin, win, uin, vin, seed);
        xin = p2;
        yin = p6;
        zin = p0;
        win = p4;
        uin = p5;
        vin = p3;
        final float b = 0.5f * valueNoise(xin + a, yin, zin, win, uin, vin, seed ^ 0x2B53AEEB);
        xin = p1;
        yin = p2;
        zin = p3;
        win = p4;
        uin = p6;
        vin = p5;
        final float c = 0.5f * valueNoise(xin + b, yin, zin, win, uin, vin, seed ^ 0xEDB6A8EF);
        xin = p6;
        yin = p0;
        zin = p2;
        win = p5;
        uin = p4;
        vin = p1;
        final float d = 0.5f * valueNoise(xin + c, yin, zin, win, uin, vin, seed ^ 0x51D00B65);
        xin = p2;
        yin = p1;
        zin = p5;
        win = p0;
        uin = p3;
        vin = p6;
        final float e = 0.5f * valueNoise(xin + d, yin, zin, win, uin, vin, seed ^ 0x6F88983F);
        xin = p0;
        yin = p4;
        zin = p6;
        win = p3;
        uin = p1;
        vin = p2;
        final float f = 0.5f * valueNoise(xin + e, yin, zin, win, uin, vin, seed ^ 0xB7D03F7B);
        xin = p5;
        yin = p1;
        zin = p2;
        win = p3;
        uin = p4;
        vin = p0;
        final float g = 0.5f * valueNoise(xin + f, yin, zin, win, uin, vin, seed ^ 0x0B04AB1B);
        final float result = (a + b + c + d + e + f + g) * 0.14285714285714285f + 0.5f;
        final float sharp = 0.75f * 6.6f;
        final float diff = 0.5f - result;
        final int sign = NumberUtils.floatToRawIntBits(diff) >> 31, one = sign | 1;
        return (((result + sign)) / (Float.MIN_VALUE - sign + (result + sharp * diff) * one) - sign - sign) - 1f;
    }

    // OTHER

    /**
     * Returns the String "FoaN", to be used as a unique tag for this generator.
     *
     * @return the String "FoaN"
     */
    @Override
    public String getTag() {
        return "FoamNoise";
    }

    /**
     * Gets the minimum dimension supported by this generator, which is 2.
     *
     * @return the minimum supported dimension, which is 2 inclusive
     */
    @Override
    public int getMinDimension() {
        return 1;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6.
     *
     * @return the maximum supported dimension, which is 6 inclusive
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

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    @Override
    public FoamNoise stringDeserialize(String data) {
        setSeed(MathSupport.intFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static FoamNoise recreateFromString(String data) {
        return new FoamNoise(MathSupport.intFromDec(data, 1, data.length() - 1));
    }

    @Override
    public FoamNoise copy() {
        return new FoamNoise(this.seed);
    }

    @Override
    public String toString() {
        return "FoamNoise{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FoamNoise that = (FoamNoise) o;

        return (seed == that.seed);
    }

    @Override
    public int hashCode() {
        return seed;
    }
}
