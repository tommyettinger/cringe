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
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Defines the core operations to generate continuous noise with a specific algorithm, and declares what properties of
 * noise are supported by that algorithm.
 */
public abstract class RawNoise implements Json.Serializable{
    /**
     * Gets the minimum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 3 for one that is only defined for 3D or higher-dimensional spaces.
     * @return the minimum supported dimension, from 2 to 6 inclusive
     */
    public abstract int getMinDimension();

    /**
     * Gets the maximum dimension supported by this generator, such as 2 for a generator that only is defined for flat
     * surfaces, or 6 for one that is defined up to the highest dimension this interface knows about (6D).
     * @return the maximum supported dimension, from 2 to 6 inclusive
     */
    public abstract int getMaxDimension();

    /**
     * Returns true if this generator can be seeded with {@link #setSeed(int)} during each call to obtain noise, or
     * false if calling setSeed() is slow enough or allocates enough that alternative approaches should be used. You
     * can always call setSeed() on your own, but generators that don't have any seed won't do anything, and generators
     * that return false for this method will generally behave differently when comparing how
     * {@link #getNoiseWithSeed(float, float, int)} changes the seed and how setSeed() does.
     *
     * @return whether {@link #setSeed(int)} should be safe to call in every {@link #getNoiseWithSeed} call
     */
    public abstract boolean hasEfficientSetSeed();

    /**
     * Gets 1D noise with a default or pre-set seed. Most noise algorithms don't behave as well in 1D, so a common
     * approach for implementations is to delegate to one of the LineWobble methods.
     * @param x x position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 1D noise cannot be produced by this generator
     */
    public abstract float getNoise(float x);

    /**
     * Gets 2D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 2D noise cannot be produced by this generator
     */
    public abstract float getNoise(float x, float y);

    /**
     * Gets 3D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 3D noise cannot be produced by this generator
     */
    public abstract float getNoise(float x, float y, float z);

    /**
     * Gets 4D noise with a default or pre-set seed.
     * @param x x position; can be any finite float
     * @param y y position; can be any finite float
     * @param z z position; can be any finite float
     * @param w w position; can be any finite float
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 4D noise cannot be produced by this generator
     */
    public abstract float getNoise(float x, float y, float z, float w);

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
    public abstract float getNoise(float x, float y, float z, float w, float u);

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
    public abstract float getNoise(float x, float y, float z, float w, float u, float v);

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
    public abstract void setSeed(int seed);

    /**
     * Gets the current seed of the generator, as an int.
     * This must be implemented, but if the generator doesn't have a seed that can be expressed as an int (potentially
     * using {@link com.badlogic.gdx.utils.NumberUtils#floatToIntBits(float)}), this can just return {@code 0}.
     * @return the current seed, as an int
     */
    public abstract int getSeed();

    /**
     * Returns a String constant that should uniquely identify this RawNoise as well as possible; usually this is just
     * the class name.
     * If a duplicate tag is already registered and {@link Serializer#register(RawNoise)} attempts to register the same
     * tag again, a message is printed to {@code System.err}. The default implementation returns the String
     * {@code (NO)}, which is already registered in Serializer to a null value. Implementing this is required for any
     * usage of Serializer.
     * @return a short String constant that identifies this RawNoise type
     */
    public abstract String getTag();

    /**
     * Produces a String that describes everything needed to recreate this RawNoise in full. This String can be read back
     * in by {@link #stringDeserialize(String)} to reassign the described state to another RawNoise. The syntax here
     * should always start and end with the {@code `} (backtick) character, which is typically used by
     * {@link #stringDeserialize(String)} to identify the portion of a String that can be read back. The
     * {@code `} character should not be otherwise used unless to serialize another RawNoise that this uses.
     * @see Serializer#serialize(RawNoise) RawNoise.Serializer wraps this method so registered RawNoise types can be deserialized.
     * @return a String that describes this RawNoise for serialization
     */
    public abstract String stringSerialize();

