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

/**
 * Provides 1D noise methods that can be queried at any point on a line to get a continuous random value.
 * These each use some form of low-quality, high-speed unary hash on the floor and ceiling of a float or double value,
 * then interpolate between the hash results. Some of these work on angles (so they wrap like a line around a circle)
 * instead of normal lines.
 * <br>
 * Some methods here were named... hastily, but the names stuck.
 * <ul>
 *     <li>{@link #wobble(int, float)} is straightforward; it uses a hermite spline to interpolate two points.</li>
 *     <li>{@link #bicubicWobble(long, float)} uses bicubic interpolation on 4 points, and tends to be smooth.</li>
 *     <li>{@link #splineWobble(int, float)} can be much more sharp or smooth, varying randomly over its length.</li>
 *     <li>{@link #trigWobble(int, float)} is a trigonometric wobble, using sine to smoothly transition.</li>
 * </ul>
 */
public class LineWobble {
    /**
     * A type of 1D noise that takes an int seed and is optimized for usage on GWT. This uses cubic interpolation
     * between random peak or valley points.
     * @param seed an int seed that will determine the pattern of peaks and valleys this will generate as value changes; this should not change between calls
     * @param value a float that typically changes slowly, by less than 1.0, with direction changes at integer inputs
     * @return a pseudo-random float between -1f and 1f (both exclusive), smoothly changing with value
     */
    public static float wobble(int seed, float value)
    {
        final int floor = ((int)(value + 0x1p14) - 0x4000);
        // the three lines below break up multiplications into component parts to avoid precision loss on GWT.
        final int z = seed + (floor * 0x22179 | 0) * 0x4A41; // this is the same as: seed + floor * 0x9E3779B9
        final int start = ((z ^ 0xD1B54A35) * 0x923EF | 0) * 0x100D | 0; // the same as: (z ^ 0xD1B54A35) * 0x92B5C323
        final int end   = ((z + 0x9E3779B9 ^ 0xD1B54A35) * 0x923EF | 0) * 0x100D | 0; // also * 0x92B5C323
        value -= floor;
        value *= value * (3 - 2 * value);
        return ((1 - value) * start + value * end) * 0x0.ffffffp-31f;
    }

    /**
     * Sway smoothly using bicubic interpolation between 4 points (the two integers before t and the two after).
     * This pretty much never produces steep changes between peaks and valleys; this may make it more useful for things
     * like generating terrain that can be walked across in a side-scrolling game.
     * @param seed any long
     * @param t a distance traveled; should change by less than 1 between calls, and should be less than about 10000
     * @return a smoothly-interpolated swaying value between -1 and 1, both exclusive
     */
    public static float bicubicWobble(long seed, float t)
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
}