package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public abstract class WangRawNoise extends RawNoise {

    public abstract int getEdge();

    public abstract void setEdge(int edge);

    public abstract int[] getAltSeeds();

    @Override
    public abstract WangRawNoise stringDeserialize(String data);

    @Override
    public abstract WangRawNoise copy();

    /**
     * Allows serializing {@link WangRawNoise} objects with {@link #serialize(WangRawNoise)} and deserializing them with
     * {@link #deserialize(String)}. This requires an instance of the class to be serialized or deserialized to be
     * registered using {@link #register(WangRawNoise)} (typically once, though registering multiple times isn't a problem).
     * This is a purely-static utility class.
     */
    public static final class Serializer {
        /**
         * Not instantiable.
         */
        private Serializer() {
        }

        private static final ObjectMap<String, WangRawNoise> NOISE_BY_TAG = new ObjectMap<>(16);

        /**
         * Given a (typically freshly-constructed and never-reused) WangRawNoise, this registers that instance by its
         * {@link WangRawNoise#getTag()} in a Map, so that this type of WangRawNoise can be deserialized correctly by
         * {@link #deserialize(String)}. The WangRawNoise type must implement {@link WangRawNoise#getTag()},
         * {@link WangRawNoise#stringSerialize()}, {@link WangRawNoise#stringDeserialize(String)}, and {@link WangRawNoise#copy()}.
         *
         * @param random a (typically freshly-constructed) WangRawNoise that should never be reused elsewhere
         */
        public static void register(WangRawNoise random) {
            String tag = random.getTag();
            if (!NOISE_BY_TAG.containsKey(tag))
                NOISE_BY_TAG.put(tag, random);
            else
                System.err.println("When registering an WangRawNoise, a duplicate tag failed to register: " + tag);
        }

        static {
            NOISE_BY_TAG.put("(NO)", null); // for classes that cannot be serialized
            // TODO: register every type of Wang raw noise generator here, alphabetically
            register(new WangPerlinNoise(1));
            register(new WangPerlueNoise(1));
        }

        /**
         * Gets a copy of the WangRawNoise registered with the given tag, or null if this has nothing registered for the
         * given tag.
         *
         * @param tag a non-null String that could be used as a tag for an WangRawNoise registered with this class
         * @return a new copy of the corresponding WangRawNoise, or null if none was found
         */
        public static WangRawNoise get(String tag) {
            WangRawNoise r = NOISE_BY_TAG.get(tag);
            if (r == null) return null;
            return r.copy();
        }

        /**
         * Given an WangRawNoise that implements {@link #stringSerialize()} and {@link #getTag()}, this produces a
         * serialized String that stores the exact state of the WangRawNoise. This serialized String can be read back in by
         * {@link #deserialize(String)}.
         *
         * @param noise an WangRawNoise that implements {@link #stringSerialize()} and {@link #getTag()}
         * @return a String that can be read back in by {@link #deserialize(String)}
         */
        public static String serialize(WangRawNoise noise) {
            return noise.getTag() + noise.stringSerialize();
        }

        /**
         * Given a String produced by calling {@link #serialize(WangRawNoise)} on any registered implementation
         * (as with {@link #register(WangRawNoise)}), this reads in the deserialized data and returns a new WangRawNoise
         * of the appropriate type. This relies on the {@link WangRawNoise#getTag() tag} of the type being registered at
         * deserialization time, though it doesn't actually need to be registered at serialization time. This cannot
         * read back the direct output of {@link WangRawNoise#stringSerialize()}; it needs the tag prepended by
         * {@link #serialize(WangRawNoise)} to work.
         *
         * @param data serialized String data probably produced by {@link #serialize(WangRawNoise)}
         * @return a new WangRawNoise with the appropriate type internally, using the state from data
         */
        public static WangRawNoise deserialize(String data) {
            int idx = data.indexOf('`');
            if (idx == -1)
                throw new IllegalArgumentException("String given cannot represent a valid WangRawNoise.");
            String tagData = data.substring(0, idx);
            WangRawNoise root = NOISE_BY_TAG.get(tagData);
            if (root == null)
                throw new RuntimeException("Tag in given data is invalid or unknown.");
            return root.copy().stringDeserialize(data.substring(idx));
        }

        /**
         * Creates and returns a libGDX Array filled with copies of the WangRawNoise instances this has registered.
         * The Array will not contain any {@code null} items, nor will it contain the {@link ContinuousNoise} wrapper.
         * @return a new Array containing each WangRawNoise instance this has registered, copied
         */
        public static Array<WangRawNoise> getAll() {
            Array<WangRawNoise> noises = new Array<>(true, NOISE_BY_TAG.size, WangRawNoise.class);
            for(WangRawNoise n : NOISE_BY_TAG.values()){
                if(n == null || "ContinuousNoise".equals(n.getTag())) continue;
                noises.add(n.copy());
            }
            return noises;
        }
    }

}