    /**
     * Given a serialized String produced by {@link #stringSerialize()}, reassigns this RawNoise to have the described
     * state from the given String. The serialized String must have been produced by the same class as this object is.
     * @see Serializer#deserialize(String) RawNoise.Serializer uses this method to deserialize any registered RawNoise type.
     * @param data a serialized String, typically produced by {@link #stringSerialize()}
     * @return this RawNoise, after being modified (if possible)
     */
    public abstract RawNoise stringDeserialize(String data);

    /**
     * Creates a copy of this RawNoise, which should be a deep copy for any mutable state but can be shallow for immutable
     * types such as functions. This almost always just calls a copy constructor.
     * <br>
     * The default implementation throws an {@link UnsupportedOperationException} only. Implementors are strongly
     * encouraged to implement this in general, and that is required to use an RawNoise class with {@link Serializer}.
     * @return a copy of this RawNoise
     */
    public abstract RawNoise copy();

    /**
     * Gets 1D noise with a specific seed. If the seed cannot be retrieved or changed per-call, then this falls back to
     * changing the position instead of the seed; you can check if this will happen with {@link #hasEfficientSetSeed()}.
     * Most noise algorithms don't
     * behave as well in 1D, so a common approach for implementations is to delegate to one of the LineWobble methods.
     * @param x x position; can be any finite float
     * @param seed any int; must be the same between calls for the noise to be continuous
     * @return a noise value between -1.0f and 1.0f, both inclusive
     * @throws UnsupportedOperationException if 1D noise cannot be produced by this generator
     */
    public float getNoiseWithSeed(float x, int seed){
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1.9E3779B9p-16f;
            return getNoise(x + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x);
        setSeed(s);
        return r;
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
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1p-16f;
            return getNoise(x + s, y + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y);
        setSeed(s);
        return r;
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
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1p-16f;
            return getNoise(x + s, y + s, z + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z);
        setSeed(s);
        return r;
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
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1p-16f;
            return getNoise(x + s, y + s, z + s, w + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w);
        setSeed(s);
        return r;
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
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1p-16f;
            return getNoise(x + s, y + s, z + s, w + s, u + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w, u);
        setSeed(s);
        return r;
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
        if(!hasEfficientSetSeed()) {
            float s = seed * 0x1p-16f;
            return getNoise(x + s, y + s, z + s, w + s, u + s, v + s);
        }
        final int s = getSeed();
        setSeed(seed);
        final float r = getNoise(x, y, z, w, u, v);
        setSeed(s);
        return r;
    }

    @Override
    public void write(Json json) {
        json.writeValue("n", stringSerialize());
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        stringDeserialize(jsonData.getString("n"));
    }

    /**
     * Writes the state of this GdxRandom instance into a String JsonValue. This overload does not write to a child
     * of the given JsonValue, and instead {@link JsonValue#set(String) sets} the JsonValue directly.
     * @param modifying the JsonValue that will have this added as a child using the given key name
     */
    public void writeToJsonValue(JsonValue modifying) {
        modifying.set(stringSerialize());
    }

    /**
     * Reads the state of this GdxRandom instance from a String JsonValue.
     * @param value the JsonValue that this will look the given key name up in
     */
    public void readFromJsonValue(JsonValue value) {
        String string = value.asString();
        if(string != null) {
            stringDeserialize(string);
        }
    }

    /**
     * Writes the state of this GdxRandom instance into a String child of the given JsonValue.
     * @param parent the JsonValue that will have this added as a child using the given key name
     * @param key the name to store the GdxRandom into
     */
    public void writeToJsonValue(JsonValue parent, String key) {
        parent.addChild(key, new JsonValue(stringSerialize()));
    }

    /**
     * Reads the state of this GdxRandom instance from a String child of the given JsonValue.
     * @param parent the JsonValue that this will look the given key name up in
     * @param key the name to read the GdxRandom data from
     */
    public void readFromJsonValue(JsonValue parent, String key) {
        String string = parent.getString(key, null);
        if(string != null) {
            stringDeserialize(string);
        }
    }

