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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

import static com.badlogic.gdx.math.MathUtils.PI2;

/**
 * Provides 1D noise methods that can be queried at any point on a line to get a continuous random value.
 * These each use some form of low-quality, high-speed unary hash on the floor and ceiling of a float or double value,
 * then interpolate between the hash results. Some of these work on angles (so they wrap like a line around a circle)
 * instead of normal lines.
 * <br>
 * The wobble methods that take a seed and a distance along a line as a value are:
 * <ul>
 *     <li>{@link #wobble(float, int)} is straightforward; it uses a hermite spline to interpolate two points.</li>
 *     <li>{@link #bicubicWobble(float, long)} uses bicubic interpolation on 4 points, and tends to be smooth.</li>
 *     <li>{@link #splineWobble(float, int)} can be much more sharp or smooth, varying randomly over its length.</li>
 *     <li>splineWobble() also can take a long seed.</li>
 *     <li>{@link #trigWobble(float, int)} is a trigonometric wobble, using sine to smoothly transition.</li>
 * </ul>
 * There are also some methods to get a wobbling angle, smoothly wrapping around the angle 0:
 * <ul>
 *     <li>{@link #wobbleAngle(float, int)} is a simple wobble with output in radians.</li>
 *     <li>{@link #wobbleAngleDeg(float, int)} is a simple wobble with output in degrees.</li>
 *     <li>{@link #wobbleAngleTurns(float, int)} is a simple wobble with output in turns (1 turn == 360 degrees).</li>
 *     <li>{@link #splineWobbleAngle(float, int)} is a more-random wobble with output in radians.</li>
 *     <li>{@link #splineWobbleAngleDeg(float, int)} is a more-random wobble with output in degrees.</li>
 *     <li>{@link #splineWobbleAngleTurns(float, int)} is a more-random wobble with output in turns (1 turn == 360 degrees).</li>
 *     <li>Each of the splineWobbleAngle() methods also can take a long seed.</li>
 * </ul>
 */
public class LineWobble {
    /**
     * A type of 1D noise that takes an int seed and is optimized for usage on GWT. This uses cubic interpolation
     * between random peak or valley points.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float wobble(float value, int seed)
    {
        final int floor = MathUtils.floor(value);
        final int z = seed + Compatibility.imul(floor, 0x9E3779B9);
        final int start = Compatibility.imul(z ^ 0xD1B54A35, 0x92B5C323);
        final int end   = Compatibility.imul(z + 0x9E3779B9 ^ 0xD1B54A35, 0x92B5C323);
        value -= floor;
        value *= value * (3 - 2 * value);
        return ((1 - value) * start + value * end) * 0x0.ffffffp-31f;
    }

    /**
     * Sway smoothly using bicubic interpolation between 4 points (the two integers before t and the two after).
     * This pretty much never produces steep changes between peaks and valleys; this may make it more useful for things
     * like generating terrain that can be walked across in a side-scrolling game.
     *
     * @param t    a distance traveled; should change by less than 1 between calls, and should be less than about 10000
     * @param seed any long
     * @return a smoothly-interpolated swaying value between -1 and 1, both exclusive
     */
    public static float bicubicWobble(float t, long seed)
    {
        final long floor = (long) Math.floor(t);
        // what we add here ensures that at the very least, the upper half will have some non-zero bits.
        long s = ((seed & 0xFFFFFFFFL) ^ (seed >>> 32)) + 0x9E3779B97F4A7C15L;
        // fancy XOR-rotate-rotate is a way to mix bits both up and down without multiplication.
        s = (s ^ (s << 21 | s >>> 43) ^ (s << 50 | s >>> 14)) + floor;
        // we use a different technique here, relative to other wobble methods.
        // to avoid frequent multiplication and replace it with addition by constants, we track 3 variables, each of
        // which updates with a different large, negative long increment. when we want to get a result, we just XOR
        // m, n, and o, and use only the upper bits (by multiplying by a tiny fraction).
        final long m = s * 0xD1B54A32D192ED03L;
        final long n = s * 0xABC98388FB8FAC03L;
        final long o = s * 0x8CB92BA72F3D8DD7L;

        final float a = (m ^ n ^ o);
        final float b = (m + 0xD1B54A32D192ED03L ^ n + 0xABC98388FB8FAC03L ^ o + 0x8CB92BA72F3D8DD7L);
        final float c = (m + 0xA36A9465A325DA06L ^ n + 0x57930711F71F5806L ^ o + 0x1972574E5E7B1BAEL);
        final float d = (m + 0x751FDE9874B8C709L ^ n + 0x035C8A9AF2AF0409L ^ o + 0xA62B82F58DB8A985L);

        // get the fractional part of t.
        t -= floor;
        // this is bicubic interpolation, inlined
        final float p = (d - c) - (a - b);
        // 7.7.228014483236334E-20 , or 0x1.5555555555428p-64 , is just inside {@code -2f/3f/Long.MIN_VALUE} .
        // it gets us about as close as we can go to 1.0 .
        return (t * (t * t * p + t * (a - b - p) + c - a) + b) * 7.228014E-20f;
    }

