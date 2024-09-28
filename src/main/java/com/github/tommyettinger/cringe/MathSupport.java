package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.NumberUtils;

import java.util.Random;

/**
 * Math code that is used internally to implement some methods in {@link GdxRandom}, but can also be used by user code.
 * Just expect some long, rambling JavaDocs.
 */
public final class MathSupport {
    /**
     * No need to instantiate.
     */
    private MathSupport() {}

    /**
     * The {@code float} value that is closer than any other to
     * <i>pi</i>, the ratio of the circumference of a circle to its
     * diameter.
     */
    public static final float PI = (float) Math.PI;
    /**
     * 1.0f divided by {@link #PI}.
     */
    public static final float PI_INVERSE = (float) (1.0 / Math.PI);
    /**
     * 2f times {@link #PI}; the same as {@link #TAU}.
     */
    public static final float PI2 = PI * 2f;
    /**
     * 2f times {@link #PI}; the same as {@link #PI2}.
     */
    public static final float TAU = PI2;
    /**
     * {@link #PI} divided by 2f; the same as {@link #ETA}.
     */
    public static final float HALF_PI = PI * 0.5f;
    /**
     * {@link #PI} divided by 2f; the same as {@link #HALF_PI}.
     */
    public static final float ETA = HALF_PI;

    /**
     * A way of taking a double in the (0.0, 1.0) range and mapping it to a Gaussian or normal distribution, so high
     * inputs correspond to high outputs, and similarly for the low range. This is centered on 0.0 and its standard
     * deviation seems to be 1.0 (the same as {@link Random#nextGaussian()}). If this is given an input of 0.0
     * or less, it returns -8.375, which is slightly less than the result when given {@link Double#MIN_VALUE}. If it is
     * given an input of 1.0 or more, it returns 8.375, which is significantly larger than the result when given the
     * largest double less than 1.0 (this value is further from 1.0 than {@link Double#MIN_VALUE} is from 0.0). If
     * given {@link Double#NaN}, it returns whatever {@link Math#copySign(double, double)} returns for the arguments
     * {@code 8.375, Double.NaN}, which is implementation-dependent.
     * <br>
     * This uses an algorithm by Peter John Acklam, as implemented by Sherali Karimov.
     * <a href="https://web.archive.org/web/20150910002142/http://home.online.no/~pjacklam/notes/invnorm/impl/karimov/StatUtil.java">Original source</a>.
     * <a href="https://web.archive.org/web/20151030215612/http://home.online.no/~pjacklam/notes/invnorm/">Information on the algorithm</a>.
     * <a href="https://en.wikipedia.org/wiki/Probit_function">Wikipedia's page on the probit function</a> may help, but
     * is more likely to just be confusing.
     * <br>
     * Acklam's algorithm and Karimov's implementation are both competitive on speed with the Box-Muller Transform and
     * Marsaglia's Polar Method, but slower than Ziggurat and the {@link #normal(long)} method here. This isn't quite
     * as precise as Box-Muller or Marsaglia Polar, and can't produce as extreme min and max results in the extreme
     * cases they should appear. If given a typical uniform random {@code double} that's exclusive on 1.0, it won't
     * produce a result higher than
     * {@code 8.209536145151493}, and will only produce results of at least {@code -8.209536145151493} if 0.0 is
     * excluded from the inputs (if 0.0 is an input, the result is {@code -8.375}). This requires a fair amount of
     * floating-point multiplication and one division for all {@code d} where it is between 0 and 1 exclusive, but
     * roughly 1/20 of the time it need a {@link Math#sqrt(double)} and {@link Math#log(double)} as well.
     * <br>
     * This can be used both as an optimization for generating Gaussian random values, and as a way of generating
     * Gaussian values that match a pattern present in the inputs (which you could have by using a sub-random sequence
     * as the input, such as those produced by a van der Corput, Halton, Sobol or R2 sequence). Most methods of generating
     * Gaussian values (e.g. Box-Muller and Marsaglia polar) do not have any way to preserve a particular pattern. Note
     * that if you don't need to preserve patterns in input, then either the Ziggurat method (which is available and the
     * default in the juniper library for pseudo-random generation) or the Marsaglia polar method (which is the default
     * in the JDK Random class) will perform better in each one's optimal circumstances. The {@link #normal(long)}
     * method here (using the Linnormal algorithm) both preserves patterns in input (given a {@code long}) and is faster
     * than Ziggurat, making it the quickest here, though at some cost to precision.
     *
     * @param d should be between 0 and 1, exclusive, but other values are tolerated
     * @return a normal-distributed double centered on 0.0; all results will be between -8.375 and 8.375, both inclusive
     */
    public static double probit (final double d) {
        return Distributor.probit(d);
    }

