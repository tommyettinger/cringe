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
 * A group of similar methods for getting hashes of points based on int coordinates in 2, 3, 4, or 6 dimensions and
 * an int for state. This hash has high enough quality to be useful as a source of random numbers based on positions.
 * The technique used here owes credit to Pelle Evensen for finding the significant quality increase from using multiple
 * bitwise rotations XORed together, and Martin Roberts for discovering the connection between higher dimensional ranks
 * and the appropriate numbers to gain similar qualities to adding the golden ratio mod 1 in 1D, using what had already
 * been named "harmonious numbers." The only case where this can return different results on GWT than on a desktop or
 * mobile JVM is when the inputs are GWT-specific out-of-range JS Numbers appearing to be ints, and that's a problem
 * with the input rather than the algorithm.
 */
public final class PointHasher {
    private PointHasher() {
    }

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
        s ^= x * 0x1827F5 ^ y * 0x123C21;
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
        s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
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
        s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
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
        s ^= x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281;
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
        s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
        return (s = (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493) ^ s >>> 11;
    }

    /**
     * A 8-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
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
        s ^= x * 0x1827F5 ^ y * 0x123C21;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }
    /**
     * A 8-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
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
        s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * A 8-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
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
        s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
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
        s ^= x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * A 8-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
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
        s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 24;
    }

    /**
     * A 6-bit point hash that smashes x and y into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y point with the given state s
     */
    public static int hash64(int x, int y, int s) {
        s ^= x * 0x1827F5 ^ y * 0x123C21;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26;
    }
    /**
     * A 6-bit point hash that smashes x, y, and z into s using XOR and multiplications by harmonious numbers,
     * then runs a simple unary hash on s and returns it. Has better performance than HastyPointHash, especially for
     * ints, and has slightly fewer collisions in a hash table of points. GWT-optimized. Inspired by Pelle Evensen's
     * rrxmrrxmsx_0 unary hash, though this doesn't use its code or its full algorithm. The unary hash used here has
     * been stripped down heavily, both for speed and because unless points are selected specifically to target
     * flaws in the hash, it doesn't need the intense resistance to bad inputs that rrxmrrxmsx_0 has.
     * @param x x position, as an int
     * @param y y position, as an int
     * @param z z position, as an int
     * @param s any int, a seed to be able to produce many hashes for a given point
     * @return 6-bit hash of the x,y,z point with the given state s
     */
    public static int hash64(int x, int y, int z, int s) {
        s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26;
    }

    /**
     * A 6-bit point hash that smashes x, y, z, and w into s using XOR and multiplications by harmonious numbers,
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
     * @return 6-bit hash of the x,y,z,w point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int s) {
        s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26;
    }

    /**
     * A 6-bit point hash that smashes x, y, z, w, and u into s using XOR and multiplications by harmonious
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
     * @return 6-bit hash of the x,y,z,w,u point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int u, int s) {
        s ^= x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26;
    }

    /**
     * A 6-bit point hash that smashes x, y, z, w, u, and v into s using XOR and multiplications by harmonious
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
     * @return 6-bit hash of the x,y,z,w,u,v point with the given state s
     */
    public static int hash64(int x, int y, int z, int w, int u, int v, int s) {
        s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 26;
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
        s ^= x * 0x1827F5 ^ y * 0x123C21;
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
        s ^= x * 0x1A36A9 ^ y * 0x157931 ^ z * 0x119725;
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
        s ^= x * 0x1B69E1 ^ y * 0x177C0B ^ z * 0x141E5D ^ w * 0x113C31;
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
        s ^= x * 0x1C3361 ^ y * 0x18DA39 ^ z * 0x15E6DB ^ w * 0x134D29 ^ u * 0x110281;
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
        s ^= x * 0x1CC1C5 ^ y * 0x19D7AF ^ z * 0x173935 ^ w * 0x14DEAF ^ u * 0x12C139 ^ v * 0x10DAA3;
        return (s ^ (s << 19 | s >>> 13) ^ (s << 5 | s >>> 27) ^ 0xD1B54A35) * 0x125493 >>> 27;
    }
}
