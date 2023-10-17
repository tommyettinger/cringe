/*
 * Copyright (c) 2023 See AUTHORS file.
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
 *
 */

package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.NumberUtils;

/**
 * Static methods that can take any {@code long} as input and produce a very different, but deterministically chosen,
 * number of some type. The simplest of these conceptually is {@link #scramble(long)}, which takes a long and returns
 * a different long (well, almost always different; there may be some value that makes scramble() return its input).
 * You can pass any long to scramble() and can get any long in return. There is also {@link #scrambleBounded(long, int)}
 * when you want an int outer bound (for this, the inner bound is always 0), {@link #scrambleLong(long, long, long)}
 * when you want a larger range of bounded values or need to specify arbitrary inner and outer bounds,
 * {@link #scrambleFloat(long)}/{@link #scrambleDouble(long)} to generate floating-point values between 0 and 1, and
 * {@link #scrambleGaussian(long)} to get a Gaussian-distributed double (a "bell curve"). Patterns in the input long
 * values are very difficult to locate if given only the output, and essentially the only way to detect a pattern in
 * the inputs is to reverse each operation in the specific scramble function and hope you have enough bits to detect any
 * bias present.
 * <br>
 * Each of these uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
 * <a href="https://github.com/jonmaiga/mx3">MX3 was provided here</a> and is public domain.
 */
public final class Scramblers {
    /**
     * No need to instantiate.
     */
    private Scramblers() {
    }

    /**
     * Given a long {@code x}, this randomly scrambles x, so it is (almost always) a very different long.
     * This can take any long and can return any long.
     * <br>
     * It is currently unknown if this has any fixed-points (inputs that produce an identical output), but
     * a step is taken at the start of the function to eliminate one major known fixed-point at 0.
     * <br>
     * This uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
     * @param x any long, to be scrambled
     * @return a scrambled long derived from {@code x}
     */
    public static long scramble(long x) {
        x ^= 0xABC98388FB8FAC03L;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        return x ^ x >>> 29;
    }

    /**
     * Given a long {@code x} and an int {@code bound}, this randomly scrambles x, so it produces an int between 0
     * (inclusive) and bound (exclusive). The bound is permitted to be negative; it is still exclusive then.
     * <br>
     * This uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
     * @param x any long, to be scrambled
     * @param bound the exclusive outer bound
     * @return a scrambled int between 0 (inclusive) and {@code bound} (exclusive) derived from {@code x}
     */
    public static int scrambleBounded(long x, int bound) {
        x ^= 0xABC98388FB8FAC03L;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        return (bound = (int) ((bound * ((x ^ x >>> 29) & 0xFFFFFFFFL)) >> 32)) + (bound >>> 31);
    }


    /**
     * Given a long {@code x} and a bound as two longs, this randomly scrambles x and
     * returns a pseudorandom, uniformly distributed {@code long} value between the
     * specified {@code inner} bound (inclusive) and the specified {@code outer} bound
     * (exclusive). This will work in cases where either bound may be negative,
     * especially if the bounds are unknown or may be user-specified. This method can
     * be useful when the result is cast to int, because if one bound is a very large
     * negative number and the other bound is a very large positive number, the range
     * between the two numbers may be impossible to produce fully with
     * {@link #scrambleBounded(long, int)}.
     *
     * @param x any long, to be scrambled
     * @param inner the inclusive inner bound; may be any long, allowing negative
     * @param outer the exclusive outer bound; may be any long, allowing negative
     * @return a scrambled long between innerBound (inclusive) and outerBound (exclusive)
     */
    public static long scrambleLong (long x, long inner, long outer) {
        if (outer < inner) {
            long t = outer;
            outer = inner + 1L;
            inner = t + 1L;
        }
        final long bound = outer - inner;
        final long randLow = x & 0xFFFFFFFFL;
        final long boundLow = bound & 0xFFFFFFFFL;
        final long randHigh = (x >>> 32);
        final long boundHigh = (bound >>> 32);
        return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
    }

    /**
     * Given a long {@code x}, this randomly scrambles x to get a pseudo-random float.
     * This can take any long, and returns a float between 0 (inclusive) and 1 (exclusive).
     * The floats that this function returns are always multiples of {@code Math.pow(2, -24)}.
     * <br>
     * This uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
     * @param x any long, to be scrambled
     * @return a scrambled float between 0 (inclusive) and 1 (exclusive) derived from {@code x}
     */
    public static float scrambleFloat(long x) {
        x ^= 0xABC98388FB8FAC03L;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        return (x >>> 40) * 0x1p-24f;
    }
    /**
     * Given a long {@code x}, this randomly scrambles x to get a pseudo-random double.
     * This can take any long, and returns a double between 0 (inclusive) and 1 (exclusive).
     * The doubles that this function returns are always multiples of {@code Math.pow(2, -53)}.
     * <br>
     * This uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
     * @param x any long, to be scrambled
     * @return a scrambled double between 0 (inclusive) and 1 (exclusive) derived from {@code x}
     */
    public static double scrambleDouble(long x) {
        x ^= 0xABC98388FB8FAC03L;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        return (x >>> 11 ^ x >>> 40) * 0x1p-53;
    }

    /**
     * Given a long {@code x}, this randomly scrambles x to get a pseudo-random double with a Gaussian distribution
     * (a "bell curve" distribution centered on 0.0 with standard deviation 1.0). This can return double values
     * between -9.155293773112453 and 8.209536145151493, both inclusive. The vast majority of results will be closer
     * to 0.0 than to either of the extreme limits.
     * <br>
     * This uses the MX3 unary hash by Jon Maiga, but XORs the input with 0xABC98388FB8FAC03L before using MX3.
     * It also uses {@link MathSupport#probit(double)} to distribute the output, and internally uses
     * {@link GdxRandom#nextExclusiveDouble()}'s small code to prepare an input for probit().
     * @param x any long
     * @return a Gaussian-distributed double between -9.155293773112453 and 8.209536145151493
     */
    public static double scrambleGaussian(long x) {
        x ^= 0xABC98388FB8FAC03L;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 32;
        x *= 0xBEA225F9EB34556DL;
        x ^= x >>> 29;
        return MathSupport.probit(NumberUtils.longBitsToDouble(1022L - Long.numberOfTrailingZeros(x) << 52 | x >>> 12));

    }
}