    /**
     * Allows serializing {@link RawNoise} objects with {@link #serialize(RawNoise)} and deserializing them with
     * {@link #deserialize(String)}. This requires an instance of the class to be serialized or deserialized to be
     * registered using {@link #register(RawNoise)} (typically once, though registering multiple times isn't a problem).
     * This is a purely-static utility class.
     */
    public static class Serializer {
        /**
         * Not instantiable.
         */
        private Serializer() {
        }

        private static final ObjectMap<String, RawNoise> NOISE_BY_TAG = new ObjectMap<>(16);

        /**
         * Given a (typically freshly-constructed and never-reused) RawNoise, this registers that instance by its
         * {@link RawNoise#getTag()} in a Map, so that this type of RawNoise can be deserialized correctly by
         * {@link #deserialize(String)}. The RawNoise type must implement {@link RawNoise#getTag()},
         * {@link RawNoise#stringSerialize()}, {@link RawNoise#stringDeserialize(String)}, and {@link RawNoise#copy()}.
         *
         * @param random a (typically freshly-constructed) RawNoise that should never be reused elsewhere
         */
        public static void register(RawNoise random) {
            String tag = random.getTag();
            if (!NOISE_BY_TAG.containsKey(tag))
                NOISE_BY_TAG.put(tag, random);
            else
                System.err.println("When registering an RawNoise, a duplicate tag failed to register: " + tag);
        }

        static {
            NOISE_BY_TAG.put("(NO)", null); // for classes that cannot be serialized
            // TODO: register every type of raw noise generator here
            register(new CyclicNoise(1, 1));
            register(new FoamNoise(1));
            register(new HoneyNoise(1));
            register(new PerlinNoise(1));
            register(new SimplexNoise(1));
            register(new SorbetNoise(1, 1));
            register(new ValueNoise(1));

            register(new ContinuousNoise(new ValueNoise(1)));
        }

        /**
         * Gets a copy of the RawNoise registered with the given tag, or null if this has nothing registered for the
         * given tag.
         *
         * @param tag a non-null String that could be used as a tag for an RawNoise registered with this class
         * @return a new copy of the corresponding RawNoise, or null if none was found
         */
        public static RawNoise get(String tag) {
            RawNoise r = NOISE_BY_TAG.get(tag);
            if (r == null) return null;
            return r.copy();
        }

        /**
         * Given an RawNoise that implements {@link #stringSerialize()} and {@link #getTag()}, this produces a
         * serialized String that stores the exact state of the RawNoise. This serialized String can be read back in by
         * {@link #deserialize(String)}.
         *
         * @param noise an RawNoise that implements {@link #stringSerialize()} and {@link #getTag()}
         * @return a String that can be read back in by {@link #deserialize(String)}
         */
        public static String serialize(RawNoise noise) {
            return noise.getTag() + noise.stringSerialize();
        }

        /**
         * Given a String produced by calling {@link #serialize(RawNoise)} on any registered implementation
         * (as with {@link #register(RawNoise)}), this reads in the deserialized data and returns a new RawNoise
         * of the appropriate type. This relies on the {@link RawNoise#getTag() tag} of the type being registered at
         * deserialization time, though it doesn't actually need to be registered at serialization time. This cannot
         * read back the direct output of {@link RawNoise#stringSerialize()}; it needs the tag prepended by
         * {@link #serialize(RawNoise)} to work.
         *
         * @param data serialized String data probably produced by {@link #serialize(RawNoise)}
         * @return a new RawNoise with the appropriate type internally, using the state from data
         */
        public static RawNoise deserialize(String data) {
            int idx = data.indexOf('`');
            if (idx == -1)
                throw new IllegalArgumentException("String given cannot represent a valid RawNoise.");
            String tagData = data.substring(0, idx);
            RawNoise root = NOISE_BY_TAG.get(tagData);
            if (root == null)
                throw new RuntimeException("Tag in given data is invalid or unknown.");
            return root.copy().stringDeserialize(data.substring(idx));
        }
    }
}
