package com.github.tommyettinger.cringe;

/**
 * Different methods for distributing input {@code long} or {@code double} values from a given domain into specific
 * distributions, such as the normal distribution. {@link #probitD(double)} and {@link #probitF(float)} take a number in
 * the 0.0 to 1.0 range (typically exclusive, but not required to be), and produce a normal-distributed number centered
 * on 0.0 with standard deviation 1.0, using an algorithm by Voutier. The float and double versions also take inputs in
 * the 0.0 to 1.0 range, but the int and long versions {@link #probitL(long)} and {@link #probitI(int)} can take any int
 * or any long, with the lowest values mapping to the lowest results, highest to highest, near 0 to near 0, etc. Using
 * the suffixed probit() methods, such as {@link #probitF(float)}, is recommended when generating normal-distributed
 * floats or doubles if you want to keep the mapping of low to low and high to high.
 * <br>
 * All of these ways so far will preserve patterns in the input, so inputs close to the lowest
 * possible input (0.0 for probit(), {@link Long#MIN_VALUE} for normal(), {@link Integer#MIN_VALUE} for normalF()) will
 * produce the lowest possible output (-26.48372928592822 for probitD() and probitL(), or -9.080134 for probitF() and
 * probitI()), and similarly for the highest possible inputs producing the highest possible outputs.
 * <br>
 * There's also {@link #normal(long)}, which uses the
 * <a href="https://en.wikipedia.org/wiki/Ziggurat_algorithm">Ziggurat method</a> and does not preserve input patterns.
 * {@link #normal(long)} is a little faster than {@link #probitL(long)}, but the Ziggurat-based normal() might have some
 * small quality issues with some inputs that have patterns, like counters.
 */
public final class Distributor {

    private Distributor() {}
    private static final int    ZIG_TABLE_ITEMS = 256;
    private static final double R               = 3.65415288536100716461;
    private static final double INV_R           = 1.0 / R;
    private static final double AREA            = 0.00492867323397465524494;
    private static final double[] ZIG_TABLE  = new double[257];

    static {
        double f = Math.exp(-0.5 * R * R);
        ZIG_TABLE[0] = AREA / f;
        ZIG_TABLE[1] = R;
        for (int i = 2; i < ZIG_TABLE_ITEMS; i++) {
            double xx = Math.log(AREA /
                    ZIG_TABLE[i - 1] + f);
            ZIG_TABLE[i] = Math.sqrt(-2 * xx);
            f = Math.exp(xx);
        }
        ZIG_TABLE[ZIG_TABLE_ITEMS] = 0.0;
    }

    // constants used by probitI() and probitF()
    private static final float
            a0f = 0.195740115269792f,
            a1f = -0.652871358365296f,
            a2f = 1.246899760652504f,
            b0f = 0.155331081623168f,
            b1f = -0.839293158122257f,
            c3f = -1.000182518730158122f,
            c0f = 16.682320830719986527f,
            c1f = 4.120411523939115059f,
            c2f = 0.029814187308200211f,
            d0f = 7.173787663925508066f,
            d1f = 8.759693508958633869f;


    // constants used by probitL() and probitD()
    private static final double
            a0 = 0.195740115269792,
            a1 = -0.652871358365296,
            a2 = 1.246899760652504,
            b0 = 0.155331081623168,
            b1 = -0.839293158122257,
            c3 = -1.000182518730158122,
            c0 = 16.682320830719986527,
            c1 = 4.120411523939115059,
            c2 = 0.029814187308200211,
            d0 = 7.173787663925508066,
            d1 = 8.759693508958633869;

