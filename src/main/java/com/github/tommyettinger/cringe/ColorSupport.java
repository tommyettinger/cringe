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
     * Gets the hue of the given RGBA color, from 0.0f to 1.0f, both inclusive.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0 (ignored)
     * @return the hue as a float from 0 to 1, both inclusive
     */
    public static float hue(final float r, final float g, final float b, final float a) {
        float x, y, z, w;
        if (g < b) {
            x = b;
            y = g;
            z = -1f;
            w = 2f / 3f;
        } else {
            x = g;
            y = b;
            z = 0f;
            w = -1f / 3f;
        }
        if (r < x) {
            z = w;
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        return Math.abs(z + (w - y) / (6f * d + 1e-10f));
    }

    /**
     * Gets the saturation of the given RGBA color, from 0.0f to 1.0f, both inclusive.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0 (ignored)
     * @return the saturation as a float from 0 to 1, both inclusive
     */
    public static float saturation(final float r, final float g, final float b, final float a) {
        float x, y, w;
        if (g < b) {
            x = b;
            y = g;
        } else {
            x = g;
            y = b;
        }
        if (r < x) {
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        float l = x * (1f - 0.5f * d / (x + 1e-10f));
        return (x - l) / (Math.min(l, 1f - l) + 1e-10f);
    }

    /**
     * Gets the lightness (as per HSL) of the given RGBA color, from 0.0f to 1.0f, both inclusive.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0 (ignored)
     * @return the lightness as a float from 0 to 1, both inclusive
     */
    public static float lightness(final float r, final float g, final float b, final float a) {
        float x, y, w;
        if (g < b) {
            x = b;
            y = g;
        } else {
            x = g;
            y = b;
        }
        if (r < x) {
            w = r;
        } else {
            w = x;
            x = r;
        }
        float d = x - Math.min(w, y);
        return x * (1f - 0.5f * d / (x + 1e-10f));
    }

    /**
     * Gets the brightness (as per HSB, also called value) of the given RGBA color, from 0.0f to 1.0f, both inclusive.
     *
     * @param r red, from 0.0 to 1.0
     * @param g green, from 0.0 to 1.0
     * @param b blue, from 0.0 to 1.0
     * @param a alpha, from 0.0 to 1.0 (ignored)
     * @return the brightness as a float from 0 to 1, both inclusive
     */
    public static float brightness(final float r, final float g, final float b, final float a) {
        return Math.max(Math.max(r, g), b);
    }

    /**
     * Just calls {@link #hue(float, float, float, float)} with the RGBA channels of the given Color.
     * @param color a Color that will not be modified
     * @return the hue of the given Color
     */
    public static float hue(Color color) {
        return hue(color.r, color.g, color.b, color.a);
    }

    /**
     * Just calls {@link #saturation(float, float, float, float)} with the RGBA channels of the given Color.
     * @param color a Color that will not be modified
     * @return the saturation of the given Color
     */
    public static float saturation(Color color) {
        return saturation(color.r, color.g, color.b, color.a);
    }

    /**
     * Just calls {@link #lightness(float, float, float, float)} with the RGBA channels of the given Color.
     * @param color a Color that will not be modified
     * @return the lightness of the given Color
     */
    public static float lightness(Color color) {
        return lightness(color.r, color.g, color.b, color.a);
    }

    /**
     * Just calls {@link #brightness(float, float, float, float)} with the RGBA channels of the given Color.
     * @param color a Color that will not be modified
     * @return the brightness of the given Color
     */
    public static float brightness(Color color) {
        return brightness(color.r, color.g, color.b, color.a);
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
}
