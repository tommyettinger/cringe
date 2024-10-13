/*
 * Licensing ===================================================================
 *
 * Copyright (c) 2021 Olaf Berstein
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.tommyettinger.cringe;

/**
 * An implementation of the Ziggurat method for generating normal-distributed random values. The Ziggurat method is not
 * an approximation, but is faster than some simple approximations while having higher statistical quality. This is not
 * an ideal implementation; it cannot produce as wide of an output range when compared to normal-distributing methods
 * that can use an arbitrarily large supply of random numbers, such as Marsaglia's Polar method. Unlike those methods,
 * this only uses one long as its input. This will randomize its input if it reaches a condition that would normally
 * require the Ziggurat algorithm to generate another random number.
 * <br>
 * Every bit in the input long may be used in some form, but the most important distinctions are between the bottom 8
 * bits, which determine a "box" for where the output could be drawn from, the upper 53 bits, which form into a random
 * double between 0 and 1, and bit 9 (or {@code (state & 512L)}), which is treated as a sign bit. If any of these bit
 * ranges contains some value more often than other values that should be equally likely, it can manifest as an output
 * defect. Further, generating values in the trail takes more time than other values, and that can happen most
 * frequently when bits 0 through 7 of {@code state} are all 0.
 * <br>
 * The range this can produce is at least from -7.6719775673883905 to 7.183851151080583, and is almost certainly larger
 * (only 4 billion distinct inputs were tested, and there are over 18 quintillion inputs possible).
 * <br>
 * The {@link #normal(long)} method can be contrasted with {@link Distributor#normal(long)}; the Ziggurat method does
 * not preserve patterns in the input long when multiple inputs are given, but Distributor will do that. Ziggurat has
 * much better behavior in the "trail," which is approximately the outer 0.1% of possible results, and can be
 * arbitrarily far from 0. Ziggurat is roughly as fast as Distributor and may actually be slightly faster.
 * <br>
 * From <a href="https://github.com/camel-cdr/cauldron/blob/7d5328441b1a1bc8143f627aebafe58b29531cb9/cauldron/random.h#L2013-L2265">Cauldron</a>,
 * MIT-licensed. This in turn is based on Doornik's form of the Ziggurat method:
 * <br>
 *      Doornik, Jurgen A (2005):
 *      "An improved ziggurat method to generate normal random samples."
 *      University of Oxford: 77.
 */
public final class Ziggurat {
    /**
     * Should never be constructed.
     */
    private Ziggurat() {
    }

    private static final int    TABLE_ITEMS = 256;
    private static final double R           = 3.65415288536100716461;
    private static final double INV_R       = 1.0 / R;
    private static final double AREA        = 0.00492867323397465524494;

    /**
     * This is private because it shouldn't ever be changed after assignment, and has nearly no use outside this code.
     */
    private static final double[] TABLE = new double[257];
    static {
        double f = Math.exp(-0.5 * R * R);
        TABLE[0] = AREA / f;
        TABLE[1] = R;

        for (int i = 2; i < TABLE_ITEMS; i++) {
            double xx = Math.log(AREA /
                    TABLE[i - 1] + f);
            TABLE[i] = Math.sqrt(-2 * xx);
            f = Math.exp(xx);
        }

        TABLE[TABLE_ITEMS] = 0.0;
    }

    /**
     * Given a long where all bits are sufficiently (independently) random, this produces a normal-distributed
     * (Gaussian) variable as if by a normal distribution with mean (mu) 0.0 and standard deviation (sigma) 1.0.
     * @param state a long that should be sufficiently random; quasi-random longs may not be enough
     * @return a normal-distributed double with mean (mu) 0.0 and standard deviation (sigma) 1.0
     */
    public static double normal(long state) {
        double x, y, f0, f1, u;
        int idx;

        while (true) {
            /* To minimize calls to the RNG, we use every bit for its own
             * purposes:
             *    - The 53 most significant bits are used to generate
             *      a random floating-point number in the range [0.0,1.0).
             *    - The first to the eighth least significant bits are used
             *      to generate an index in the range [0,256).
             *    - The ninth least significant bit is treated as the sign
             *      bit of the result, unless the result is in the trail.
             *    - If the random variable is in the trail, the state will
             *      be modified instead of generating a new random number.
             *      This could yield lower quality, but variables in the
             *      trail are already rare (1/256 values or fewer).
             *    - If the result is in the trail, the parity of the
             *      complete state is used to randomly set the sign of the
             *      return value.
             */
            idx = (int)(state & (TABLE_ITEMS - 1));
            u = (state >>> 11) * 0x1p-53 * TABLE[idx];

            /* Take a random box from TABLE
             * and get the value of a random x-coordinate inside it.
             * If it's also inside TABLE[idx + 1] we already know to accept
             * this value. */
            if (u < TABLE[idx + 1])
                break;

            /* If our random box is at the bottom, we can't use the lookup
             * table and need to generate a variable for the trail of the
             * normal distribution, as described by Marsaglia in 1964: */
            if (idx == 0) {
                /* If idx is 0, then the bottom 8 bits of state must all be 0,
                 * and u must be on the larger side.
                 * Doing a "proper" mix of state to get a new random state is
                 * not especially fast, but we could do it here with MX3. */
//                state ^= 0xABC98388FB8FAC03L;
//                state ^= state >>> 32;
//                state *= 0xBEA225F9EB34556DL;
//                state ^= state >>> 29;
//                state *= 0xBEA225F9EB34556DL;
//                state ^= state >>> 32;
//                state *= 0xBEA225F9EB34556DL;
//                state ^= state >>> 29;
                do {
                    x = Math.log((((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) + 1L) * 0x1p-53) * INV_R;
                    y = Math.log((((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) + 1L) * 0x1p-53);
                } while (-(y + y) < x * x);
                return (Long.bitCount(state) & 1L) == 0L ?
                        x - R :
                        R - x;
            }

            /* Take a random x-coordinate u in between TABLE[idx] and TABLE[idx+1]
             * and return x if u is inside the normal distribution,
             * otherwise, repeat the entire ziggurat method. */
            y = u * u;
            f0 = Math.exp(-0.5 * (TABLE[idx]     * TABLE[idx]     - y));
            f1 = Math.exp(-0.5 * (TABLE[idx + 1] * TABLE[idx + 1] - y));
            if (f1 + (((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) * 0x1p-53) * (f0 - f1) < 1.0)
                break;
        }
        /* (Zero-indexed ) bits 8, 9, and 10 aren't used in the calculations for idx
         * or u, so we use bit 9 as a sign bit here. */
        return Math.copySign(u, 256L - (state & 512L));
    }
}
