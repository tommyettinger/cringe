package com.github.tommyettinger.cringe;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Color handling code that is used internally to implement some methods in {@link GdxRandom}, but can also be used by
 * user code.
 */
public final class ColorSupport {
    /**
     * No need to instantiate.
     */
    private ColorSupport() {}

    /**
     * Converts the four HSLA components, each in the 0.0 to 1.0 range, to the RGBA values in {@code color}, modifying
     * {@code color} in-place. <em>YES, IT MODIFIES THE GIVEN COLOR, SO DON'T PASS THIS A COLOR CONSTANT.</em>
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSL(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     * The {@code h} parameter for hue can be lower than 0.0 or higher than 1.0 because the hue "wraps around;" only the
     * fractional part of h is used. The other parameters must be between 0.0 and 1.0 (inclusive) to make sense.
     * Note that this takes a hue ({@code h}) as a value from 0.0 to 1.0, not an angle in degrees.
     *
     * @param color a non-null Color that will be modified in-place
     * @param h hue, usually from 0.0 to 1.0, but only the fractional part is used
     * @param s saturation, from 0.0 to 1.0
     * @param l lightness, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return {@code color}, after changes
     */
    public static Color hsl2rgb(final Color color, final float h, final float s, final float l, final float a) {
        float hue = h - MathUtils.floor(h);
        float x = Math.min(Math.max(Math.abs(hue * 6f - 3f) - 1f, 0f), 1f);
        float y = hue + (2f / 3f);
        float z = hue + (1f / 3f);
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        float v = (l + s * Math.min(l, 1f - l));
        float d = 2f * (1f - l / (v + 1e-10f));
        return color.set(v * MathUtils.lerp(1f, x, d), v * MathUtils.lerp(1f, y, d), v * MathUtils.lerp(1f, z, d), a);
    }

    /**
     * Converts the four HSBA or HSVA components, each in the 0.0 to 1.0 range, to the RGBA values in {@code color},
     * modifying {@code color} in-place. <em>YES, IT MODIFIES THE GIVEN COLOR, SO DON'T PASS THIS A COLOR CONSTANT.</em>
     * I brought this over from colorful-gdx's FloatColors class. I can't recall where I got the original HSB(A) code
     * from, but there's a strong chance it was written by cypherdare/cyphercove for their color space comparison.
     * The {@code h} parameter for hue can be lower than 0.0 or higher than 1.0 because the hue "wraps around;" only the
     * fractional part of h is used. The other parameters must be between 0.0 and 1.0 (inclusive) to make sense.
     * Note that this takes a hue ({@code h}) as a value from 0.0 to 1.0, not an angle in degrees.
     * HSB and HSV are synonyms, but it makes more sense to call the parameter "brightness."
     *
     * @param color a non-null Color that will be modified in-place
     * @param h hue, usually from 0.0 to 1.0, but only the fractional part is used
     * @param s saturation, from 0.0 to 1.0
     * @param b brightness or value, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0
     * @return {@code color}, after changes
     */
    public static Color hsb2rgb(final Color color, final float h, final float s, final float b, final float a) {
        float hue = h - MathUtils.floor(h);
        float x = Math.min(Math.max(Math.abs(hue * 6f - 3f) - 1f, 0f), 1f);
        float y = hue + (2f / 3f);
        float z = hue + (1f / 3f);
        y -= (int) y;
        z -= (int) z;
        y = Math.min(Math.max(Math.abs(y * 6f - 3f) - 1f, 0f), 1f);
        z = Math.min(Math.max(Math.abs(z * 6f - 3f) - 1f, 0f), 1f);
        return color.set(b * MathUtils.lerp(1f, x, s), b * MathUtils.lerp(1f, y, s), b * MathUtils.lerp(1f, z, s), a);
    }

    /**
     * An overly-permissive, but fast, way of looking up the numeric value of a hex digit provided as a char.
     * This does not use a table lookup. It will return garbage if not given a valid hex digit, but will not crash
     * on any input.
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
     * Reads in a CharSequence containing only hex digits (only 0-9, a-f, and A-F) with an optional sign at the start
     * and returns the long they represent, reading at most 16 characters (17 if there is a sign) and returning the
     * result if valid, or 0 if nothing could be read. The leading sign can be '+' or '-' if present. This can also
     * represent negative numbers as they are printed by such methods as String.format() given %X in the formatting
     * string; that is, if the first char of a 16-char (or longer)
     * CharSequence is a hex digit 8 or higher, then the whole number represents a negative number, using two's
     * complement and so on. This means "FFFFFFFFFFFFFFFF" would return the long -1 when passed to this, though you
     * could also simply use "-1" . If you use both '-' at the start and have the most significant digit as 8 or higher,
     * such as with "-FFFFFFFFFFFFFFFF", then both indicate a negative number, but the digits will be processed first
     * (producing -1) and then the whole thing will be multiplied by -1 to flip the sign again (returning 1).
     * <br>
     * Should be fairly close to Java 8's Long.parseUnsignedLong method, which is an odd omission from earlier JDKs.
     * This doesn't throw on invalid input, though, instead returning 0 if the first char is not a hex digit, or
     * stopping the parse process early if a non-hex-digit char is read before end is reached. If the parse is stopped
     * early, this behaves as you would expect for a number with fewer digits, and simply doesn't fill the larger places.
     *
     * @param cs    a CharSequence, such as a String, containing only hex digits with an optional sign (no 0x at the start)
     * @param start the (inclusive) first character position in cs to read
     * @param end   the (exclusive) last character position in cs to read (this stops after 16 characters if end is too large)
     * @return the long that cs represents
     */
    public static int intFromHex(final CharSequence cs, final int start, int end) {
        int sign, h, lim, len;
        if (cs == null || start < 0 || end <= 0 || end - start <= 0
                || (len = cs.length()) - start <= 0 || end > len)
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
            }
            h = hexCode(c);
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

}
