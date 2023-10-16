package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public final class UniqueIdentifier implements Json.Serializable {

    private long hi;
    private long lo;

    public UniqueIdentifier(){
        hi = 0L;
        lo = 0L;
    }
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

    public static final Generator GENERATOR = new Generator();

    public static UniqueIdentifier next() {
        return GENERATOR.generate();
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
