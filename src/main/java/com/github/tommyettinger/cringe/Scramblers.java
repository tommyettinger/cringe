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
 * You can pass any long to scramble() and can get any long in return. There is also {@link #scrambleInt(int)} when you
 * have an int instead of a long as input, and want any int potentially returned, {@link #scrambleBounded(long, int)}
 * when you want an int outer bound (for this, the inner bound is always 0), {@link #scrambleLong(long, long, long)}
 * when you want a larger range of bounded values or need to specify arbitrary inner and outer bounds,
 * {@link #scrambleFloat(long)}/{@link #scrambleDouble(long)} to generate floating-point values between 0 and 1, and
 * {@link #scrambleGaussian(long)} to get a Gaussian-distributed double (a "bell curve"). Patterns in the input long
 * values are very difficult to locate if given only the output, and essentially the only way to detect a pattern in
 * the inputs is to reverse each operation in the specific scramble function and hope you have enough bits to detect any
 * bias present.
 * <br>
 * There are also a few non-cryptographic hashing methods here; these take any long seed and any CharSequence, and
 * return a long. These methods should usually be faster than {@link String#hashCode()} for large String inputs, and are
 * certainly more useful when you only have a {@link StringBuilder}, since it doesn't have any way to get a hash code by
 * the value of its contents (only by its referential identity). They are expected to be much slower on GWT, because all
 * math on long values is so much slower there.
 * <br>
 * Most of the scramble methods uses the MX3 unary hash by Jon Maiga, and XOR the input with 0xABC98388FB8FAC03L before
 * using MX3. <a href="https://github.com/jonmaiga/mx3">MX3 was provided here</a> and is public domain.
 * The {@link #scrambleInt(int)} method uses the
 * <a href="https://github.com/skeeto/hash-prospector#three-round-functions">triple32 unary hash</a>, found by
 * Christopher Wellons' hash-prospector tool. The hash64 methods are based on an early version of wyhash,
 * <a href="https://github.com/wangyi-fudan/wyhash/blob/version_1/wyhash.h">source here</a>,
 * but have diverged significantly. The general style of wyhash has been very influential in the hash64 methods.
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
     * Given an int {@code x}, this randomly scrambles x, so it is (almost always) a very different int.
     * This can take any int and can return any int.
     * <br>
     * It is currently unknown if this has any fixed-points (inputs that produce an identical output), but
     * a step is taken at the start of the function to eliminate one major known fixed-point at 0.
     * <br>
     * This uses the <a href="https://github.com/skeeto/hash-prospector#three-round-functions">triple32 unary hash</a>,
     * but XORs the input with 0xFB8FAC03L before using the hash.
     * @param x any long, to be scrambled
     * @return a scrambled long derived from {@code x}
     */
    public static int scrambleInt(int x) {
        x ^= 0xFB8FAC03;
        x ^= x >>> 17;
        x *= 0xED5AD4BB;
        x ^= x >>> 11;
        x *= 0xAC4C1B51;
        x ^= x >>> 15;
        x *= 0x31848BAB;
        x ^= x >>> 14;
        return x;
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

    /**
     * Big constant 0. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5. Used by {@link #hash64(long, CharSequence)}, and taken from
     * <a href="https://github.com/wangyi-fudan/wyhash>wyhash</a> (in an earlier version).
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits). It also can't
     * really use 128-bit math on the JVM, so there's that, too.
     *
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long mum(final long a, final long b) {
        final long n = a * b;
        return n ^ (n >>> 30);
    }

    /**
     * Gets a 64-bit hash code of the given CharSequence (such as a String) {@code data} in its entirety. The first
     * argument, {@code seed}, allows changing what hashes are produced for the same CharSequence subranges, just by
     * changing the seed.
     *
     * @param seed the seed to use for this hash, as a long
     * @param data  the String or other CharSequence to hash
     * @return a 64-bit hash of data
     */
    public static long hash64(long seed, final CharSequence data) {
        if (data == null) return 0;
        return hash64(seed, data, 0, data.length());
    }

    /**
     * Gets a 64-bit hash code of a subrange of the given CharSequence (such as a String) {@code data} starting
     * at {@code start} and extending for {@code length} chars. The first argument, {@code seed}, allows changing what
     * hashes are produced for the same CharSequence subranges, just by changing the seed.
     *
     * @param seed the seed to use for this hash, as a long
     * @param data  the String or other CharSequence to hash
     * @param start the start index
     * @param length how many items to hash (this will hash fewer if there aren't enough items in the array)
     * @return a 64-bit hash of data
     */
    public static long hash64(long seed, final CharSequence data, final int start, final int length) {
        if (data == null || start < 0 || length < 0 || start >= length)
            return 0;
        final int len = Math.min(length, data.length());
        for (int i = start + 3; i < len; i += 4) {
            seed = mum(
                    mum(data.charAt(i - 3) ^ b1, data.charAt(i - 2) ^ b2) - seed,
                    mum(data.charAt(i - 1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len & 3) {
            case 0:
                seed = mum(b1 - seed, b4 + seed);
                break;
            case 1:
                seed = mum(b5 - seed, b3 ^ (data.charAt(start + len - 1)));
                break;
            case 2:
                seed = mum(data.charAt(start + len - 2) - seed, b0 ^ data.charAt(start + len - 1));
                break;
            case 3:
                seed = mum(data.charAt(start + len - 3) - seed, b2 ^ data.charAt(start + len - 2)) + mum(b5 ^ seed, b4 ^ (data.charAt(start + len - 1)));
                break;
        }
        seed = (seed ^ len) * (seed << 16 ^ b0);
        return (seed ^ (seed << 33 | seed >>> 31) ^ (seed << 19 | seed >>> 45));
    }

}