    /**
     * A variant on {@link #wobble(float, int)} that uses {@link MathSupport#barronSpline(float, float, float)} to
     * interpolate between peaks/valleys, with the shape and turning point determined like the other values.
     * This can be useful when you want a curve to seem more "natural," without the similarity between every peak or
     * every valley in {@link #wobble(float, int)}. This can produce both fairly sharp turns and very gradual curves.
     *
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float splineWobble(float value, int seed)
    {
        // int fast floor, from libGDX; 16384 is 2 to the 14, or 0x1p14, or 0x4000
        final int floor = ((int)(value + 16384.0) - 16384);
        final int z = seed + Compatibility.imul(floor, 0x9E3779B9);
        final int startBits = Compatibility.imul(z ^ 0xD1B54A35, 0x92B5C323);
        final int endBits   = Compatibility.imul(z + 0x9E3779B9 ^ 0xD1B54A35, 0x92B5C323);
        final int mixBits = startBits + endBits;
        value -= floor;
        value = MathSupport.barronSpline(value,
                (mixBits & 0xFFFF) * 6.1035156E-5f + 1f, // 6.1035156E-5f == 0x1p-14f
                (mixBits >>> 16) * 1.1444092E-5f + 0.125f); // 1.1444092E-5f == 0x1.8p-17f
        value *= value * (3f - 2f * value);
        return ((1 - value) * startBits + value * endBits) * 4.6566126E-10f; // 4.6566126E-10f == 0x0.ffffffp-31f
    }

    /**
     * A variant on {@link #wobble(float, int)} that uses {@link MathSupport#barronSpline(float, float, float)} to
     * interpolate between peaks/valleys, with the shape and turning point determined like the other values.
     * This can be useful when you want a curve to seem more "natural," without the similarity between every peak or
     * every valley in {@link #wobble(float, int)}. This can produce both fairly sharp turns and very gradual curves.
     *
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @param seed  a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float splineWobble(float value, long seed)
    {
        final long floor = ((long)(value + 0x1p14) - 0x4000);
        final long z = seed + floor * 0x6C8E9CF570932BD5L;
        final long startBits = ((z ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L ^ 0x9E3779B97F4A7C15L),
                endBits = ((z + 0x6C8E9CF570932BD5L ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L ^ 0x9E3779B97F4A7C15L),
                mixBits = startBits + endBits;
        value -= floor;
        value = MathSupport.barronSpline(value, (mixBits & 0xFFFFFFFFL) * 0x1p-30f + 1f, (mixBits >>> 32 & 0xFFFFL) * 0x1.8p-17f + 0.125f);
        value *= value * (3f - 2f * value);
        return ((1 - value) * startBits + value * endBits) * 0x0.ffffffp-63f;
    }

    /**
     * Trigonometric wobble. Domain for {@code value} is effectively [-16384, 16384]. Range is (-1, 1).
     *
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @param seed  a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float trigWobble(float value, long seed)
    {
        final long floor = ((int)(value + 16384.0) - 16384);
        final long z = seed + floor * 0x6C8E9CF570932BD5L;
        final long start = ((z ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L),
                end = ((z + 0x6C8E9CF570932BD5L ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L);
        value = MathUtils.sinDeg((value - floor) * 90);
        value *= value;
        return ((1f - value) * start + value * end) * 0x0.ffffffp-63f;
    }

    /**
     * Trigonometric wobble. Domain for {@code value} is effectively [-16384, 16384]. Range is (-1, 1).
     *
     * @param value a float that typically changes slowly, by less than 2.0, with direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float trigWobble(float value, int seed)
    {
        final int floor = MathUtils.floor(value);
        final int z = seed + Compatibility.imul(floor, 0x9E3779B9);
        final int start = Compatibility.imul(z ^ 0xD1B54A35, 0x92B5C323);
        final int end   = Compatibility.imul(z + 0x9E3779B9 ^ 0xD1B54A35, 0x92B5C323);
        value = MathUtils.sinDeg((value - floor) * 90);
        value *= value;
        return ((1f - value) * start + value * end) * 0x0.ffffffp-31f;
    }

    /**
     * A 1D "noise" method that produces smooth transitions like a sine wave, but also wrapping around at pi * 2 so this
     * can be used to get smoothly-changing random angles. Has (seeded) random peaks and valleys where it
     * slows its range of change, but can return any value from 0 to 6.283185307179586f, or pi * 2. The pattern this
     * will produce will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than a sine wave would, and the shape here uses cubic interpolation.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0f and 6.283185307179586f (both inclusive), smoothly changing with value and wrapping
     */
    public static float wobbleAngle(float value, int seed)
    {
        return wobbleAngleTurns(value, seed) * 6.283185307179586f;
    }

