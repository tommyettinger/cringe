package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A substitute for the UUID class that isn't available on GWT.
 * The typical usage is to call {@link #next()} when you want a new UniqueIdentifier. If the app is closing down and
 * needs to save its state to be resumed later, {@link #GENERATOR} must be serialized as well, and deserialized before
 * calling {@link #next()} again after resuming. Without this last step, the generated identifiers are <em>likely</em>
 * to be unique, but not <em>guaranteed</em> to be unique.
 * <br>
 * This can be serialized as JSON, and so can (and must) the {@link #GENERATOR} that produces new UniqueIdentifier
 * instances and ensures they are unique. This is also Comparable, for some reason (UUID is, but since these should all
 * be random, it doesn't mean much). UniqueIdentifier supports up to 2 to the 128 minus 1 unique instances, which should
 * be far more than enough for centuries of generation.
 */
public final class UniqueIdentifier implements Comparable<UniqueIdentifier>, Json.Serializable, Externalizable {

    private long hi;
    private long lo;

    /**
     * Creates a new, invalid UniqueIdentifier. Both hi and lo will be 0.
     */
    public UniqueIdentifier(){
        hi = 0L;
        lo = 0L;
    }

    /**
     * Creates a new UniqueIdentifier that may or may not actually be unique. This uses hi and lo verbatim.
     * If both hi and lo are 0, this will be treated as an invalid identifier. Most usage should prefer
     * {@link #next()} instead.
     *
     * @param hi the high 64 bits, as a long
     * @param lo the low 64 bits, as a long
     */
    public UniqueIdentifier(long hi, long lo){
        this.hi = hi;
        this.lo = lo;
    }

    public long getHi() {
        return hi;
    }

    public long getLo() {
        return lo;
    }

    /**
     * @return false if this instance was produced by {@link #UniqueIdentifier()} and not modified; true otherwise
     */
    public boolean isValid() {
        return ((hi | lo) != 0L);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UniqueIdentifier that = (UniqueIdentifier) o;

        if (hi != that.hi) return false;
        return lo == that.lo;
    }

    @Override
    public int hashCode() {
        int result = (int) (hi ^ (hi >>> 32));
        result = 31 * result + (int) (lo ^ (lo >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "UniqueIdentifier{" +
                "hi=" + hi +
                ", lo=" + lo +
                '}';
    }

    /**
     * Serializes this UniqueIdentifier to a String, where it can be read back by {@link #stringDeserialize(String)}.
     * This is different from most other stringSerialize() methods in that it always produces a 33-character String,
     * consisting of {@link #getHi()}, then a {@code '_'}, then {@link #getLo()}, with hi and lo represented as unsigned
     * hex long Strings.
     * @return a 33-character-long String storing this identifier; can be read back with {@link #stringDeserialize(String)}
     */
    public String stringSerialize() {
        return MathSupport.appendUnsignedHex(MathSupport.appendUnsignedHex(new StringBuilder(33), hi).append('_'), lo).toString();
    }

    /**
     * Reads back a String produced by {@link #stringSerialize()}, storing the result in this UniqueIdentifier.
     * @param data a String almost certainly produced by {@link #stringSerialize()}
     * @return this UniqueIdentifier, after it has been modified.
     */
    public UniqueIdentifier stringDeserialize(String data) {
        hi = MathSupport.longFromHex(data, 0, 16);
        lo = MathSupport.longFromHex(data, 17, 33);
        return this;
    }

    @Override
    public void write(Json json) {
        json.writeObjectStart("ui");
        json.writeValue("h", hi);
        json.writeValue("l", lo);
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        jsonData = jsonData.get("ui");
        hi = jsonData.getLong("h");
        lo = jsonData.getLong("l");
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(hi);
        out.writeLong(lo);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException {
        hi = in.readLong();
        lo = in.readLong();
    }

    /**
     * The {@link Generator} that actually produces unique identifiers.
     * If your application pauses and needs to be resumed later by loading serialized state,
     * you must include this field in what you serialize, and load it before creating any
     * additional UniqueIdentifier values with {@link #next()} or {@link Generator#generate()}.
     * Failure to maintain the previous GENERATOR value can result in identifiers not being unique.
     */
    public static Generator GENERATOR = new Generator();

    /**
     * Generates a UniqueIdentifier that will actually be unique, assuming {@link #GENERATOR}
     * is non-null and has had its state tracked with the rest of the program (see the docs
     * for {@link #GENERATOR}).
     * @return a new UniqueIdentifier that should be actually unique
     */
    public static UniqueIdentifier next() {
        return GENERATOR.generate();
    }

    @Override
    public int compareTo(UniqueIdentifier other) {
        int c = Long.compare(hi, other.hi);
        return c == 0 ? Long.compare(lo, other.lo) : c;
    }

    /**
     * The type used as a factory to produce UniqueIdentifiers that are actually unique for a given Generator.
     * This is used in {@link UniqueIdentifier#GENERATOR}, and can be used independently via {@link #generate()}.
     */
    public static final class Generator implements Json.Serializable, Externalizable {
        private long stateA;
        private long stateB;

        /**
         * Creates a new Generator with one of (2 to the 64) possible random initial states.
         */
        public Generator() {
            stateA = Scramblers.scramble(System.currentTimeMillis()) ^ GdxRandom.seedFromMath();
            stateB = Scramblers.scramble(stateA);
        }

        /**
         * Creates a new Generator given two long values for state.
         * @param stateA may be any long
         * @param stateB may be any long unless both states are 0, in which case this is treated as 1
         */
        public Generator(long stateA, long stateB) {
            this.stateA = stateA;
            this.stateB = (stateA | stateB) == 0L ? 1L : stateB;
        }

        /**
         * Creates a new UniqueIdentifier, advancing the state of this Generator in the process.
         * @return a new UniqueIdentifier that will not occur again from this Generator unless (2 to the 128) - 1 more identifiers are generated
         */
        public UniqueIdentifier generate(){
            // xoroshiro algorithm
            final long s0 = stateA;
            final long s1 = stateB ^ s0;
            stateA = (s0 << 24 | s0 >>> 40) ^ s1 ^ (s1 << 16);
            stateB = (s1 << 37 | s1 >>> 27);
            return new UniqueIdentifier(stateA, stateB);
        }

        public String stringSerialize() {
            return MathSupport.appendUnsignedHex(MathSupport.appendUnsignedHex(new StringBuilder(33), stateA).append('$'), stateB).toString();
        }

        public Generator stringDeserialize(String data) {
            stateA = MathSupport.longFromHex(data, 0, 16);
            stateB = MathSupport.longFromHex(data, 17, 33);
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("uig");
            json.writeValue("a", stateA);
            json.writeValue("b", stateB);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("uig");
            stateA = jsonData.getLong("a");
            stateB = jsonData.getLong("b");
        }

        /**
         * The object implements the writeExternal method to save its contents
         * by calling the methods of DataOutput for its primitive values or
         * calling the writeObject method of ObjectOutput for objects, strings,
         * and arrays.
         *
         * @param out the stream to write the object to
         * @throws IOException Includes any I/O exceptions that may occur
         * @serialData Overriding methods should use this tag to describe
         * the data layout of this Externalizable object.
         * List the sequence of element types and, if possible,
         * relate the element to a public/protected field and/or
         * method of this Externalizable class.
         */
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeLong(stateA);
            out.writeLong(stateB);
        }

        /**
         * The object implements the readExternal method to restore its
         * contents by calling the methods of DataInput for primitive
         * types and readObject for objects, strings and arrays.  The
         * readExternal method must read the values in the same sequence
         * and with the same types as were written by writeExternal.
         *
         * @param in the stream to read data from in order to restore the object
         * @throws IOException            if I/O errors occur
         */
        @Override
        public void readExternal(ObjectInput in) throws IOException {
            stateA = in.readLong();
            stateB = in.readLong();
        }
    }
}