    /**
     * The cumulative distribution function for the normal distribution, with the range expanded to {@code [-1,1]}
     * instead of the usual {@code [0,1]} . This might be useful to bring noise functions (which sometimes have a range
     * of {@code -1,1}) from a very-centrally-biased form to a more uniformly-distributed form. The math here doesn't
     * exactly match the normal distribution's CDF because the goal was to handle inputs between -1 and 1, not the full
     * range of a normal-distributed variate (which is infinite). For dealing with noise,
     * {@link RawNoise#redistribute(float, float, float)} is probably a better option because it is configurable.
     *
     * @param x a float between -1 and 1; will be clamped if outside that domain
     * @return a more-uniformly distributed value between -1 and 1
     */
    public static float redistributeNormal(float x) {
        final float xx = x * x * 6.03435f, axx = 0.1400122886866665f * xx;
        return Math.copySign((float) Math.sqrt(1.0051551f - Math.exp(xx * (-1.2732395447351628f - axx) / (0.9952389057917015f + axx))), x);
    }

    /**
     * A decent approximation of {@link Math#exp(double)} for small float arguments, meant to be faster than Math's
     * version for floats at the expense of accuracy. This uses the 2/2 Padé
     * approximant to {@code Math.exp(power)}, but halves the argument to exp() before approximating, and squares it
     * before returning.The halving/squaring keeps the calculation in a more precise span for a larger domain. You
     * should not use this if your {@code power} inputs will be much higher than about 3 or lower than -3 .
     * <br>
     * Pretty much all the work for this was done by Wolfram Alpha.
     *
     * @param power what exponent to raise {@link Math#E} to
     * @return a rough approximation of E raised to the given power
     */
    public static float exp(float power) {
        power *= 0.5f;
        power = (12 + power * (6 + power)) / (12 + power * (-6 + power));
        return power * power;
    }

    /**
     * An approximation of the cube-root function for float inputs and outputs.
     * This can be about twice as fast as {@link Math#cbrt(double)}. It
     * correctly returns negative results when given negative inputs.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between -512 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">Wikipedia</a> for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds, posted in his
     * <a href="https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/7d8d1e19b2ab09743f46964f60244906e1023f6a/src/Posts/ballcube.c#L182-L197">"Stand-alone-junk" repo</a> .
     * <br>
     * This was adjusted very slightly so {@code cbrt(1f) == 1f}. While this corrects the behavior for one of the most
     * commonly-expected inputs, it may change results for (very) large positive or negative inputs.
     * <br>
     * If you need to work with doubles, or need higher precision, use {@link Math#cbrt(double)}.
     * @param x any finite float to find the cube root of
     * @return the cube root of x, approximated
     */
    public static float cbrt(float x) {
        int ix = NumberUtils.floatToIntBits(x);
        final int sign = ix & 0x80000000;
        ix &= 0x7FFFFFFF;
        final float x0 = x;
        ix = (ix >>> 2) + (ix >>> 4);
        ix += ix >>> 4;
        ix = ix + (ix >>> 8) + 0x2A5137A0 | sign;
        x = NumberUtils.intBitsToFloat(ix);
        x = 0.33333334f * (2f * x + x0 / (x * x));
        x = 0.33333334f * (1.9999999f * x + x0 / (x * x));
        return x;
    }

