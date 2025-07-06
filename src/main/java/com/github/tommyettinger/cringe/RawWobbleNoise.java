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

import com.badlogic.gdx.utils.Json;

import java.io.Externalizable;

/**
 * Defines the core operations to generate continuous noise with a specific algorithm, and declares what properties of
 * noise are supported by that algorithm.
 */
public abstract class RawWobbleNoise extends RawNoise implements Json.Serializable, Externalizable {
    public int seed = 1;
    /**
     * Gets the minimum dimension supported by this generator, which is 1 here.
     * @return the minimum supported dimension, here 1
     */
    public int getMinDimension() {
        return 1;
    }

    /**
     * Gets the maximum dimension supported by this generator, which is 6 here.
     * @return the maximum supported dimension, here 6
     */
    public int getMaxDimension() {
        return 6;
    }

    /**
     * Returns true if this generator can be seeded with {@link #setSeed(int)} during each call to obtain noise, which
     * is always true here.
     *
     * @return true, always
     */
    public boolean hasEfficientSetSeed(){
        return true;
    }

    /**
     * Gets 2D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    public float getNoise(float x, float y){
        return getNoiseWithSeed(x, y, seed);
    }

    /**
     * Gets 3D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    public float getNoise(float x, float y, float z){
        return getNoiseWithSeed(x, y, z, seed);
    }

    /**
     * Gets 4D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    public float getNoise(float x, float y, float z, float w){
        return getNoiseWithSeed(x, y, z, w, seed);
    }

    /**
     * Gets 5D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    public float getNoise(float x, float y, float z, float w, float u){
        return getNoiseWithSeed(x, y, z, w, u, seed);
    }

    /**
     * Gets 6D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    public float getNoise(float x, float y, float z, float w, float u, float v){
        return getNoiseWithSeed(x, y, z, w, u, v, seed);
    }

    /**
     * Sets the seed to the given int, if int seeds are supported.
     * If this generator cannot be seeded, this should do nothing, and should not throw an exception. If this operation
     * allocates or is time-intensive, then {@link #hasEfficientSetSeed()} should return false. That method is checked
     * in {@link #getNoiseWithSeed}, and if it returns false, the noise call will avoid calling setSeed(). You can
     * always at least try to set the seed, even if it does nothing or is heavy on performance, and doing it a few times
     * each frame should typically be fine for any generator. In the case this is called thousands of times each frame,
     * check {@link #hasEfficientSetSeed()}.
     *
     * @param seed an int seed, with no restrictions unless otherwise documented
     */
    public void setSeed(int seed){
        this.seed = seed;
    }

    /**
     * Gets the current seed of the generator, as an int.
     * This must be implemented, but if the generator doesn't have a seed that can be expressed as an int (potentially
     * using {@link com.badlogic.gdx.utils.NumberUtils#floatToIntBits(float)}), this can just return {@code 0}.
     * @return the current seed, as an int
     */
    public int getSeed(){
        return seed;
    }

    @Override
    public String stringSerialize() {
        return "`" + seed + "`";
    }

    @Override
    public RawWobbleNoise stringDeserialize(String data) {
        setSeed(MathSupport.intFromDec(data, 1, data.length() - 1));
        return this;
    }

    /**
     * Creates a copy of this RawNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. Implementors are strongly
     * encouraged to implement this in general, and that is required to use an RawNoise class with {@link Serializer}.
     * @return a copy of this RawNoise
     */
    public abstract RawWobbleNoise copy();

    /**
     * Gets a simple human-readable String that describes this noise generator. This should use names instead of coded
     * numbers, and should be enough to differentiate any two generators.
     * @return a String that describes this noise generator for human consumption
     */
    public String toHumanReadableString(){
        return getTag() + " with seed " + getSeed();
    }

    /**
     * Gets 1D noise with this generator's {@link #getSeed() seed}.
     *
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoise(float x) {
        return getNoiseWithSeed(x, seed);
    }

    /**
     * Gets 1D noise with a specific seed.
     * <br>
     * This should be overridden to call a method from {@link LineWobble}.
     *
     * @param x    x position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     */
    public float getNoiseWithSeed(float x, int seed) {
        return LineWobble.bicubicWobble(x, seed);
    }

    /**
     * Gets 2D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, float y, int seed) {
        float a = getNoiseWithSeed(x, seed--);
        float b = getNoiseWithSeed(y, seed--);
        return getNoiseWithSeed((a + b) * 3f + (x + y) * (1f/2f), seed);
    }

    /**
     * Gets 3D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        float a = getNoiseWithSeed(x, seed--);
        float b = getNoiseWithSeed(y, seed--);
        float c = getNoiseWithSeed(z, seed--);
        return getNoiseWithSeed((a + b + c) * 2f + (x + y + z) * (1f/3f), seed);
    }

    /**
     * Gets 4D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        float a = getNoiseWithSeed(x, seed--);
        float b = getNoiseWithSeed(y, seed--);
        float c = getNoiseWithSeed(z, seed--);
        float d = getNoiseWithSeed(w, seed--);
        return getNoiseWithSeed((a + b + c + d) * 1.5f + (x + y + z + w) * (1f/4f), seed);
    }

    /**
     * Gets 5D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 5D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        float a = getNoiseWithSeed(x, seed--);
        float b = getNoiseWithSeed(y, seed--);
        float c = getNoiseWithSeed(z, seed--);
        float d = getNoiseWithSeed(w, seed--);
        float e = getNoiseWithSeed(u, seed--);
        return getNoiseWithSeed((a + b + c + d + e) * 1.2f + (x + y + z + w + u) * (1f/5f), seed);
    }

    /**
     * Gets 6D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @param u u position; can be any finite float
     * @param v v position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 6D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        float a = getNoiseWithSeed(x, seed--);
        float b = getNoiseWithSeed(y, seed--);
        float c = getNoiseWithSeed(z, seed--);
        float d = getNoiseWithSeed(w, seed--);
        float e = getNoiseWithSeed(u, seed--);
        float f = getNoiseWithSeed(v, seed--);
        return getNoiseWithSeed((a + b + c + d + e + f) + (x + y + z + w + u + v) * (1f/6f), seed);
    }
}
