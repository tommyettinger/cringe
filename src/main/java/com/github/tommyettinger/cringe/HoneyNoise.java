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

import static com.github.tommyettinger.cringe.SimplexNoise.noise;
import static com.github.tommyettinger.cringe.ValueNoise.valueNoise;

/**
 * An INoise implementation that combines and accentuates {@link SimplexNoise} and {@link ValueNoise}.
 */
public class HoneyNoise extends RawNoise {

    public static final HoneyNoise instance = new HoneyNoise();

    public int seed = 0xBEEFD1CE;

    public HoneyNoise() {
    }

    public HoneyNoise(int seed) {
        this.seed = seed;
    }

    @Override
    public String getTag() {
        return "HnyN";
    }

    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public HoneyNoise stringDeserialize(String data) {
        if (data == null || data.length() < 3)
            return this;
        seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static HoneyNoise recreateFromString(String data) {
        if (data == null || data.length() < 3)
            return null;
        int seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return new HoneyNoise(seed);
    }

    @Override
    public HoneyNoise copy() {
        return new HoneyNoise(seed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HoneyNoise that = (HoneyNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "HoneyNoise{" +
                "seed=" + seed +
                '}';
    }

    @Override
    public int getMinDimension() {
        return 2;
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
    public float getNoise(float x, float y) {
        float n = (valueNoise(x, y, seed) + noise(x, y, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        float n = (valueNoise(x, y, z, seed) + noise(x, y, z, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        float n = (valueNoise(x, y, z, w, seed) + noise(x, y, z, w, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        float n = (valueNoise(x, y, z, w, u, seed) + noise(x, y, z, w, u, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        float n = (valueNoise(x, y, z, w, u, v, seed) + noise(x, y, z, w, u, v, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
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
    public float getNoiseWithSeed(float x, float y, int seed) {
        float n = (valueNoise(x, y, seed) + noise(x, y, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        float n = (valueNoise(x, y, z, seed) + noise(x, y, z, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        float n = (valueNoise(x, y, z, w, seed) + noise(x, y, z, w, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        float n = (valueNoise(x, y, z, w, u, seed) + noise(x, y, z, w, u, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        float n = (valueNoise(x, y, z, w, u, v, seed) + noise(x, y, z, w, u, v, seed));
        return n / (0.6f * Math.abs(n) + 0.8f);
    }
}