    /**
     * An approximation of the cube-root function for positive float inputs and outputs.
     * This can be about twice as fast as {@link Math#cbrt(double)}. It
     * only accepts positive inputs and only produces positive outputs. It should be
     * a tiny hair faster than {@link #cbrt(float)}.
     * <br>
     * Has very low relative error (less than 1E-9) when inputs are uniformly
     * distributed between -512 and 512, and absolute mean error of less than
     * 1E-6 in the same scenario. Uses a bit-twiddling method similar to one
     * presented in Hacker's Delight and also used in early 3D graphics (see
     * <a href="https://en.wikipedia.org/wiki/Fast_inverse_square_root">Wikipedia</a> for more, but
     * this code approximates cbrt(x) and not 1/sqrt(x)). This specific code
     * was originally by Marc B. Reynolds, posted in his
     * <a href="https://github.com/Marc-B-Reynolds/Stand-alone-junk/blob/7d8d1e19b2ab09743f46964f60244906e1023f6a/src/Posts/ballcube.c#L182-L197">"Stand-alone-junk" repo</a> .
     * <br>
     * This was adjusted very slightly so {@code cbrt(1f) == 1f}. While this corrects the behavior for one of the most
     * commonly-expected inputs, it may change results for (very) large positive or negative inputs.
     * <br>
     * If you need to work with doubles, or need higher precision, use {@link Math#cbrt(double)}.
     * @param x any finite float to find the cube root of
     * @return the cube root of x, approximated
     */
    public static float cbrtPositive(float x) {
        int ix = NumberUtils.floatToIntBits(x);
        final float x0 = x;
        ix = (ix >>> 2) + (ix >>> 4);
        ix += ix >>> 4;
        ix = ix + (ix >>> 8) + 0x2A5137A0;
        x = NumberUtils.intBitsToFloat(ix);
        x = 0.33333334f * (2f * x + x0 / (x * x));
        x = 0.33333334f * (1.9999999f * x + x0 / (x * x));
        return x;
    }


    /**
     * A generalization on bias and gain functions that can represent both; this version is branch-less.
     * This is based on <a href="https://arxiv.org/abs/2010.09714">this micro-paper</a> by Jon Barron, which
     * generalizes the earlier bias and gain rational functions by Schlick. The second and final page of the
     * paper has useful graphs of what the s (shape) and t (turning point) parameters do; shape should be 0
     * or greater, while turning must be between 0 and 1, inclusive. This effectively combines two different
     * curving functions so that they continue into each other when x equals turning. The shape parameter will
     * cause this to imitate "smoothstep-like" splines when greater than 1 (where the values ease into their
     * starting and ending levels), or to be the inverse when less than 1 (where values start like square
     * root does, taking off very quickly, but also end like square does, landing abruptly at the ending
     * level). You should only give x values between 0 and 1, inclusive.
     *
     * @param x       progress through the spline, from 0 to 1, inclusive
     * @param shape   must be greater than or equal to 0; values greater than 1 are "normal interpolations"
     * @param turning a value between 0.0 and 1.0, inclusive, where the shape changes
     * @return a float between 0 and 1, inclusive
     */
    public static float barronSpline(final float x, final float shape, final float turning) {
        final float d = turning - x;
        final int f = NumberUtils.floatToIntBits(d) >> 31, n = f | 1;
        return (turning * n - f) * (x + f) / (1.17549435E-38f - f + (x + shape * d) * n) - f;
    }
    // 1.17549435E-38f is 0x1p-126f

    /**
     * An overly-permissive, but fast, way of looking up the numeric value of a hex digit provided as a char.
     * This does not use a table lookup. It will return garbage if not given a valid hex digit, but will not crash
     * or throw an Exception on any input. If you know the given digit is between 0 and 9 inclusive, this can also be
     * used to get the numeric value of that digit as decimal, rather than hexadecimal. You could instead use
     * {@code (c & 15)} or just {@code (c - '0')} in that case, though.
     * @param c a char that should really be a valid hex digit (matching the regex {@code [0-9A-Fa-f]})
     * @return the numeric value of the given digit char
     */
    public static int hexCode(final char c) {
        // this will be 0 when c is between 0-9, and 64 when c is any letter.
        final int h = (c & 64);
        // the bottom bits (going up to 15) are accurate for 0-9, but are 9 off for letters.
        // (64 >>> 3) is 8, and (64 >>> 6) is 1.
        return (c & 15) + (h >>> 3) + (h >>> 6);
    }

