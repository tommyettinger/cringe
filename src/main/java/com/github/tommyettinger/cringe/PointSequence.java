package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.Random;

public abstract class PointSequence<V extends Vector<V>> implements Iterator<V>, Iterable<V>, Json.Serializable, Externalizable {
    public abstract V nextInto(V into);

    /**
     * Always throws an UnsupportedOperationException; you cannot remove from an infinite sequence.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from an infinite sequence.");
    }

    /**
     * Always returns true; this sequence is infinite.
     * @return true
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Iterator<V> iterator() {
        return this;
    }

    /**
     * Gets the {@code index}-th element from the base-{@code base} van der Corput sequence. The base should usually be
     * a prime number. The index must be greater than 0 and should be less than 16777216. The number this returns is a
     * float between 0 (inclusive) and 1 (exclusive).
     *
     * @param base  a (typically) prime number to use as the base/radix of the van der Corput sequence
     * @param index the position in the sequence of the requested base, as a positive int
     * @return a quasi-random float between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static float vanDerCorput(final int base, final int index) {
        if (base <= 2) {
            return (Integer.reverse(index) >>> 8) * 0x1p-24f;
        }
        float denominator = base, res = 0.0f;
        int n = (index & 0x00ffffff);
        while (n > 0) {
            res += (n % base) / denominator;
            n /= base;
            denominator *= base;
        }
        return res;
    }

    /**
     * A very simple Iterator or Iterable over Vector2 items, this produces Vector2 points that won't overlap
     * or be especially close to each other for a long time by using a 2D
     * <a href="https://en.wikipedia.org/wiki/Halton_sequence">Halton Sequence</a>. If given no constructor
     * arguments, this uses a base of 2 for x and 3 for y. You can specify the two bases yourself (as long as
     * they share no common factors). if you want to resume the sequence, you need only the {@link #index} this
     * was on when you want to resume it, as well as possibly the {@link #baseX} and {@link #baseY} if those
     * differ, and can call {@link #resume(int, int, int)}.
     * All Vector2 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector2 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector2)} to fill an existing Vector2 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class Halton2 extends PointSequence<Vector2> {
        public int baseX, baseY, index;

        /**
         * Uses base (2,3) and starts at index 0.
         */
        public Halton2() {
            this(2, 3, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase and yBase is a good idea.
         * @param xBase Base for x, must not share any common factors with other bases (use a prime)
         * @param yBase Base for y, must not share any common factors with other bases (use a prime)
         */
        public Halton2(int xBase, int yBase) {
            this(xBase, yBase, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase and yBase is a good idea.
         * @param xBase Base for x, must not share any common factors with other bases (use a prime)
         * @param yBase Base for y, must not share any common factors with other bases (use a prime)
         * @param index which (usually small) positive index to start at; often starts at 0
         */
        public Halton2(int xBase, int yBase, int index) {
            this.baseX = xBase;
            this.baseY = yBase;
            this.index = index;
        }

        @Override
        public Vector2 next() {
            ++index;
            return new Vector2(vanDerCorput(baseX, index), vanDerCorput(baseY, index));
        }

        /**
         * Sets the x and y of {@code into} to the x and y of the next item in this Halton sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector2 nextInto(Vector2 into) {
            ++index;
            return into.set(vanDerCorput(baseX, index), vanDerCorput(baseY, index));
        }

        public Halton2 resume(int index){
            this.index = index;
            return this;
        }

        public Halton2 resume(int baseX, int baseY, int index){
            this.baseX = baseX;
            this.baseY = baseY;
            this.index = index;
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("hs");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("hs");
            baseX = jsonData.getInt("x");
            baseY = jsonData.getInt("y");
            index = jsonData.getInt("i");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(baseX);
            out.writeInt(baseY);
            out.writeInt(index);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            baseX = in.readInt();
            baseY = in.readInt();
            index = in.readInt();
        }

    }

    /**
     * A very simple Iterator or Iterable over Vector2 items, this produces Vector2 points that won't overlap
     * or be especially close to each other for a long time by using the
     * <a href="https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">R2 Sequence</a>.
     * This uses a slight variant credited to
     * <a href="https://www.martysmods.com/a-better-r2-sequence/">Pascal Gilcher's article</a>.
     * If constructed with no arguments, this gets random
     * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
     * resume the sequence, you only need the last Vector2 produced, and can call {@link #resume(Vector2)} with it.
     * All Vector2 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector2 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector2)} to fill an existing Vector2 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class R2 extends PointSequence<Vector2> {
        public float x, y;

        /**
         * Gets random initial offsets from {@link MathUtils#random}.
         */
        public R2() {
            this(MathUtils.random);
        }

        /**
         * Gets random initial offsets from the given {@link Random} (or subclass).
         * @param random any Random or a subclass of Random, such as RandomXS128
         */
        public R2(Random random) {
            this.x = random.nextFloat();
            this.y  = random.nextFloat();
        }

        /**
         * Uses the given initial offsets; this only uses their fractional parts.
         * @param x initial offset for x
         * @param y initial offset for y
         */
        public R2(float x, float y) {
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
        }

        public R2(Vector2 offsets) {
            this(offsets.x, offsets.y);
        }

        @Override
        public Vector2 next() {
            // These specific "magic numbers" are what make this the R2 sequence, as found here:
            // https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // These specific numbers are 1f minus the original constants, an approach to minimize
            // floating-point error noted by: https://www.martysmods.com/a-better-r2-sequence/
            x += 0.24512233375330728f;
            y += 0.4301597090019468f;
            x -= (int)x;
            y -= (int)y;
            return new Vector2(x, y);
        }

        /**
         * Sets the x and y of {@code into} to the x and y of the next item in the R2 sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector2 nextInto(Vector2 into) {
            x += 0.24512233375330728f;
            y += 0.4301597090019468f;
            x -= (int)x;
            y -= (int)y;
            return into.set(x, y);
        }

        public R2 resume(float x, float y){
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            return this;
        }

        public R2 resume(Vector2 previous) {
            return resume(previous.x, previous.y);
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("r2");
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("r2");
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
        }

    }
}
