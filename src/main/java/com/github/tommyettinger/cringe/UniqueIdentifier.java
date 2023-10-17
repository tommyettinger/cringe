package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

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
public final class UniqueIdentifier implements Comparable<UniqueIdentifier>, Json.Serializable {

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

    public String stringSerialize() {
        return hi + "~" + lo;
    }

    public UniqueIdentifier stringDeserialize(String data) {
        int mid = data.indexOf('~');
        hi = Long.parseLong(data.substring(0, mid));
        lo = Long.parseLong(data.substring(mid+1));
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

    public static final class Generator implements Json.Serializable {
        private long stateA;
        private long stateB;
        private Generator() {
            stateA = Scramblers.scramble(System.currentTimeMillis()) ^ GdxRandom.seedFromMath();
            stateB = Scramblers.scramble(stateA);
        }
        public UniqueIdentifier generate(){
            // xoroshiro algorithm
            final long s0 = stateA;
            long s1 = stateB;
            s1 ^= s0;
            stateA = (s0 << 24 | s0 >>> 40) ^ s1 ^ (s1 << 16);
            stateB = (s1 << 37 | s1 >>> 27);
            return new UniqueIdentifier(stateA, stateB);
        }
        public String stringSerialize() {
            return stateA + "~" + stateB;
        }

        public Generator stringDeserialize(String data) {
            int mid = data.indexOf('~');
            stateA = Long.parseLong(data.substring(0, mid));
            stateB = Long.parseLong(data.substring(mid+1));
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
    }
}
