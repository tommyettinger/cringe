package com.github.tommyettinger.cringe;

public final class UniqueIdentifier {

    private long hi;
    private long lo;

    private UniqueIdentifier(){
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

    public static final Generator GENERATOR = new Generator();

    public static UniqueIdentifier next() {
        return GENERATOR.generate();
    }

    public static final class Generator {
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
    }
}