    /**
     * Converts the given number, which should be between 0 and 15 inclusive, to its corresponding hex digit as a char.
     * This does not use a table lookup. It will return garbage if not given a number in range, but will not crash
     * or throw an Exception on any input.
     * @param number an int that should really be between 0 (inclusive) and 15 (inclusive)
     * @return the hex digit that matches the given number
     */
    @SuppressWarnings("ShiftOutOfRange")
    public static char hexChar(final int number) {
        return (char)(number + 48 + (9 - number >>> -3));
    }

    /**
     * Converts the given number, which should be between 0 and 15 inclusive, to its corresponding hex digit as a char.
     * This does not use a table lookup. It will return garbage if not given a number in range, but will not crash
     * or throw an Exception on any input.
     * <br>
     * This overload only exists to ease conversion to hex digits when given a long input. Its body is the same as the
     * overload that takes an int, {@link #hexChar(int)}.
     * @param number an int that should really be between 0 (inclusive) and 15 (inclusive)
     * @return the hex digit that matches the given number
     */
    @SuppressWarnings("ShiftOutOfRange")
    public static char hexChar(final long number) {
        return (char)(number + 48 + (9 - number >>> -3));
    }

    /**
     * Appends the 8-digit unsigned hex format of {@code number} to the given StringBuilder. This always draws from
     * only the digits 0-9 and the capital letters A-F for its hex digits.
     * @param sb an existing StringBuilder to append to
     * @param number any int to write in hex format
     * @return sb, after modification, for chaining.
     */
    public static StringBuilder appendUnsignedHex(StringBuilder sb, int number) {
        for (int s = 28; s >= 0; s -= 4) {
            sb.append(hexChar(number >>> s & 15));
        }
        return sb;
    }

    /**
     * Appends the 16-digit unsigned hex format of {@code number} to the given StringBuilder. This always draws from
     * only the digits 0-9 and the capital letters A-F for its hex digits.
     * @param sb an existing StringBuilder to append to
     * @param number any long to write in hex format
     * @return sb, after modification, for chaining.
     */
    public static StringBuilder appendUnsignedHex(StringBuilder sb, long number) {
        for (int i = 60; i >= 0; i -= 4) {
            sb.append(hexChar(number >>> i & 15));
        }
        return sb;
    }

    /**
     * Allocates a new 8-char array filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any int to write in hex format
     * @return a new char array holding the 8-character unsigned hex representation of {@code number}
     */
    public static char[] unsignedHexArray(int number) {
        final char[] chars = new char[8];
        for (int i = 0, s = 28; i < 8; i++, s -= 4) {
            chars[i] = hexChar(number >>> s & 15);
        }
        return chars;
    }

    /**
     * Allocates a new 16-char array filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any long to write in hex format
     * @return a new char array holding the 16-character unsigned hex representation of {@code number}
     */
    public static char[] unsignedHexArray(long number) {
        final char[] chars = new char[16];
        for (int i = 0, s = 60; i < 16; i++, s -= 4) {
            chars[i] = hexChar(number >>> s & 15);
        }
        return chars;
    }

    /**
     * Allocates a new 8-char String filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any int to write in hex format
     * @return a new String holding the 8-character unsigned hex representation of {@code number}
     */
    public static String unsignedHex(int number) {
        return String.valueOf(unsignedHexArray(number));
    }

    /**
     * Allocates a new 16-char String filled with the unsigned hex format of {@code number}, and returns it.
     * @param number any long to write in hex format
     * @return a new String holding the 16-character unsigned hex representation of {@code number}
     */
    public static String unsignedHex(long number) {
        return String.valueOf(unsignedHexArray(number));
    }

