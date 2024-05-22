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

import static com.github.tommyettinger.cringe.SimplexNoise.noise;
import static com.github.tommyettinger.cringe.ValueNoise.valueNoise;

/**
 * An INoise implementation that divides space up into cells, and has configurable ways to get values from cells.
 */
public class CellularNoise extends RawNoise {

    public enum NoiseType {
        CELL_VALUE, NOISE_LOOKUP,
        DISTANCE, DISTANCE_2,
        DISTANCE_2_ADD, DISTANCE_2_SUB, DISTANCE_2_MUL, DISTANCE_2_DIV, DISTANCE_VALUE;

        public static final NoiseType[] ALL = values();

    }
    public static final CellularNoise instance = new CellularNoise();

    public int seed = 0xBEEFD1CE;

    public NoiseType noiseType = NoiseType.DISTANCE;

    public CellularNoise() {
    }

    public CellularNoise(int seed) {
        this.seed = seed;
    }

    @Override
    public String getTag() {
        return "CellularNoise";
    }

    @Override
    public CellularNoise copy() {
        return new CellularNoise(seed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellularNoise that = (CellularNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    @Override
    public String toString() {
        return "CellularNoise{" +
                "seed=" + seed +
                '}';
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 2;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
    }

    /**
     * Gets 1D noise with using this generator's {@link #getSeed() seed}.
     * Delegates to {@link LineWobble#bicubicWobble(float, int)}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoise(float x) {
        return LineWobble.bicubicWobble(x, seed);
    }

    @Override
    public float getNoise(float x, float y) {
        return getNoiseWithSeed(x, y, seed);
    }

    @Override
    public float getNoise(float x, float y, float z) {
        return getNoiseWithSeed(x, y, z, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    @Override
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    public NoiseType getCellularReturnType() {
        return noiseType;
    }

    public void setCellularReturnType(NoiseType noiseType) {
        // temporary, to avoid setting to an unhandled type.
        this.noiseType = NoiseType.ALL[(noiseType.ordinal() % 3 + 3) % 3];
    }

    /**
     * Gets 1D noise with using a specific seed.
     * Delegates to {@link LineWobble#bicubicWobble(float, int)}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    @Override
    public float getNoiseWithSeed(float x, int seed) {
        return LineWobble.bicubicWobble(x, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, seed);
            case DISTANCE_VALUE:
            default:
                return mergingCellular(x, y, seed);
        }
    }
    public float basicCellular(float x, float y, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;
        float distance = 999999;
        int xc = 0, yc = 0;
        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                int hash = PointHasher.hash256(xi, yi, seed) << 1;
                float vecX = xi - x + gradients[hash];
                float vecY = yi - y + gradients[hash+1];

                float newDistance = vecX * vecX + vecY * vecY;

                if (newDistance < distance) {
                    distance = newDistance;
                    xc = xi;
                    yc = yi;
                }
            }
        }
        switch (noiseType) {
            case CELL_VALUE:
                return PointHasher.hashAll(xc, yc, seed) * 0x1.0p-31f;

            case NOISE_LOOKUP:
                int hash = PointHasher.hash256(xc, yc, seed) << 1;
                return SimplexNoise.instance.getNoiseWithSeed(xc + gradients[hash], yc + gradients[hash + 1], 123);

            case DISTANCE:
                return Math.min(Math.max(distance - 1, -1), 1);

            default:
                return 0f;
        }
    }

    public float mergingCellular(float x, float y, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);

        float sum = 0f;
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                int hash = PointHasher.hash256(xi, yi, seed) << 1;
                float vecX = xi - x + gradients[hash];
                float vecY = yi - y + gradients[hash + 1];

                float distance = 1f - (vecX * vecX + vecY * vecY);

                if (distance > 0f) {
                    distance *= 3f;
                    sum += ((hash >>> 28) - (hash >>> 24 & 15)) * distance * distance * distance;
                }
            }
        }
        return sum / (64f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        return 0f;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        return 0f;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        return 0f;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        return 0f;
    }
    public String stringSerialize() {
        return "`" + seed + '`';
    }

    public CellularNoise stringDeserialize(String data) {
        if (data == null || data.length() < 3)
            return this;
        seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return this;
    }

    public static CellularNoise recreateFromString(String data) {
        if (data == null || data.length() < 3)
            return null;
        int seed = MathSupport.intFromDec(data, 1, data.indexOf('`', 2));
        return new CellularNoise(seed);
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(seed);
        out.writeInt(noiseType.ordinal());
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setSeed(in.readInt());
        noiseType = NoiseType.ALL[in.readInt()];
    }


}
