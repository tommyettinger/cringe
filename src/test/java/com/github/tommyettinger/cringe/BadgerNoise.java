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
 * Combined higher-dimensional value noise with simplex noise as one of its axes. Though much like
 * {@link HoneyNoise} on the surface, this looks quite different in practice.
 * This tends to look like simplex noise, but with more mid-range outputs. When used with ridged or billow modes, this
 * has fewer high or low lines than vanilla simplex noise, while keeping the lines' nice shapes. If patterns are
 * noticeable in some simplex noise you generate, this may be a good, similar alternative.
 */
public class BadgerNoise extends RawNoise {
    public int seed;
    public BadgerNoise() {
        this(0x7F4A7C15);
    }
    public BadgerNoise(int seed) {
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
        return "BadgerNoise";
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
        float n = SimplexNoise.noise(x, y, seed); // regular simplex noise call
        n = (ValueNoise.valueNoise(x, y, n * 0.75f, seed) - n) * 0.5f; // uses higher-dim value noise with n
        return n / (float)Math.sqrt(0.33f + n * n); // approach 1 or -1 quickly
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = SimplexNoise.noise(x, y, z, seed);
        n = (ValueNoise.valueNoise(x, y, z, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.30f + n * n);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = SimplexNoise.noise(x, y, z, w, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.27f + n * n);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = SimplexNoise.noise(x, y, z, w, u, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, u, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.24f + n * n);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = SimplexNoise.noise(x, y, z, w, u, v, seed);
        n = (ValueNoise.valueNoise(x, y, z, w, u, v, n * 0.75f, seed) - n) * 0.5f;
        return n / (float)Math.sqrt(0.21f + n * n);
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public BadgerNoise stringDeserialize(String data) {
        if(data == null || data.length() < 3)
            return this;
        this.seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static BadgerNoise recreateFromString(String data) {
        if(data == null || data.length() < 3)
            return null;
        int seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));

        return new BadgerNoise(seed);
    }

    /**
     * Creates a copy of this INoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This just calls a copy constructor.
     *
     * @return a copy of this BadgerNoise
     */
    @Override
    public BadgerNoise copy() {
        return new BadgerNoise(seed);
    }

    @Override
    public String toString() {
        return "BadgerNoise{" +
                "state=" + seed +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BadgerNoise that = (BadgerNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }


}
