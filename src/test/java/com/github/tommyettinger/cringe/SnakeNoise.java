/*
 * Copyright (c) 2023-2022-2024 See AUTHORS file.
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
 * Combines value noise with simplex noise, like {@link HoneyNoise}, but has more sinuous squashing and stretching of
 * its lattice because each axis is run through {@link LineWobble#bicubicWobble(float, long)}. This was developed at
 * about the same time as {@link BadgerNoise}, but BadgerNoise focuses more on having flatter areas of mid-range values,
 * and SnakeNoise focuses on removing any predictable patterns for extreme values present in SimplexNoise. SnakeNoise
 * tends to look like SimplexNoise or HoneyNoise with multiple octaves, while BadgerNoise does not.
 */
public class SnakeNoise extends RawNoise {

    public static final float EQ_ADD = 0.8f/1.75f;

    public static final float EQ_MUL = 1.2071217f;

    public int seed;
    public SnakeNoise() {
        this(0x7F4A7C15);
    }
    public SnakeNoise(int seed) {
        this.seed = seed;
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
    public boolean canUseSeed() {
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
    public String getTag() {
        return "SnakeNoise";
    }

    /**
     * Gets 1D noise with a default or pre-set seed. Most noise algorithms don't behave as well in 1D, so a common
     * approach for implementations is to delegate to one of the LineWobble methods.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 1D noise cannot be produced by this generator
     */
    @Override
    public float getNoise(float x) {
        return LineWobble.wobble(x, seed);
    }

    @Override
    public float getNoise(float x, float y) {
        float n = ValueNoise.valueNoise(x, y, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(n + y, seed^0x11111111) * 0.3f,
                y + LineWobble.bicubicWobble(n + x, seed^0x22222222) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = ValueNoise.valueNoise(x, y, z, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(n + y, seed^0x11111111) * 0.3f,
                y + LineWobble.bicubicWobble(n + z, seed^0x22222222) * 0.3f,
                z + LineWobble.bicubicWobble(n + x, seed^0x33333333) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = ValueNoise.valueNoise(x, y, z, w, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(n + y, seed^0x11111111) * 0.3f,
                y + LineWobble.bicubicWobble(n + z, seed^0x22222222) * 0.3f,
                z + LineWobble.bicubicWobble(n + w, seed^0x33333333) * 0.3f,
                w + LineWobble.bicubicWobble(n + x, seed^0x44444444) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = ValueNoise.valueNoise(x, y, z, w, u, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(n + y, seed^0x11111111) * 0.3f,
                y + LineWobble.bicubicWobble(n + z, seed^0x22222222) * 0.3f,
                z + LineWobble.bicubicWobble(n + w, seed^0x33333333) * 0.3f,
                w + LineWobble.bicubicWobble(n + u, seed^0x44444444) * 0.3f,
                u + LineWobble.bicubicWobble(n + x, seed^0x55555555) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = ValueNoise.valueNoise(x, y, z, w, u, v, seed) * 0.6f;
        n = (SimplexNoise.noise(
                x + LineWobble.bicubicWobble(n + y, seed^0x11111111) * 0.3f,
                y + LineWobble.bicubicWobble(n + z, seed^0x22222222) * 0.3f,
                z + LineWobble.bicubicWobble(n + w, seed^0x33333333) * 0.3f,
                w + LineWobble.bicubicWobble(n + u, seed^0x44444444) * 0.3f,
                u + LineWobble.bicubicWobble(n + v, seed^0x55555555) * 0.3f,
                v + LineWobble.bicubicWobble(n + x, seed^0x66666666) * 0.3f,
                seed) - n) * 0.625f;
        return PerlinNoise.equalize(n, EQ_ADD, EQ_MUL);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public SnakeNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static SnakeNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));

        return new SnakeNoise(seed);
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This just calls a copy constructor.
     *
     * @return a copy of this SnakeNoise
     */
    @Override
    public SnakeNoise copy() {
        return new SnakeNoise(seed);
    }

    @Override
    public String toString() {
        return "SnakeNoise{" +
                "state=" + seed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SnakeNoise that = (SnakeNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }


}
