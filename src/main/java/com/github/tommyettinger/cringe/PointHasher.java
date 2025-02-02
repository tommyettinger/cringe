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

/**
 * A group of similar methods for getting hashes of points based on int coordinates in 2, 3, 4, 5, or 6 dimensions and
 * an int for state. This hash has high enough quality to be useful as a source of random numbers based on positions.
 * The technique used here owes credit to Pelle Evensen for finding the significant quality increase from using multiple
 * bitwise rotations XORed together, and Martin Roberts for discovering the connection between higher dimensional ranks
 * and the appropriate numbers to gain similar qualities to adding the golden ratio mod 1 in 1D, using what had already
 * been named "harmonious numbers." This class doesn't use the exact harmonious numbers (which are irrational numbers),
 * but instead takes 2 to the 22 divided by that harmonious number and finds the next probable prime greater than or
 * equal to that. Using primes may have advantages when there are lots of products being mixed like this.
 * <br>
 * The only case where this can return different results on GWT than on a desktop or
 * mobile JVM is when the inputs are GWT-specific out-of-range JS Numbers appearing to be ints, and that's a problem
 * with the input rather than the algorithm.
 */
public final class PointHasher {
    private PointHasher() {
    }

 */
    public static final int
        X2 = 0x1827F5, Y2 = 0x123C3B,
        X3 = 0x1A36BF, Y3 = 0x157931, Z3 = 0x119749,
        X4 = 0x1B69E5, Y4 = 0x177C1F, Z4 = 0x141E75, W4 = 0x113C33,
        X5 = 0x1C3367, Y5 = 0x18DA39, Z5 = 0x15E6E3, W5 = 0x134D49, U5 = 0x110281,
        X6 = 0x1CC205, Y6 = 0x19D7B5, Z6 = 0x173935, W6 = 0x14DEC5, U6 = 0x12C139, V6 = 0x10DAAD;
    /**
     * A 32-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y point with the given state s
     */
    public static int hashAll(int x, int y, int s) {
        s ^= x * X2 ^ y * Y2;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }
    /**
     * A 32-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z point with the given state s
     */
    public static int hashAll(int x, int y, int z, int s) {
        s ^= x * X3 ^ y * Y3 ^ z * Z3;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }

    /**
     * A 32-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 32-bit hash of the x,y,z,w point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int s) {
        s ^= x * X4 ^ y * Y4 ^ z * Z4 ^ w * W4;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }

    /**
     * A 32-bit point hash that smashes x, y, z, w, and u into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 32-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int s) {
        s ^= x * X5 ^ y * Y5 ^ z * Z5 ^ w * W5 ^ u * U5;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }

    /**
     * A 32-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 32-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hashAll(int x, int y, int z, int w, int u, int v, int s) {
        s ^= x * X6 ^ y * Y6 ^ z * Z6 ^ w * W6 ^ u * U6 ^ v * V6;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }

    /**
     * An 8-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y point with the given state s
     */
    public static int hash256(int x, int y, int s) {
        s ^= x * X2 ^ y * Y2;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }
    /**
     * An 8-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z point with the given state s
     */
    public static int hash256(int x, int y, int z, int s) {
        s ^= x * X3 ^ y * Y3 ^ z * Z3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * An 8-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int s) {
        s ^= x * X4 ^ y * Y4 ^ z * Z4 ^ w * W4;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }
    /**
     * An 8-bit point hash that smashes x, y, z, w, and u into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 8-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int s) {
        s ^= x * X5 ^ y * Y5 ^ z * Z5 ^ w * W5 ^ u * U5;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * An 8-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 8-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash256(int x, int y, int z, int w, int u, int v, int s) {
        s ^= x * X6 ^ y * Y6 ^ z * Z6 ^ w * W6 ^ u * U6 ^ v * V6;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * A 5-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y point with the given state s
     */
    public static int hash32(int x, int y, int s) {
        s ^= x * X2 ^ y * Y2;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }
    /**
     * A 5-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z point with the given state s
     */
    public static int hash32(int x, int y, int z, int s) {
        s ^= x * X3 ^ y * Y3 ^ z * Z3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }

    /**
     * A 5-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int s) {
        s ^= x * X4 ^ y * Y4 ^ z * Z4 ^ w * W4;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }

    /**
     * A 5-bit point hash that smashes x, y, z, w, and u into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 5-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int u, int s) {
        s ^= x * X5 ^ y * Y5 ^ z * Z5 ^ w * W5 ^ u * U5;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }

    /**
     * A 5-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
     * numbers, then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash,
     * especially for ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by
     * Pelle Evensen's rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary
     * hash used here has been stripped down heavily, both for speed and because unless points are selected
     * specifically to target flaws in the hash, it doesn't need the intense resistance to bad inputs that
     * rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param w w position, as an int
     * @param u u position, as an int
     * @param v v position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point 
     * @return 5-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash32(int x, int y, int z, int w, int u, int v, int s) {
        s ^= x * X6 ^ y * Y6 ^ z * Z6 ^ w * W6 ^ u * U6 ^ v * V6;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }
}
