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
import com.github.tommyettinger.cringe.CellularNoise.NoiseType;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import static com.github.tommyettinger.cringe.PointHasher.*;

/**
 * An INoise implementation that divides space up into cells, and has configurable ways to get values from cells.
 */
public class CellularExperimentalNoise extends RawNoise {

    public static final CellularExperimentalNoise instance = new CellularExperimentalNoise();

    public int seed = 0xBEEFD1CE;

    public NoiseType noiseType = NoiseType.DISTANCE;

    public CellularExperimentalNoise() {
    }

    public CellularExperimentalNoise(int seed) {
        this.seed = seed;
    }

    public CellularExperimentalNoise(int seed, NoiseType noiseType) {
        this.seed = seed;
        this.noiseType = noiseType;
    }

    public CellularExperimentalNoise(int seed, int noiseTypeIndex) {
        this.seed = seed;
        setNoiseType(noiseTypeIndex);
    }

    @Override
    public String getTag() {
        return "CellularExperimentalNoise";
    }

    @Override
    public CellularExperimentalNoise copy() {
        return new CellularExperimentalNoise(seed, noiseType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CellularExperimentalNoise that = (CellularExperimentalNoise) o;

        return seed == that.seed && noiseType == that.noiseType;
    }

    @Override
    public int hashCode() {
        return seed + noiseType.ordinal();
    }

    @Override
    public String toString() {
        return "CellularExperimentalNoise{" +
                "seed=" + seed +
                ", noiseType=" + noiseType +
                '}';
    }

    @Override
    public int getMinDimension() {
        return 1;
    }

    @Override
    public int getMaxDimension() {
        return 4;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return true;
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

    public NoiseType getNoiseType() {
        return noiseType;
    }

    public void setNoiseType(NoiseType noiseType) {
        this.noiseType = NoiseType.ALL[(noiseType.ordinal() % 9 + 9) % 9];
    }

    public void setNoiseType(int noiseType) {
        this.noiseType = NoiseType.ALL[(noiseType % 9 + 9) % 9];
    }

    public float basicCellular(float x, float y, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);

        int xp = xr * X2;
        int yp = yr * Y2;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;
        float distance = 999999;
        int xc = 0, yc = 0, xpc = 0, ypc = 0;
        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X2) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y2) {
                final int hash = PointHasher.hashPrimed256(xpi, ypi, seed) << 1;
                final float vecX = xi - x + gradients[hash];
                final float vecY = yi - y + gradients[hash+1];

                final float newDistance = vecX * vecX + vecY * vecY;

                if (newDistance < distance) {
                    distance = newDistance;
                    xc = xi;
                    yc = yi;
                    xpc = xpi;
                    ypc = ypi;
                }
            }
        }

        switch (noiseType) {
            case CELL_VALUE:
                return PointHasher.hashPrimedAll(xpc, ypc, seed) * 0x1.0p-31f;

            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, seed);

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
        int xp = xr * X2;
        int yp = yr * Y2;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X2) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y2) {
                final int hash = PointHasher.hashPrimedAll(xpi, ypi, seed);
                final int h = (hash & 255) << 1;
                float vecX = xi - x + gradients[h];
                float vecY = yi - y + gradients[h + 1];

                float distance = 1f - (vecX * vecX + vecY * vecY);

                if (distance > 0f) {
                    sum += ((hash >>> 23) - (hash >>> 14 & 0x1FF)) * distance * distance * distance;
                }
            }
        }
        return sum / (128f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);

        float distance = 999999;
        float distance2 = 999999;
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_2D;

                for (int xi = xr - 1; xi <= xr + 1; xi++) {
                    for (int yi = yr - 1; yi <= yr + 1; yi++) {
                        int hash = PointHasher.hash256(xi, yi, seed) << 1;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];

                        float newDistance = vecX * vecX + vecY * vecY;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    public float basicCellular(float x, float y, float z, int seed) {
        float distance = 999999;
        int xc = 0, yc = 0, zc = 0;
        int xpc = 0, ypc = 0, zpc = 0;

        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);
        int zr = MathUtils.round(z);

        int xp = xr * X3;
        int yp = yr * Y3;
        int zp = zr * Z3;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X3) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y3) {
                for (int zrl = zr + 1, zi = zr - 1, zpi = zp; zi <= zrl; zi++, zpi += Z3) {

                    int hash = PointHasher.hashPrimed256(xpi, ypi, zpi, seed) << 2;
                    float vecX = xi - x + gradients[hash];
                    float vecY = yi - y + gradients[hash + 1];
                    float vecZ = zi - z + gradients[hash + 2];

                    float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                    if (newDistance < distance) {
                        distance = newDistance;
                        xc = xi;
                        yc = yi;
                        zc = zi;
                        xpc = xpi;
                        ypc = ypi;
                        zpc = zpi;
                    }
                }
            }
        }

        switch (noiseType) {
            case CELL_VALUE:
                return PointHasher.hashPrimedAll(xpc, ypc, zpc, seed) * 0x1p-31f;
            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, zc * 0.0625f, seed);
            case DISTANCE:
                return distance - 1;
            default:
                return 0f;
        }
    }
    public float mergingCellular(float x, float y, float z, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);
        int zr = MathUtils.round(z);

        float sum = 0f;

        int xp = xr * X3;
        int yp = yr * Y3;
        int zp = zr * Z3;

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X3) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y3) {
                for (int zrl = zr + 1, zi = zr - 1, zpi = zp; zi <= zrl; zi++, zpi += Z3) {
                    int hash = PointHasher.hashPrimedAll(xpi, ypi, zpi, seed);
                    int h = (hash & 255) << 2;
                    float vecX = xi - x + gradients[h];
                    float vecY = yi - y + gradients[h + 1];
                    float vecZ = zi - z + gradients[h + 2];

                    float distance = 1f - (vecX * vecX + vecY * vecY + vecZ * vecZ);

                    if (distance > 0f) {
                        sum += ((hash >>> 23) - (hash >>> 14 & 0x1FF)) * distance * distance * distance;
                    }
                }
            }
        }
        return sum / (128f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, float z, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);
        int zr = MathUtils.round(z);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_3D;

        float distance = 999999;
        float distance2 = 999999;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    int hash = PointHasher.hash256(xi, yi, zi, seed) << 2;
                    float vecX = xi - x + gradients[hash];
                    float vecY = yi - y + gradients[hash + 1];
                    float vecZ = zi - z + gradients[hash + 2];

                    float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ;

                    distance2 = Math.max(Math.min(distance2, newDistance), distance);
                    distance = Math.min(distance, newDistance);
                }
            }
        }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    public float basicCellular(float x, float y, float z, float w, int seed) {
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;
        float distance = 999999;
        int xc = 0, yc = 0, zc = 0, wc = 0;
        int xpc = 0, ypc = 0, zpc = 0, wpc = 0;

        int xr = MathUtils.round(x), xp = xr * X4;
        int yr = MathUtils.round(y), yp = yr * Y4;
        int zr = MathUtils.round(z), zp = zr * Z4;
        int wr = MathUtils.round(w), wp = wr * W4;

        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X4) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y4) {
                for (int zrl = zr + 1, zi = zr - 1, zpi = zp; zi <= zrl; zi++, zpi += Z4) {
                    for (int wrl = wr + 1, wi = wr - 1, wpi = wp; wi <= wrl; wi++, wpi += W4) {
                        int hash = PointHasher.hashPrimed256(xpi, ypi, zpi, wpi, seed) << 2;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];
                        float vecZ = zi - z + gradients[hash + 2];
                        float vecW = wi - w + gradients[hash + 3];

                        float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW;

                        if (newDistance < distance) {
                            distance = newDistance;
                            xc = xi; xpc = xpi;
                            yc = yi; ypc = ypi;
                            zc = zi; zpc = zpi;
                            wc = wi; wpc = wpi;
                        }
                    }
                }
            }
        }
        switch (noiseType) {
            case CELL_VALUE:
                return PointHasher.hashPrimedAll(xpc, ypc, zpc, wpc, seed) * 0x1p-31f;
            case NOISE_LOOKUP:
                return SimplexNoise.instance.getNoiseWithSeed(xc * 0.0625f, yc * 0.0625f, zc * 0.0625f, wc * 0.0625f, seed);
            case DISTANCE:
                return distance - 1;
            default:
                return 0f;
        }
    }
    public float mergingCellular(float x, float y, float z, float w, int seed) {
        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;
        float sum = 0f;

        int xr = MathUtils.round(x), xp = xr * X4;
        int yr = MathUtils.round(y), yp = yr * Y4;
        int zr = MathUtils.round(z), zp = zr * Z4;
        int wr = MathUtils.round(w), wp = wr * W4;

        for (int xrl = xr + 1, xi = xr - 1, xpi = xp; xi <= xrl; xi++, xpi += X4) {
            for (int yrl = yr + 1, yi = yr - 1, ypi = yp; yi <= yrl; yi++, ypi += Y4) {
                for (int zrl = zr + 1, zi = zr - 1, zpi = zp; zi <= zrl; zi++, zpi += Z4) {
                    for (int wrl = wr + 1, wi = wr - 1, wpi = wp; wi <= wrl; wi++, wpi += W4) {
                        int hash = PointHasher.hashPrimedAll(xpi, ypi, zpi, wpi, seed);
                        int h = (hash & 255) << 2;
                        float vecX = xi - x + gradients[h];
                        float vecY = yi - y + gradients[h + 1];
                        float vecZ = zi - z + gradients[h + 2];
                        float vecW = wi - w + gradients[h + 3];

                        float distance = 1f - (vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW);

                        if (distance > 0f) {
                            sum += ((hash >>> 23) - (hash >>> 14 & 0x1FF)) * distance * distance * distance;
                        }
                    }
                }
            }
        }
        return sum / (128f + Math.abs(sum));
//        return RoughMath.tanhRougher(0x1p-6f * sum);
    }

    public float edgePairCellular(float x, float y, float z, float w, int seed) {
        int xr = MathUtils.round(x);
        int yr = MathUtils.round(y);
        int zr = MathUtils.round(z);
        int wr = MathUtils.round(w);

        final float[] gradients = GradientVectors.CELLULAR_GRADIENTS_4D;

        float distance = 999999;
        float distance2 = 999999;

        for (int xi = xr - 1; xi <= xr + 1; xi++) {
            for (int yi = yr - 1; yi <= yr + 1; yi++) {
                for (int zi = zr - 1; zi <= zr + 1; zi++) {
                    for (int wi = wr - 1; wi <= wr + 1; wi++) {
                        int hash = PointHasher.hash256(xi, yi, zi, wi, seed) << 2;
                        float vecX = xi - x + gradients[hash];
                        float vecY = yi - y + gradients[hash + 1];
                        float vecZ = zi - z + gradients[hash + 2];
                        float vecW = wi - w + gradients[hash + 3];

                        float newDistance = vecX * vecX + vecY * vecY + vecZ * vecZ + vecW * vecW;

                        distance2 = Math.max(Math.min(distance2, newDistance), distance);
                        distance = Math.min(distance, newDistance);
                    }
                }
            }
        }

        switch (noiseType) {
            case DISTANCE_2:
                return distance2 - 1;
            case DISTANCE_2_ADD:
                return Math.min(Math.max(distance2 + distance - 1, -1), 1);
            case DISTANCE_2_SUB:
                return Math.min(Math.max(distance2 - distance - 1, -1), 1);
            case DISTANCE_2_MUL:
                return Math.min(Math.max(distance2 * distance - 1, -1), 1);
            case DISTANCE_2_DIV:
                return Math.min(Math.max(distance / distance2 - 1, -1), 1);
            default:
                return 0;
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, seed);
            default:
                return edgePairCellular(x, y, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, z, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, z, seed);
            default:
                return edgePairCellular(x, y, z, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        switch (noiseType) {
            case CELL_VALUE:
            case NOISE_LOOKUP:
            case DISTANCE:
                return basicCellular(x, y, z, w, seed);
            case DISTANCE_VALUE:
                return mergingCellular(x, y, z, w, seed);
            default:
                return edgePairCellular(x, y, z, w, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        return 0f;
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        return 0f;
    }

    @Override
    public String stringSerialize() {
        return "`" + seed + "~" + noiseType.ordinal() + '`';
    }

    @Override
    public CellularExperimentalNoise stringDeserialize(String data) {
        if (data == null || data.length() < 5)
            return this;
        int middle;
        seed = MathSupport.intFromDec(data, 1, middle = data.indexOf('~', 2));
        int index = MathSupport.intFromDec(data, middle + 1, data.indexOf('`', middle+1));
        setNoiseType(index);
        return this;
    }

    public static CellularExperimentalNoise recreateFromString(String data) {
        if (data == null || data.length() < 5)
            return null;
        int middle;
        int seed = MathSupport.intFromDec(data, 1, middle = data.indexOf('`', 2));
        int index = MathSupport.intFromDec(data, middle + 1, data.indexOf('`', middle+1));
        return new CellularExperimentalNoise(seed, index);
    }

    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(seed);
        out.writeInt(noiseType.ordinal());
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setSeed(in.readInt());
        setNoiseType(in.readInt());
    }

    @Override
    public String toHumanReadableString() {
        return getTag() + " with seed " + getSeed() + " and noise type " + noiseType;
    }
}