    /**
     * A 1D "noise" method that produces smooth transitions like a sine wave, but also wrapping around at 360.0 so this
     * can be used to get smoothly-changing random angles. Has (seeded) random peaks and valleys where it
     * slows its range of change, but can return any value from 0 to 360.0f . The pattern this
     * will produce will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than a sine wave would, and the shape here uses cubic interpolation.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0f and 360.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float wobbleAngleDeg(float value, int seed)
    {
        return wobbleAngleTurns(value, seed) * 360.0f;
    }

    /**
     * A 1D "noise" method that produces smooth transitions like a sine wave, but also wrapping around at 1.0
     * so this can be used to get smoothly-changing random angles in turns. Has (seeded) random peaks and valleys where
     * it slows its range of change, but can return any value from 0 to 1.0. The pattern this
     * will produce will be completely different if the seed changes, and the value is expected to be something other
     * than an angle, like time. Uses a simple method of cubic interpolation between random values, where a random value
     * is used without modification when given an integer for {@code value}. Note that this uses a different type of
     * interpolation than a sine wave would, and the shape here uses cubic interpolation.
     * <br>
     * Turns are a way of measuring angles, like radians or degrees, but where a full rotation is 360 in degrees or
     * {@link MathUtils#PI2} in radians, it is 1.0 in turns. This proves handy for things like hue calculations in
     * {@link ColorSupport#hsl2rgb(Color, float, float, float, float)}.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 1.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float wobbleAngleTurns(float value, int seed)
    {
        // int fast floor, from libGDX
        final int floor = MathUtils.floor(value);
        // gets roughly-random values for the start and end, involving the seed also.
        final int z = seed + Compatibility.imul(floor, 0x9E3779B9);
        float start = (Compatibility.imul(z ^ 0xD1B54A35, 0x92B5C323) >>> 1) * 0x1p-31f;
        float end   = (Compatibility.imul(z + 0x9E3779B9 ^ 0xD1B54A35, 0x92B5C323) >>> 1) * 0x1p-31f;
        value -= floor;
        // makes the changes smoother by slowing down near start or end.
        value *= value * (3f - 2f * value);
        // like lerpAngle code, but in turns
        end = end - start + 1.5f;
        end -= (int)end + 0.5f;
        start += end * value + 1;
        return (start - (int)start);
    }

    /**
     * Like {@link #splineWobble(float, int)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in radians.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 6.283185307179586f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngle(float value, int seed) {
        return splineWobbleAngleTurns(value, seed) * PI2;
    }

    /**
     * Like {@link #splineWobble(float, int)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in degrees.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 360.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngleDeg(float value, int seed) {
        return splineWobbleAngleTurns(value, seed) * 360;
    }

    /**
     * Like {@link #splineWobble(float, int)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in turns.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 1.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngleTurns(float value, int seed)
    {
        final int floor = ((int)(value + 16384.0) - 16384);
        final int z = seed + Compatibility.imul(floor, 0x9E3779B9);
        final int startBits = Compatibility.imul(z ^ 0xD1B54A35, 0x92B5C323);
        final int endBits   = Compatibility.imul(z + 0x9E3779B9 ^ 0xD1B54A35, 0x92B5C323);
        final int mixBits = startBits + endBits;
        value -= floor;
        value = MathSupport.barronSpline(value,
                (mixBits & 0xFFFF) * 6.1035156E-5f + 1f, // 6.1035156E-5f == 0x1p-14f
                (mixBits >>> 16) * 1.1444092E-5f + 0.125f); // 1.1444092E-5f == 0x1.8p-17f
        value *= value * (3f - 2f * value);
        float start = (startBits >>> 1) * 4.6566126E-10f; // 4.6566126E-10f == 0x0.ffffffp-31f
        float end   = (endBits   >>> 1) * 4.6566126E-10f; // 4.6566126E-10f == 0x0.ffffffp-31f
        end = end - start + 1.5f;
        end -= (int)end + 0.5f;
        start += end * value + 1;
        return (start - (int)start);
    }

    /**
     * Like {@link #splineWobble(float, long)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in radians.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 6.283185307179586f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngle(float value, long seed) {
        return splineWobbleAngleTurns(value, seed) * PI2;
    }

    /**
     * Like {@link #splineWobble(float, long)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in degrees.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 360.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngleDeg(float value, long seed) {
        return splineWobbleAngleTurns(value, seed) * 360;
    }

    /**
     * Like {@link #splineWobble(float, long)}, this is a 1D wobble that can become more sharp or more gradual at different
     * points on its length, but this produces a (wrapping) angle measured in turns.
     *
     * @param value a float that typically changes slowly, by less than 1.0, with possible direction changes at integer inputs
     * @param seed  a long seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @return a pseudo-random float between 0.0f and 1.0f (both inclusive), smoothly changing with value and wrapping
     */
    public static float splineWobbleAngleTurns(float value, long seed)
    {
        final long floor = ((long)(value + 0x1p14) - 0x4000);
        final long z = seed + floor * 0x6C8E9CF570932BD5L;
        final long startBits = ((z ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L ^ 0x9E3779B97F4A7C15L),
                endBits = ((z + 0x6C8E9CF570932BD5L ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5C323L ^ 0x9E3779B97F4A7C15L),
                mixBits = startBits + endBits;
        value -= floor;
        value = MathSupport.barronSpline(value, (mixBits & 0xFFFFFFFFL) * 0x1p-30f + 1f, (mixBits & 0xFFFFL) * 0x1.8p-17f + 0.125f);
        value *= value * (3f - 2f * value);
        float start = (startBits >>> 1) * 0x0.ffffffp-63f;
        float end   = (endBits   >>> 1) * 0x0.ffffffp-63f;
        end = end - start + 1.5f;
        end -= (int)end + 0.5f;
        start += end * value + 1;
        return (start - (int)start);
    }
}