    /**
     * A single-precision probit() approximation that takes a float between 0 and 1 inclusive and returns an
     * approximately-Gaussian-distributed float between -9.080134 and 9.080134 .
     * The function maps the lowest inputs to the most negative outputs, the highest inputs to the most
     * positive outputs, and inputs near 0.5 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param p should be between 0 and 1, inclusive.
     * @return an approximately-Gaussian-distributed float between -9.080134 and 9.080134
     */
    public static float probitF(float p) {
        if(0.0465f > p){
            float r = (float)Math.sqrt(logRough(1f / (p * p)));
            return c3f * r + c2f + (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else if(0.9535f < p) {
            float q = 1f - p, r = (float)Math.sqrt(logRough(1f / (q * q)));
            return -c3f * r - c2f - (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else {
            float q = p - 0.5f, r = q * q;
            return q * (a2f + (a1f * r + a0f) / (r * (r + b1f) + b0f));
        }
    }

    /**
     * A double-precision probit() approximation that takes a double between 0 and 1 inclusive and returns an
     * approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822 .
     * The function maps the lowest inputs to the most negative outputs, the highest inputs to the most
     * positive outputs, and inputs near 0.5 to outputs near 0.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param p should be between 0 and 1, inclusive.
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     */
    public static double probitD(double p) {
        if(0.0465 > p){
            double r = Math.sqrt(Math.log(1.0 / (p * p + 5.56268464626801E-309)));
            return c3 * r + c2 + (c1 * r + c0) / (r * (r + d1) + d0);
        } else if(0.9535 < p) {
            double q = 1.0 - p, r = Math.sqrt(Math.log(1.0 / (q * q + 5.56268464626801E-309)));
            return -c3 * r - c2 - (c1 * r + c0) / (r * (r + d1) + d0);
        } else {
            double q = p - 0.5, r = q * q;
            return q * (a2 + (a1 * r + a0) / (r * (r + b1) + b0));
        }
    }

    /**
     * A single-precision probit() approximation that takes any int and returns an
     * approximately-Gaussian-distributed float between -9.080134 and 9.080134 .
     * The function maps the most negative inputs to the most negative outputs, the most positive inputs to the most
     * positive outputs, and inputs near 0 to outputs near 0. This does not consider the bottom 8 bits of {@code i}.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param i may be any int, though very close ints will not produce different results
     * @return an approximately-Gaussian-distributed float between -9.080134 and 9.080134
     */
    public static float probitI(int i) {
        /* 2.3283064E-10f is 0x1p-32f */
        final float p = 2.3283064E-10f * i + 0.5f;
        if(0.0465f > p){
            float r = (float)Math.sqrt(logRough(1f / (p * p)));
            return c3f * r + c2f + (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else if(0.9535f < p) {
            float q = 1f - p, r = (float)Math.sqrt(logRough(1f / (q * q)));
            return -c3f * r - c2f - (c1f * r + c0f) / (r * (r + d1f) + d0f);
        } else {
            float q = p - 0.5f, r = q * q;
            return q * (a2f + (a1f * r + a0f) / (r * (r + b1f) + b0f));
        }
    }

    /**
     * A double-precision probit() approximation that takes any long and returns an
     * approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822 .
     * The function maps the most negative inputs to the most negative outputs, the most positive inputs to the most
     * positive outputs, and inputs near 0 to outputs near 0. This does not consider the bottom 11 bits of {@code l}.
     * <a href="https://www.researchgate.net/publication/46462650_A_New_Approximation_to_the_Normal_Distribution_Quantile_Function">Uses this algorithm by Paul Voutier</a>.
     * @see <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia has a page on the probit function.</a>
     * @param l may be any long, though very close longs will not produce different results
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     */
    public static double probitL(long l) {
        /* 5.421010862427522E-20 is 0x1p-64 */
        final double p = l * 5.421010862427522E-20 + 0.5;
        if(0.0465 > p) {
            double r = Math.sqrt(Math.log(1.0 / (p * p + 5.56268464626801E-309)));
            return c3 * r + c2 + (c1 * r + c0) / (r * (r + d1) + d0);
        } else if(0.9535 < p) {
            double q = 1.0 - p, r = Math.sqrt(Math.log(1.0 / (q * q + 5.56268464626801E-309)));
            return -c3 * r - c2 - (c1 * r + c0) / (r * (r + d1) + d0);
        } else {
            double q = p - 0.5, r = q * q;
            return q * (a2 + (a1 * r + a0) / (r * (r + b1) + b0));
        }
    }

    /**
     * Given a long where all bits are sufficiently (independently) random, this produces a normal-distributed
     * (Gaussian) variable as if by a normal distribution with mean (mu) 0.0 and standard deviation (sigma) 1.0.
     * This uses the Ziggurat algorithm, and takes one {@code long} input to produce one {@code double} value.
     * Note that no additive counters are considered sufficiently random for this, and linear congruential generators
     * might not be random enough either if they return the low-order bits without changes.
     * Patterns between different {@code state} values provided to this will generally not be preserved in the
     * output, but this may not be true all the time for patterns on all bits.
     * <br>
     * The range this can produce is at least from -7.6719775673883905 to 7.183851151080583, and is almost certainly larger
     * (only 4 billion distinct inputs were tested, and there are over 18 quintillion inputs possible).
     * <br>
     * From <a href="https://github.com/camel-cdr/cauldron/blob/7d5328441b1a1bc8143f627aebafe58b29531cb9/cauldron/random.h#L2013-L2265">Cauldron</a>,
     * MIT-licensed. This in turn is based on Doornik's form of the Ziggurat method:
     * <br>
     *      Doornik, Jurgen A (2005):
     *      "An improved ziggurat method to generate normal random samples."
     *      University of Oxford: 77.
     *
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
            idx = (int)(state & (ZIG_TABLE_ITEMS - 1));
            u = (state >>> 11) * 0x1p-53 * ZIG_TABLE[idx];

            /* Take a random box from TABLE
             * and get the value of a random x-coordinate inside it.
             * If it's also inside TABLE[idx + 1] we already know to accept
             * this value. */
            if (u < ZIG_TABLE[idx + 1])
                break;

            /* If our random box is at the bottom, we can't use the lookup
             * table and need to generate a variable for the trail of the
             * normal distribution, as described by Marsaglia in 1964: */
            if (idx == 0) {
                /* If idx is 0, then the bottom 8 bits of state must all be 0,
                 * and u must be on the larger side. */
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
            f0 = Math.exp(-0.5 * (ZIG_TABLE[idx]     * ZIG_TABLE[idx]     - y));
            f1 = Math.exp(-0.5 * (ZIG_TABLE[idx + 1] * ZIG_TABLE[idx + 1] - y));
            if (f1 + (((state = (state ^ 0xF1357AEA2E62A9C5L) * 0xABC98388FB8FAC03L) >>> 11) * 0x1p-53) * (f0 - f1) < 1.0)
                break;
        }
        /* (Zero-indexed ) bits 8, 9, and 10 aren't used in the calculations for idx
         * or u, so we use bit 9 as a sign bit here. */
        return Math.copySign(u, 256L - (state & 512L));
    }

    /**
     * Approximates the natural logarithm of {@code x} (that is, with base E), using single-precision, somewhat roughly.
     * Ported from <a href="https://code.google.com/archive/p/fastapprox/">fastapprox</a>, which is open source
     * under the New BSD License.
     * @param x the argument to the logarithm; must be greater than 0
     * @return an approximation of the logarithm of x with base E; can be any float
     */
    public static float logRough (float x)
    {
        final int vx = Compatibility.floatToIntBits(x);
        final float mx = Compatibility.intBitsToFloat((vx & 0x007FFFFF) | 0x3f000000);
        return vx * 8.262958E-8f - 86.10657f - 1.0383555f * mx - 1.1962888f / (0.3520887068f + mx);
    }

    /**
     * This is an alias for {@link #probitD(double)}.
     *
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     * @deprecated Use {@link #probitD(double)} directly instead.
     */
    @Deprecated
    public static double probit (final double d) {
        return probitD(d);
    }

    /**
     * This is an alias for {@link #probitD(double)}. It doesn't have any different precision.
     * The name is an artifact of an earlier version.
     *
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return an approximately-Gaussian-distributed double between -26.48372928592822 and 26.48372928592822
     * @deprecated Use {@link #probitD(double)} directly instead.
     */
    @Deprecated
    public static double probitHighPrecision(double d)
    {
        return probitD(d);
    }

    /**
     * This is an alias for {@link #probitL(long)}.
     *
     * @param l any long; input patterns will be preserved
     * @return a normal-distributed double, matching patterns in {@code l}
     * @deprecated Use {@link #probitL(long)} directly instead.
     */
    @Deprecated
    public static double linearNormal(long l) {
        return probitL(l);
    }
    /**
     * This is an alias for {@link #probitI(int)}.
     *
     * @param i any int; input patterns will be preserved
     * @return a normal-distributed float, matching patterns in {@code i}
     * @deprecated Use {@link #probitI(int)} directly instead.
     */
    @Deprecated
    public static float linearNormalF(int i) {
        return probitI(i);
    }
}