    /**
     * Reads in a CharSequence containing only decimal digits (only 0-9) with an optional sign at the start
     * and returns the long they represent, reading at most 19 characters (20 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed as unsigned longs. This means "18446744073709551615" would
     * return the long -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start
     * and have the number as greater than {@link Long#MAX_VALUE}, such as with "-18446744073709551615", then both
     * indicate a negative number, but the digits will be processed first (producing -1) and then the whole thing will
     * be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a decimal digit, or
     * stopping the parse process early if a non-0-9 char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing decimal digits with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 20 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromDec(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 21;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 21;
        } else {
            if (!(c >= '0' && c <= '9'))
                return 0;
            else {
                sign = 1;
                lim = 20;
            }
            h = (c - '0');
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!(c >= '0' && c <= '9'))
                return data * sign;
            data *= 10;
            data += (c - '0');
        }
        return data * sign;
    }


    /**
     * Reads in a CharSequence containing only decimal digits (only 0-9) with an optional sign at the start
     * and returns the int they represent, reading at most 10 characters (11 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed as unsigned integers. This means "4294967295" would return the int
     * -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start and have the
     * number as greater than {@link Integer#MAX_VALUE}, such as with "-4294967295", then both indicate a negative
     * number, but the digits will be processed first (producing -1) and then the whole thing will be multiplied by -1
     * to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a decimal digit, or
     * stopping the parse process early if a non-0-9 char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing decimal digits with an optional sign
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 10 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromDec(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 11;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 11;
        } else {
            if (!(c >= '0' && c <= '9'))
                return 0;
            else {
                sign = 1;
                lim = 10;
            }
            h = (c - '0');
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!(c >= '0' && c <= '9'))
                return data * sign;
            data *= 10;
            data += (c - '0');
        }
        return data * sign;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the int they represent, reading at most 8 characters (9 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format() given %X in the formatting
     * string; that is, if the first char of an 8-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFF" would return the int -1 when passed to this, though you
     * could also simply use "-1" . If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Integer.parseUnsignedInt method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and just doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 8 characters if end is too large)
     * @return the int that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 9;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 9;
        } else {
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return 0;
            else {
                sign = 1;
                lim = 8;
                h = hexCode(c);
            }
        }
        int data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return data * sign;
            data <<= 4;
            data |= hexCode(c);
        }
        return data * sign;
    }

    /**
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format() given %X in the formatting
     * string; that is, if the first char of a 16-char (or longer) CharSequence is a hex digit 8 or higher, then the
     * whole number represents a negative number, using two's complement and so on. This means "FFFFFFFFFFFFFFFF" would
     * return the long -1 when passed to this, though you could also simply use "-1" . If you use both '-' at the start
     * and have the most significant digit as 8 or higher, such as with "-FFFFFFFFFFFFFFFF", then both indicate a
     * negative number, but the digits will be processed first (producing -1) and then the whole thing will be
     * multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and just doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 8 characters if end is too large)
     * @return the long that cs represents
     */
    public static long longFromHex(final CharSequence cs, final int start, int end) {
        int sign, h, lim;
        if (cs == null || start < 0 || end <= 0 || (end = Math.min(end, cs.length())) - start <= 0)
            return 0;
        char c = cs.charAt(start);
        if (c == '-') {
            sign = -1;
            h = 0;
            lim = 17;
        } else if (c == '+') {
            sign = 1;
            h = 0;
            lim = 17;
        } else {
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return 0;
            else {
                sign = 1;
                lim = 16;
                h = hexCode(c);
            }
        }
        long data = h;
        for (int i = start + 1; i < end && i < start + lim; i++) {
            c = cs.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f')))
                return data * sign;
            data <<= 4;
            data |= hexCode(c);
        }
        return data * sign;
    }

    /**
     * Reads a float in from the String {@code str}, using only the range from {@code start} (inclusive) to {@code end}
     * (exclusive). This effectively returns {@code Float.parseFloat(str.substring(start, Math.min(str.length(), end)))}
     * . Unlike the other number-reading methods here, this doesn't do much to validate its input, so the end of the end
     * of the String must be after the full float number. If the parse fails, this returns 0f.
     * @param str a String containing a valid float in the specified range
     * @param start the start index (inclusive) to read from
     * @param end the end index (exclusive) to stop reading before
     * @return the parsed float from the given range, or 0f if the parse failed.
     */
    public static float floatFromDec(final String str, final int start, int end) {
        try {
            return Float.parseFloat(str.substring(start, Math.min(str.length(), end)));
        } catch (NumberFormatException ignored) {
            return 0f;
        }
    }

}
