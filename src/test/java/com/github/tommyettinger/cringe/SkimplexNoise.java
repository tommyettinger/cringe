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
import static com.github.tommyettinger.cringe.PointHasher.hash256;

/**
 * Simplex noise functions, in 2D, 3D, 4D, 5D, and 6D. This variety scales the result with multiplication by a constant,
 * which isn't always guaranteed to produce a value in the -1 to 1 range for 4D, 5D, or 6D noise. Because this has to
 * scale by a rather small constant in 4D and up, the mostly-mid-range results for those dimensions are run through a
 * gain function that sharpens the result, making high and low values more common than they would otherwise be.
 * <br>
 * Simplex noise is very fast, especially in 2D and 3D, but loses quality somewhat in higher dimensions. It tends to
 * look drastically better if you use it with 2 or more octaves than if you only use 1 octave.
 */
public class SkimplexNoise extends RawNoise {

    public int seed;
    public static final SkimplexNoise instance = new SkimplexNoise();

    public SkimplexNoise() {
        seed = 0x1337BEEF;
    }

    public SkimplexNoise(int seed)
    {
        this.seed = seed;
    }

    @Override
    public float getNoise(float x) {
        return LineWobble.trigWobble(x, seed);
    }
    @Override
    public float getNoise(final float x, final float y) {
        return noise(x, y, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z) {
        return noise(x, y, z, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w) {
        return noise(x, y, z, w, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u) {
        return noise(x, y, z, w, u, seed);
    }
    @Override
    public float getNoise(final float x, final float y, final float z, final float w, final float u, final float v) {
        return noise(x, y, z, w, u, v, seed);
    }

    @Override
    public float getNoiseWithSeed(float x, int seed) {
        return LineWobble.trigWobble(x, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final int seed) {
        return noise(x, y, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final int seed) {
        return noise(x, y, z, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final int seed) {
        return noise(x, y, z, w, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final float u, final int seed) {
        return noise(x, y, z, w, u, seed);
    }
    @Override
    public float getNoiseWithSeed(final float x, final float y, final float z, final float w, final float u, final float v, final int seed) {
        return noise(x, y, z, w, u, v, seed);
    }

    protected static final float F2 = 0.36602540378443864676372317075294f,
            G2 = 0.21132486540518711774542560974902f,
            H2 = G2 * 2.0f,
            F3 = (float)(1.0 / 3.0),
            G3 = (float)(1.0 / 6.0),
            LIMIT3 = 0.6f,
            F4 = (float)((Math.sqrt(5.0) - 1.0) * 0.25),
            G4 = (float)((5.0 - Math.sqrt(5.0)) * 0.05),
            LIMIT4 = 0.62f,
            F5 = (float)((Math.sqrt(6.0) - 1.0) / 5.0),
            G5 = (float)((6.0 - Math.sqrt(6.0)) / 30.0),
            LIMIT5 = 0.7f,
            F6 = (float)((Math.sqrt(7.0) - 1.0) / 6.0),
            G6 = (float)(F6 / (1.0 + 6.0 * F6)),
            LIMIT6 = 0.8375f;

    public static float noise(final float x, final float y, final int seed) {
        final float[] GRADIENTS_2D = GradientVectors.GRADIENTS_2D;

        float t = (x + y) * F2;
        int i = floor(x + t), im = i * X_2;
        int j = floor(y + t), jm = j * Y_2;

        t = (i + j) * G2;
        float X0 = i - t;
        float Y0 = j - t;

        float x0 = x - X0;
        float y0 = y - Y0;

        int i1, j1, im1, jm1;
        if (x0 > y0) {
            i1 = 1;
            j1 = 0;
            im1 = im + X_2;
            jm1 = jm;
        } else {
            i1 = 0;
            j1 = 1;
            im1 = im;
            jm1 = jm + Y_2;
        }

        float x1 = x0 - i1 + G2;
        float y1 = y0 - j1 + G2;
        float x2 = x0 - 1 + H2;
        float y2 = y0 - 1 + H2;

        float vert0, vert1, vert2;

        float t0 = 0.5f - x0 * x0 - y0 * y0;
        if (t0 > 0) {
            t0 *= t0;
            final int h = hash256(im, jm, seed);
            vert0 = t0 * t0 * (x0 * GRADIENTS_2D[h] + y0 * GRADIENTS_2D[h+1]);
        } else vert0 = 0;

        float t1 = 0.5f - x1 * x1 - y1 * y1;
        if (t1 > 0) {
            t1 *= t1;
            final int h = hash256(im1, jm1, seed);
            vert1 = t1 * t1 * (x1 * GRADIENTS_2D[h] + y1 * GRADIENTS_2D[h+1]);
        } else vert1 = 0;

        float t2 = 0.5f - x2 * x2 - y2 * y2;
        if (t2 > 0)  {
            t2 *= t2;
            final int h = hash256(im + X_2, jm + Y_2, seed);
            vert2 = t2 * t2 * (x2 * GRADIENTS_2D[h] + y2 * GRADIENTS_2D[h+1]);
        } else vert2 = 0;

        return (vert0 + vert1 + vert2) * 99.20689070704672f; // this is 99.83685446303647 / 1.00635 ; the first number was found by kdotjpg
    }

    public static float noise(final float x, final float y, final float z, final int seed) {
        final float[] GRADIENTS_3D = GradientVectors.GRADIENTS_3D;

        float t = (x + y + z) * F3;
        int i = floor(x + t), im = i * X_3;
        int j = floor(y + t), jm = j * Y_3;
        int k = floor(z + t), km = k * Z_3;

        t = (i + j + k) * G3;
        float x0 = x - (i - t);
        float y0 = y - (j - t);
        float z0 = z - (k - t);

        int i1, j1, k1, im1, jm1, km1;
        int i2, j2, k2, im2, jm2, km2;

        if (x0 >= y0) {
            if (y0 >= z0) {
                i1 = 1; im1 = im + X_3;
                j1 = 0; jm1 = jm;
                k1 = 0; km1 = km;
                i2 = 1; im2 = im + X_3;
                j2 = 1; jm2 = jm + Y_3;
                k2 = 0; km2 = km;
            } else if (x0 >= z0) {
                i1 = 1; im1 = im + X_3;
                j1 = 0; jm1 = jm;
                k1 = 0; km1 = km;
                i2 = 1; im2 = im + X_3;
                j2 = 0; jm2 = jm;
                k2 = 1; km2 = km + Z_3;
            } else // x0 < z0
            {
                i1 = 0; im1 = im;
                j1 = 0; jm1 = jm;
                k1 = 1; km1 = km + Z_3;
                i2 = 1; im2 = im + X_3;
                j2 = 0; jm2 = jm;
                k2 = 1; km2 = km + Z_3;
            }
        } else // x0 < y0
        {
            if (y0 < z0) {
                i1 = 0; im1 = im;
                j1 = 0; jm1 = jm;
                k1 = 1; km1 = km + Z_3;
                i2 = 0; im2 = im;
                j2 = 1; jm2 = jm + Y_3;
                k2 = 1; km2 = km + Z_3;
            } else if (x0 < z0) {
                i1 = 0; im1 = im;
                j1 = 1; jm1 = jm + Y_3;
                k1 = 0; km1 = km;
                i2 = 0; im2 = im;
                j2 = 1; jm2 = jm + Y_3;
                k2 = 1; km2 = km + Z_3;
            } else // x0 >= z0
            {
                i1 = 0; im1 = im;
                j1 = 1; jm1 = jm + Y_3;
                k1 = 0; km1 = km;
                i2 = 1; im2 = im + X_3;
                j2 = 1; jm2 = jm + Y_3;
                k2 = 0; km2 = km;
            }
        }

        float x1 = x0 - i1 + G3;
        float y1 = y0 - j1 + G3;
        float z1 = z0 - k1 + G3;
        float x2 = x0 - i2 + F3;
        float y2 = y0 - j2 + F3;
        float z2 = z0 - k2 + F3;
        float x3 = x0 - 0.5f;
        float y3 = y0 - 0.5f;
        float z3 = z0 - 0.5f;

        float vert0, vert1, vert2, vert3;

        float t0 = LIMIT3 - x0 * x0 - y0 * y0 - z0 * z0;
        if (t0 > 0) {
            t0 *= t0;
            final int h = hash256(im, jm, km, seed);
            vert0 = t0 * t0 * (x0 * GRADIENTS_3D[h] + y0 * GRADIENTS_3D[h + 1] + z0 * GRADIENTS_3D[h + 2]);
        } else vert0 = 0;

        float t1 = LIMIT3 - x1 * x1 - y1 * y1 - z1 * z1;
        if (t1 > 0) {
            t1 *= t1;
            final int h = hash256(im1, jm1, km1, seed);
            vert1 = t1 * t1 * (x1 * GRADIENTS_3D[h] + y1 * GRADIENTS_3D[h + 1] + z1 * GRADIENTS_3D[h + 2]);
        } else vert1 = 0;

        float t2 = LIMIT3 - x2 * x2 - y2 * y2 - z2 * z2;
        if (t2 > 0) {
            t2 *= t2;
            final int h = hash256(im2, jm2, km2, seed);
            vert2 = t2 * t2 * (x2 * GRADIENTS_3D[h] + y2 * GRADIENTS_3D[h + 1] + z2 * GRADIENTS_3D[h + 2]);
        } else vert2 = 0;

        float t3 = LIMIT3 - x3 * x3 - y3 * y3 - z3 * z3;
        if (t3 > 0)  {
            t3 *= t3;
            final int h = hash256(im + X_3, jm + Y_3, km + Z_3, seed);
            vert3 = t3 * t3 * (x3 * GRADIENTS_3D[h] + y3 * GRADIENTS_3D[h + 1] + z3 * GRADIENTS_3D[h + 2]);
        } else vert3 = 0;

        return 39.59758f * (vert0 + vert1 + vert2 + vert3);
    }

    public static float noise(final float x, final float y, final float z, final float w, final int seed) {
        final float[] GRADIENTS_4D = GradientVectors.GRADIENTS_4D;
        float t = (x + y + z + w) * F4;
        int i = floor(x + t), im = i * X_4;
        int j = floor(y + t), jm = j * Y_4;
        int k = floor(z + t), km = k * Z_4;
        int l = floor(w + t), lm = l * W_4;
        t = (i + j + k + l) * G4;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;

        if (z0 > w0) rankz++; else rankw++;
        // @formatter:on

        int i1 = 2 - rankx >>> 31, im1 = im + (X_4 & -i1);
        int j1 = 2 - ranky >>> 31, jm1 = jm + (Y_4 & -j1);
        int k1 = 2 - rankz >>> 31, km1 = km + (Z_4 & -k1);
        int l1 = 2 - rankw >>> 31, lm1 = lm + (W_4 & -l1);

        int i2 = 1 - rankx >>> 31, im2 = im + (X_4 & -i2);
        int j2 = 1 - ranky >>> 31, jm2 = jm + (Y_4 & -j2);
        int k2 = 1 - rankz >>> 31, km2 = km + (Z_4 & -k2);
        int l2 = 1 - rankw >>> 31, lm2 = lm + (W_4 & -l2);

        int i3 = -rankx >>> 31, im3 = im + (X_4 & -i3);
        int j3 = -ranky >>> 31, jm3 = jm + (Y_4 & -j3);
        int k3 = -rankz >>> 31, km3 = km + (Z_4 & -k3);
        int l3 = -rankw >>> 31, lm3 = lm + (W_4 & -l3);

        float x1 = x0 - i1 + G4;
        float y1 = y0 - j1 + G4;
        float z1 = z0 - k1 + G4;
        float w1 = w0 - l1 + G4;

        float x2 = x0 - i2 + 2 * G4;
        float y2 = y0 - j2 + 2 * G4;
        float z2 = z0 - k2 + 2 * G4;
        float w2 = w0 - l2 + 2 * G4;

        float x3 = x0 - i3 + 3 * G4;
        float y3 = y0 - j3 + 3 * G4;
        float z3 = z0 - k3 + 3 * G4;
        float w3 = w0 - l3 + 3 * G4;

        float x4 = x0 - 1 + 4 * G4;
        float y4 = y0 - 1 + 4 * G4;
        float z4 = z0 - 1 + 4 * G4;
        float w4 = w0 - 1 + 4 * G4;

        float vert0, vert1, vert2, vert3, vert4;
        float t0 = LIMIT4 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0;
        if(t0 > 0) {
            final int h0 = hash256(im, jm, km, lm, seed);
            t0 *= t0;
            vert0 = t0 * t0 * (x0 * GRADIENTS_4D[h0] + y0 * GRADIENTS_4D[h0 + 1] + z0 * GRADIENTS_4D[h0 + 2] + w0 * GRADIENTS_4D[h0 + 3]);
        } else vert0 = 0;
        float t1 = LIMIT4 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1;
        if (t1 > 0) {
            final int h1 = hash256(im1, jm1, km1, lm1, seed);
            t1 *= t1;
            vert1 = t1 * t1 * (x1 * GRADIENTS_4D[h1] + y1 * GRADIENTS_4D[h1 + 1] + z1 * GRADIENTS_4D[h1 + 2] + w1 * GRADIENTS_4D[h1 + 3]);
        } else vert1 = 0;
        float t2 = LIMIT4 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2;
        if (t2 > 0) {
            final int h2 = hash256(im2, jm2, km2, lm2, seed);
            t2 *= t2;
            vert2 = t2 * t2 * (x2 * GRADIENTS_4D[h2] + y2 * GRADIENTS_4D[h2 + 1] + z2 * GRADIENTS_4D[h2 + 2] + w2 * GRADIENTS_4D[h2 + 3]);
        } else vert2 = 0;
        float t3 = LIMIT4 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3;
        if (t3 > 0) {
            final int h3 = hash256(im3, jm3, km3, lm3, seed);
            t3 *= t3;
            vert3 = t3 * t3 * (x3 * GRADIENTS_4D[h3] + y3 * GRADIENTS_4D[h3 + 1] + z3 * GRADIENTS_4D[h3 + 2] + w3 * GRADIENTS_4D[h3 + 3]);
        } else vert3 = 0;
        float t4 = LIMIT4 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4;
        if (t4 > 0) {
            final int h4 = hash256(im + X_4, jm + Y_4, km + Z_4, lm + W_4, seed);
            t4 *= t4;
            vert4 = t4 * t4 * (x4 * GRADIENTS_4D[h4] + y4 * GRADIENTS_4D[h4 + 1] + z4 * GRADIENTS_4D[h4 + 2] + w4 * GRADIENTS_4D[h4 + 3]);
        } else vert4 = 0;

        float n = (vert0 + vert1 + vert2 + vert3 + vert4) * 37.20266f;
        return n / (0.3f * Math.abs(n) + (1f - 0.3f));
    }

    /**
     * Thanks to Mark A. Ropper for
     * <a href="https://computergraphics.stackexchange.com/questions/6408/what-might-be-causing-these-artifacts-in-5d-6d-simplex-noise">this implementation</a>.
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     * @param w w coordinate (4th dimension)
     * @param u u coordinate (5th dimension)
     * @param seed int value that should completely change the noise if it changes even slightly
     * @return a continuous noise value between -1.0 and 1.0, both inclusive
     */
    public static float noise(final float x, final float y, final float z, final float w, final float u, final int seed) {
        final float[] GRADIENTS_5D = GradientVectors.GRADIENTS_5D;
        float n = 0f;
        float t = (x + y + z + w + u) * F5;
        int i = floor(x + t);
        int j = floor(y + t);
        int k = floor(z + t);
        int l = floor(w + t);
        int h = floor(u + t);
        t = (i + j + k + l + h) * G5;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;

        if (w0 > u0) rankw++; else ranku++;
        // @formatter:on

        int i1 = 3 - rankx >>> 31;
        int j1 = 3 - ranky >>> 31;
        int k1 = 3 - rankz >>> 31;
        int l1 = 3 - rankw >>> 31;
        int h1 = 3 - ranku >>> 31;

        int i2 = 2 - rankx >>> 31;
        int j2 = 2 - ranky >>> 31;
        int k2 = 2 - rankz >>> 31;
        int l2 = 2 - rankw >>> 31;
        int h2 = 2 - ranku >>> 31;

        int i3 = 1 - rankx >>> 31;
        int j3 = 1 - ranky >>> 31;
        int k3 = 1 - rankz >>> 31;
        int l3 = 1 - rankw >>> 31;
        int h3 = 1 - ranku >>> 31;

        int i4 = -rankx >>> 31;
        int j4 = -ranky >>> 31;
        int k4 = -rankz >>> 31;
        int l4 = -rankw >>> 31;
        int h4 = -ranku >>> 31;

        float x1 = x0 - i1 + G5;
        float y1 = y0 - j1 + G5;
        float z1 = z0 - k1 + G5;
        float w1 = w0 - l1 + G5;
        float u1 = u0 - h1 + G5;

        float x2 = x0 - i2 + 2 * G5;
        float y2 = y0 - j2 + 2 * G5;
        float z2 = z0 - k2 + 2 * G5;
        float w2 = w0 - l2 + 2 * G5;
        float u2 = u0 - h2 + 2 * G5;

        float x3 = x0 - i3 + 3 * G5;
        float y3 = y0 - j3 + 3 * G5;
        float z3 = z0 - k3 + 3 * G5;
        float w3 = w0 - l3 + 3 * G5;
        float u3 = u0 - h3 + 3 * G5;

        float x4 = x0 - i4 + 4 * G5;
        float y4 = y0 - j4 + 4 * G5;
        float z4 = z0 - k4 + 4 * G5;
        float w4 = w0 - l4 + 4 * G5;
        float u4 = u0 - h4 + 4 * G5;

        float x5 = x0 - 1 + 5 * G5;
        float y5 = y0 - 1 + 5 * G5;
        float z5 = z0 - 1 + 5 * G5;
        float w5 = w0 - 1 + 5 * G5;
        float u5 = u0 - 1 + 5 * G5;

        t = LIMIT5 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i, j, k, l, h, seed) << 3;
            n += t * t * (x0 * GRADIENTS_5D[hash] + y0 * GRADIENTS_5D[hash + 1] + z0 * GRADIENTS_5D[hash + 2] + w0 * GRADIENTS_5D[hash + 3] + u0 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, seed) << 3;
            n += t * t * (x1 * GRADIENTS_5D[hash] + y1 * GRADIENTS_5D[hash + 1] + z1 * GRADIENTS_5D[hash + 2] + w1 * GRADIENTS_5D[hash + 3] + u1 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, seed) << 3;
            n += t * t * (x2 * GRADIENTS_5D[hash] + y2 * GRADIENTS_5D[hash + 1] + z2 * GRADIENTS_5D[hash + 2] + w2 * GRADIENTS_5D[hash + 3] + u2 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, seed) << 3;
            n += t * t * (x3 * GRADIENTS_5D[hash] + y3 * GRADIENTS_5D[hash + 1] + z3 * GRADIENTS_5D[hash + 2] + w3 * GRADIENTS_5D[hash + 3] + u3 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, seed) << 3;
            n += t * t * (x4 * GRADIENTS_5D[hash] + y4 * GRADIENTS_5D[hash + 1] + z4 * GRADIENTS_5D[hash + 2] + w4 * GRADIENTS_5D[hash + 3] + u4 * GRADIENTS_5D[hash + 4]);
        }

        t = LIMIT5 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5;
        if (t > 0) {
            t *= t;
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, seed) << 3;
            n += t * t * (x5 * GRADIENTS_5D[hash] + y5 * GRADIENTS_5D[hash + 1] + z5 * GRADIENTS_5D[hash + 2] + w5 * GRADIENTS_5D[hash + 3] + u5 * GRADIENTS_5D[hash + 4]);
        }

        n *= 20.0f;
        return n / (0.5f * Math.abs(n) + (1f - 0.5f));
    }

    public static float noise(final float x, final float y, final float z,
                              final float w, final float u, final float v, final int seed) {
        final float[] GRADIENTS_6D = GradientVectors.GRADIENTS_6D;
        float n0, n1, n2, n3, n4, n5, n6, n = 0f;
        float t = (x + y + z + w + u + v) * F6;
        int i = floor(x + t);
        int j = floor(y + t);
        int k = floor(z + t);
        int l = floor(w + t);
        int h = floor(u + t);
        int g = floor(v + t);
        t = (i + j + k + l + h + g) * G6;
        float X0 = i - t;
        float Y0 = j - t;
        float Z0 = k - t;
        float W0 = l - t;
        float U0 = h - t;
        float V0 = g - t;
        float x0 = x - X0;
        float y0 = y - Y0;
        float z0 = z - Z0;
        float w0 = w - W0;
        float u0 = u - U0;
        float v0 = v - V0;

        int rankx = 0;
        int ranky = 0;
        int rankz = 0;
        int rankw = 0;
        int ranku = 0;
        int rankv = 0;

        // @formatter:off
        if (x0 > y0) rankx++; else ranky++;
        if (x0 > z0) rankx++; else rankz++;
        if (x0 > w0) rankx++; else rankw++;
        if (x0 > u0) rankx++; else ranku++;
        if (x0 > v0) rankx++; else rankv++;

        if (y0 > z0) ranky++; else rankz++;
        if (y0 > w0) ranky++; else rankw++;
        if (y0 > u0) ranky++; else ranku++;
        if (y0 > v0) ranky++; else rankv++;

        if (z0 > w0) rankz++; else rankw++;
        if (z0 > u0) rankz++; else ranku++;
        if (z0 > v0) rankz++; else rankv++;

        if (w0 > u0) rankw++; else ranku++;
        if (w0 > v0) rankw++; else rankv++;

        if (u0 > v0) ranku++; else rankv++;
        // @formatter:on

        int i1 = 4 - rankx >>> 31;
        int j1 = 4 - ranky >>> 31;
        int k1 = 4 - rankz >>> 31;
        int l1 = 4 - rankw >>> 31;
        int h1 = 4 - ranku >>> 31;
        int g1 = 4 - rankv >>> 31;

        int i2 = 3 - rankx >>> 31;
        int j2 = 3 - ranky >>> 31;
        int k2 = 3 - rankz >>> 31;
        int l2 = 3 - rankw >>> 31;
        int h2 = 3 - ranku >>> 31;
        int g2 = 3 - rankv >>> 31;

        int i3 = 2 - rankx >>> 31;
        int j3 = 2 - ranky >>> 31;
        int k3 = 2 - rankz >>> 31;
        int l3 = 2 - rankw >>> 31;
        int h3 = 2 - ranku >>> 31;
        int g3 = 2 - rankv >>> 31;

        int i4 = 1 - rankx >>> 31;
        int j4 = 1 - ranky >>> 31;
        int k4 = 1 - rankz >>> 31;
        int l4 = 1 - rankw >>> 31;
        int h4 = 1 - ranku >>> 31;
        int g4 = 1 - rankv >>> 31;

        int i5 = -rankx >>> 31;
        int j5 = -ranky >>> 31;
        int k5 = -rankz >>> 31;
        int l5 = -rankw >>> 31;
        int h5 = -ranku >>> 31;
        int g5 = -rankv >>> 31;

        float x1 = x0 - i1 + G6;
        float y1 = y0 - j1 + G6;
        float z1 = z0 - k1 + G6;
        float w1 = w0 - l1 + G6;
        float u1 = u0 - h1 + G6;
        float v1 = v0 - g1 + G6;

        float x2 = x0 - i2 + 2 * G6;
        float y2 = y0 - j2 + 2 * G6;
        float z2 = z0 - k2 + 2 * G6;
        float w2 = w0 - l2 + 2 * G6;
        float u2 = u0 - h2 + 2 * G6;
        float v2 = v0 - g2 + 2 * G6;

        float x3 = x0 - i3 + 3 * G6;
        float y3 = y0 - j3 + 3 * G6;
        float z3 = z0 - k3 + 3 * G6;
        float w3 = w0 - l3 + 3 * G6;
        float u3 = u0 - h3 + 3 * G6;
        float v3 = v0 - g3 + 3 * G6;

        float x4 = x0 - i4 + 4 * G6;
        float y4 = y0 - j4 + 4 * G6;
        float z4 = z0 - k4 + 4 * G6;
        float w4 = w0 - l4 + 4 * G6;
        float u4 = u0 - h4 + 4 * G6;
        float v4 = v0 - g4 + 4 * G6;

        float x5 = x0 - i5 + 5 * G6;
        float y5 = y0 - j5 + 5 * G6;
        float z5 = z0 - k5 + 5 * G6;
        float w5 = w0 - l5 + 5 * G6;
        float u5 = u0 - h5 + 5 * G6;
        float v5 = v0 - g5 + 5 * G6;

        float x6 = x0 - 1 + 6 * G6;
        float y6 = y0 - 1 + 6 * G6;
        float z6 = z0 - 1 + 6 * G6;
        float w6 = w0 - 1 + 6 * G6;
        float u6 = u0 - 1 + 6 * G6;
        float v6 = v0 - 1 + 6 * G6;

        n0 = LIMIT6 - x0 * x0 - y0 * y0 - z0 * z0 - w0 * w0 - u0 * u0 - v0 * v0;
        if (n0 > 0.0f) {
            final int hash = hash256(i, j, k, l, h, g, seed) << 3;
            n0 *= n0;
            n += n0 * n0 * (GRADIENTS_6D[hash] * x0 + GRADIENTS_6D[hash + 1] * y0 + GRADIENTS_6D[hash + 2] * z0 +
                    GRADIENTS_6D[hash + 3] * w0 + GRADIENTS_6D[hash + 4] * u0 + GRADIENTS_6D[hash + 5] * v0);
        }

        n1 = LIMIT6 - x1 * x1 - y1 * y1 - z1 * z1 - w1 * w1 - u1 * u1 - v1 * v1;
        if (n1 > 0.0f) {
            final int hash = hash256(i + i1, j + j1, k + k1, l + l1, h + h1, g + g1, seed) << 3;
            n1 *= n1;
            n += n1 * n1 * (GRADIENTS_6D[hash] * x1 + GRADIENTS_6D[hash + 1] * y1 + GRADIENTS_6D[hash + 2] * z1 +
                    GRADIENTS_6D[hash + 3] * w1 + GRADIENTS_6D[hash + 4] * u1 + GRADIENTS_6D[hash + 5] * v1);
        }

        n2 = LIMIT6 - x2 * x2 - y2 * y2 - z2 * z2 - w2 * w2 - u2 * u2 - v2 * v2;
        if (n2 > 0.0f) {
            final int hash = hash256(i + i2, j + j2, k + k2, l + l2, h + h2, g + g2, seed) << 3;
            n2 *= n2;
            n += n2 * n2 * (GRADIENTS_6D[hash] * x2 + GRADIENTS_6D[hash + 1] * y2 + GRADIENTS_6D[hash + 2] * z2 +
                    GRADIENTS_6D[hash + 3] * w2 + GRADIENTS_6D[hash + 4] * u2 + GRADIENTS_6D[hash + 5] * v2);
        }

        n3 = LIMIT6 - x3 * x3 - y3 * y3 - z3 * z3 - w3 * w3 - u3 * u3 - v3 * v3;
        if (n3 > 0.0f) {
            final int hash = hash256(i + i3, j + j3, k + k3, l + l3, h + h3, g + g3, seed) << 3;
            n3 *= n3;
            n += n3 * n3 * (GRADIENTS_6D[hash] * x3 + GRADIENTS_6D[hash + 1] * y3 + GRADIENTS_6D[hash + 2] * z3 +
                    GRADIENTS_6D[hash + 3] * w3 + GRADIENTS_6D[hash + 4] * u3 + GRADIENTS_6D[hash + 5] * v3);
        }

        n4 = LIMIT6 - x4 * x4 - y4 * y4 - z4 * z4 - w4 * w4 - u4 * u4 - v4 * v4;
        if (n4 > 0.0f) {
            final int hash = hash256(i + i4, j + j4, k + k4, l + l4, h + h4, g + g4, seed) << 3;
            n4 *= n4;
            n += n4 * n4 * (GRADIENTS_6D[hash] * x4 + GRADIENTS_6D[hash + 1] * y4 + GRADIENTS_6D[hash + 2] * z4 +
                    GRADIENTS_6D[hash + 3] * w4 + GRADIENTS_6D[hash + 4] * u4 + GRADIENTS_6D[hash + 5] * v4);
        }

        n5 = LIMIT6 - x5 * x5 - y5 * y5 - z5 * z5 - w5 * w5 - u5 * u5 - v5 * v5;
        if (n5 > 0.0f) {
            final int hash = hash256(i + i5, j + j5, k + k5, l + l5, h + h5, g + g5, seed) << 3;
            n5 *= n5;
            n += n5 * n5 * (GRADIENTS_6D[hash] * x5 + GRADIENTS_6D[hash + 1] * y5 + GRADIENTS_6D[hash + 2] * z5 +
                    GRADIENTS_6D[hash + 3] * w5 + GRADIENTS_6D[hash + 4] * u5 + GRADIENTS_6D[hash + 5] * v5);
        }

        n6 = LIMIT6 - x6 * x6 - y6 * y6 - z6 * z6 - w6 * w6 - u6 * u6 - v6 * v6;
        if (n6 > 0.0f) {
            final int hash = hash256(i + 1, j + 1, k + 1, l + 1, h + 1, g + 1, seed) << 3;
            n6 *= n6;
            n += n6 * n6 * (GRADIENTS_6D[hash] * x6 + GRADIENTS_6D[hash + 1] * y6 + GRADIENTS_6D[hash + 2] * z6 +
                    GRADIENTS_6D[hash + 3] * w6 + GRADIENTS_6D[hash + 4] * u6 + GRADIENTS_6D[hash + 5] * v6);
        }

        n *= 7.499f;
        return n / (0.7f * Math.abs(n) + (1f - 0.7f));
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
    public void setSeed(int seed) {
        this.seed = seed;
    }

    @Override
    public int getSeed() {
        return seed;
    }

    @Override
    public String getTag() {
        return "SkimplexNoise";
    }

    public String stringSerialize() {
        return "`" + seed + "`";
    }

    public SkimplexNoise stringDeserialize(String data) {
        seed = (MathSupport.intFromDec(data, 1, data.length() - 1));
        return this;
    }

    public static SkimplexNoise recreateFromString(String data) {
        return new SkimplexNoise(MathSupport.intFromDec(data, 1, data.length() - 1));
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
    public SkimplexNoise copy() {
        return new SkimplexNoise(seed);
    }

    @Override
    public String toString() {
        return "SimplexNoise{seed=" + seed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SkimplexNoise that = (SkimplexNoise) o;

        return seed == that.seed;
    }

    @Override
    public int hashCode() {
        return seed;
    }

    /**
     * An 8-bit point hash that needs 2 dimensions pre-multiplied by constants {@link #X_2} and {@link #Y_2}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_2}
     * @param y y position, as an int pre-multiplied by {@link #Y_2}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y point with the given state s, shifted for {@link GradientVectors#GRADIENTS_2D}
     */
    public static int hash256(int x, int y, int s) {
        final int h = (s ^ x ^ y) * 0x125493;
        return (h ^ h >>> 23) & (255 << 1);
    }
    /**
     * An 8-bit point hash that needs 3 dimensions pre-multiplied by constants {@link #X_3} through {@link #Z_3}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_3}
     * @param y y position, as an int pre-multiplied by {@link #Y_3}
     * @param z z position, as an int pre-multiplied by {@link #Z_3}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z point with the given state s, shifted for {@link GradientVectors#GRADIENTS_3D}
     */
    public static int hash256(int x, int y, int z, int s) {
        final int h = (s ^ x ^ y ^ z) * 0x125493;
        return (h ^ h >>> 22) & (255 << 2);
    }

    /**
     * An 8-bit point hash that needs 4 dimensions pre-multiplied by constants {@link #X_4} through {@link #W_4}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_4}
     * @param y y position, as an int pre-multiplied by {@link #Y_4}
     * @param z z position, as an int pre-multiplied by {@link #Z_4}
     * @param w w position, as an int pre-multiplied by {@link #W_4}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w point with the given state s, shifted for {@link GradientVectors#GRADIENTS_4D}
     */
    public static int hash256(int x, int y, int z, int w, int s) {
        final int h = (s ^ x ^ y ^ z ^ w) * 0x125493;
        return (h ^ h >>> 22) & (255 << 2);
    }
    /**
     * An 8-bit point hash that needs 5 dimensions pre-multiplied by constants {@link #X_5} through {@link #U_5}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_5}
     * @param y y position, as an int pre-multiplied by {@link #Y_5}
     * @param z z position, as an int pre-multiplied by {@link #Z_5}
     * @param w w position, as an int pre-multiplied by {@link #W_5}
     * @param u u position, as an int pre-multiplied by {@link #U_5}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int s) {
        s ^= x ^ y ^ z ^ w ^ u;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27)) * 0x125493 >>> 24;
    }

    /**
     * An 8-bit point hash that needs 6 dimensions pre-multiplied by constants {@link #X_6} through {@link #V_6}, as
     * well as an int seed.
     * @param x x position, as an int pre-multiplied by {@link #X_6}
     * @param y y position, as an int pre-multiplied by {@link #Y_6}
     * @param z z position, as an int pre-multiplied by {@link #Z_6}
     * @param w w position, as an int pre-multiplied by {@link #W_6}
     * @param u u position, as an int pre-multiplied by {@link #U_6}
     * @param v v position, as an int pre-multiplied by {@link #V_6}
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int v, int s) {
        s ^= x ^ y ^ z ^ w ^ u ^ v;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27)) * 0x125493 >>> 24;
    }
    
    public static final int X_2 = 0x1827F5, Y_2 = 0x123C21;
    public static final int X_3 = 0x1A36A9, Y_3 = 0x157931, Z_3 = 0x119725;
    public static final int X_4 = 0x1B69E1, Y_4 = 0x177C0B, Z_4 = 0x141E5D, W_4 = 0x113C31;
    public static final int X_5 = 0x1C3361, Y_5 = 0x18DA39, Z_5 = 0x15E6DB, W_5 = 0x134D29, U_5 = 0x110281;
    public static final int X_6 = 0x1CC1C5, Y_6 = 0x19D7AF, Z_6 = 0x173935, W_6 = 0x14DEAF, U_6 = 0x12C139, V_6 = 0x10DAA3;

}