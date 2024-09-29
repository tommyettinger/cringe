package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.Random;

/**
 * A parent type for infinite point sequences, such as Halton sequences and the R2 sequence. This has several inner
 * classes that extend PointSequence, currently all "low-discrepancy sequences." {@link R2} will look more random for
 * the first several elements than the similar {@link Halton2} sequence, and R2 will maintain a higher minimum distance
 * between points than Halton will. R2 will start to look more patterned than Halton after enough points have been
 * produced to see repeating line patterns in R2 that aren't present in Halton sequences.
 * <br>
 * All PointSequence subclasses can be serialized using libGDX Json or with anything compatible with
 * {@link Externalizable}, such as Apache Fury. They can be iterated over like any {@link Iterable}, or they can be
 * treated as an {@link Iterator}; both return {@code V} items.
 *
 * @param <V> the libGDX {@link Vector} type of points, such as {@link Vector2} or {@link Vector3}
 */
public abstract class PointSequence<V extends Vector<V>> implements Iterator<V>, Iterable<V>, Json.Serializable, Externalizable {
    /**
     * Gets the next point in this sequence and fills {@code into} with its contents instead of allocating any
     * new vector object.
     * @param into a Vector of the appropriate type for this PointSequence
     * @return {@code into}, after modifications
     */
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

    public abstract PointSequence<V> copy();

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
     * they share no common factors). If you want to resume the sequence, you need only the {@link #index} this
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
            json.writeObjectStart("h2");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("h2");
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

        @Override
        public Halton2 copy() {
            return new Halton2(baseX, baseY, index);
        }
    }

    /**
     * A very simple Iterator or Iterable over Vector3 items, this produces Vector3 points that won't overlap
     * or be especially close to each other for a long time by using a 3D
     * <a href="https://en.wikipedia.org/wiki/Halton_sequence">Halton Sequence</a>. If given no constructor
     * arguments, this uses a base of 2 for x, 3 for y, and 5 for z. You can specify the bases yourself (as long
     * as they share no common factors). If you want to resume the sequence, you need only the {@link #index}
     * this was on when you want to resume it, as well as possibly the {@link #baseX}, {@link #baseY}, and
     * {@link #baseZ} if those differ, and can call {@link #resume(int, int, int, int)}.
     * All Vector3 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector3 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector3)} to fill an existing Vector3 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class Halton3 extends PointSequence<Vector3> {
        public int baseX, baseY, baseZ, index;

        /**
         * Uses base (2,3,5) and starts at index 0.
         */
        public Halton3() {
            this(2, 3, 5, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, and zBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         */
        public Halton3(int xBase, int yBase, int zBase) {
            this(xBase, yBase, zBase, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, and zBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param index which (usually small) positive index to start at; often starts at 0
         */
        public Halton3(int xBase, int yBase, int zBase, int index) {
            this.baseX = xBase;
            this.baseY = yBase;
            this.baseZ = zBase;
            this.index = index;
        }

        @Override
        public Vector3 next() {
            ++index;
            return new Vector3(vanDerCorput(baseX, index), vanDerCorput(baseY, index), vanDerCorput(baseZ, index));
        }

        /**
         * Sets the x,y,z of {@code into} to the x,y,z of the next item in this Halton sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector3 nextInto(Vector3 into) {
            ++index;
            return into.set(vanDerCorput(baseX, index), vanDerCorput(baseY, index), vanDerCorput(baseZ, index));
        }

        public Halton3 resume(int index){
            this.index = index;
            return this;
        }

        public Halton3 resume(int baseX, int baseY, int baseZ, int index){
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.index = index;
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("h3");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("z", baseZ);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("h3");
            baseX = jsonData.getInt("x");
            baseY = jsonData.getInt("y");
            baseZ = jsonData.getInt("z");
            index = jsonData.getInt("i");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(baseX);
            out.writeInt(baseY);
            out.writeInt(baseZ);
            out.writeInt(index);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            baseX = in.readInt();
            baseY = in.readInt();
            baseZ = in.readInt();
            index = in.readInt();
        }

        @Override
        public Halton3 copy() {
            return new Halton3(baseX, baseY, baseZ, index);
        }
    }
    
    /**
     * A very simple Iterator or Iterable over Vector4 items, this produces Vector4 points that won't overlap
     * or be especially close to each other for a long time by using a 4D
     * <a href="https://en.wikipedia.org/wiki/Halton_sequence">Halton Sequence</a>. If given no constructor
     * arguments, this uses a base of 2 for x, 3 for y, 5 for z, and 7 for w. You can specify the bases yourself (as
     * long as they share no common factors). If you want to resume the sequence, you need only the {@link #index}
     * this was on when you want to resume it, as well as possibly the {@link #baseX}, {@link #baseY}, {@link #baseZ},
     * and {@link #baseW} if those differ, and can call {@link #resume(int, int, int, int, int)}.
     * All Vector4 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector4 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector4)} to fill an existing Vector4 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class Halton4 extends PointSequence<Vector4> {
        public int baseX, baseY, baseZ, baseW, index;

        /**
         * Uses base (2,3,5,7) and starts at index 0.
         */
        public Halton4() {
            this(2, 3, 5, 7, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, and wBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         */
        public Halton4(int xBase, int yBase, int zBase, int wBase) {
            this(xBase, yBase, zBase, wBase, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, and wBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         * @param index which (usually small) positive index to start at; often starts at 0
         */
        public Halton4(int xBase, int yBase, int zBase, int wBase, int index) {
            this.baseX = xBase;
            this.baseY = yBase;
            this.baseZ = zBase;
            this.baseW = wBase;
            this.index = index;
        }

        @Override
        public Vector4 next() {
            ++index;
            return new Vector4(vanDerCorput(baseX, index), vanDerCorput(baseY, index),
                    vanDerCorput(baseZ, index), vanDerCorput(baseW, index));
        }

        /**
         * Sets the x,y,z,w of {@code into} to the x,y,z,w of the next item in this Halton sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector4 nextInto(Vector4 into) {
            ++index;
            return into.set(vanDerCorput(baseX, index), vanDerCorput(baseY, index),
                    vanDerCorput(baseZ, index), vanDerCorput(baseW, index));
        }

        public Halton4 resume(int index){
            this.index = index;
            return this;
        }

        public Halton4 resume(int baseX, int baseY, int baseZ, int baseW, int index){
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.baseW = baseW;
            this.index = index;
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("h4");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("z", baseZ);
            json.writeValue("w", baseW);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("h4");
            baseX = jsonData.getInt("x");
            baseY = jsonData.getInt("y");
            baseZ = jsonData.getInt("z");
            baseW = jsonData.getInt("w");
            index = jsonData.getInt("i");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(baseX);
            out.writeInt(baseY);
            out.writeInt(baseZ);
            out.writeInt(baseW);
            out.writeInt(index);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            baseX = in.readInt();
            baseY = in.readInt();
            baseZ = in.readInt();
            baseW = in.readInt();
            index = in.readInt();
        }

        @Override
        public Halton4 copy() {
            return new Halton4(baseX, baseY, baseZ, baseW, index);
        }
    }
    
    /**
     * A very simple Iterator or Iterable over Vector5 items, this produces Vector5 points that won't overlap
     * or be especially close to each other for a long time by using a 5D
     * <a href="https://en.wikipedia.org/wiki/Halton_sequence">Halton Sequence</a>. If given no constructor
     * arguments, this uses a base of 2 for x, 3 for y, 5 for z, 7 for w, and 11 for u. You can specify the bases
     * yourself (as long as they share no common factors). If you want to resume the sequence, you need only the
     * {@link #index} this was on when you want to resume it, as well as possibly the {@link #baseX}, {@link #baseY},
     * {@link #baseZ}, {@link #baseW} and {@link #baseU} if those differ, and can call {@link #resume(int, int, int, int, int, int)}.
     * All Vector5 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector5 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector5)} to fill an existing Vector5 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class Halton5 extends PointSequence<Vector5> {
        public int baseX, baseY, baseZ, baseW, baseU, index;

        /**
         * Uses base (2,3,5,7,11) and starts at index 0.
         */
        public Halton5() {
            this(2, 3, 5, 7, 11, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, wBase, and uBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         * @param uBase base for u, must not share any common factors with other bases (use a prime)
         */
        public Halton5(int xBase, int yBase, int zBase, int wBase, int uBase) {
            this(xBase, yBase, zBase, wBase, uBase, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, wBase, and uBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         * @param uBase base for u, must not share any common factors with other bases (use a prime)
         * @param index which (usually small) positive index to start at; often starts at 0
         */
        public Halton5(int xBase, int yBase, int zBase, int wBase, int uBase, int index) {
            this.baseX = xBase;
            this.baseY = yBase;
            this.baseZ = zBase;
            this.baseW = wBase;
            this.baseU = uBase;
            this.index = index;
        }

        @Override
        public Vector5 next() {
            ++index;
            return new Vector5(vanDerCorput(baseX, index), vanDerCorput(baseY, index),
                    vanDerCorput(baseZ, index), vanDerCorput(baseW, index), vanDerCorput(baseU, index));
        }

        /**
         * Sets the x,y,z,w,u of {@code into} to the x,y,z,w,u of the next item in this Halton sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector5 nextInto(Vector5 into) {
            ++index;
            return into.set(vanDerCorput(baseX, index), vanDerCorput(baseY, index),
                    vanDerCorput(baseZ, index), vanDerCorput(baseW, index), vanDerCorput(baseU, index));
        }

        public Halton5 resume(int index){
            this.index = index;
            return this;
        }

        public Halton5 resume(int baseX, int baseY, int baseZ, int baseW, int baseU, int index){
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.baseW = baseW;
            this.baseU = baseU;
            this.index = index;
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("h5");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("z", baseZ);
            json.writeValue("w", baseW);
            json.writeValue("u", baseU);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("h5");
            baseX = jsonData.getInt("x");
            baseY = jsonData.getInt("y");
            baseZ = jsonData.getInt("z");
            baseW = jsonData.getInt("w");
            baseU = jsonData.getInt("u");
            index = jsonData.getInt("i");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(baseX);
            out.writeInt(baseY);
            out.writeInt(baseZ);
            out.writeInt(baseW);
            out.writeInt(baseU);
            out.writeInt(index);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            baseX = in.readInt();
            baseY = in.readInt();
            baseZ = in.readInt();
            baseW = in.readInt();
            baseU = in.readInt();
            index = in.readInt();
        }

        @Override
        public Halton5 copy() {
            return new Halton5(baseX, baseY, baseZ, baseW, baseU, index);
        }
    }
    
    /**
     * A very simple Iterator or Iterable over Vector6 items, this produces Vector6 points that won't overlap
     * or be especially close to each other for a long time by using a 6D
     * <a href="https://en.wikipedia.org/wiki/Halton_sequence">Halton Sequence</a>. If given no constructor
     * arguments, this uses a base of 2 for x, 3 for y, 5 for z, 7 for w, 11 for u, and 13 for v. You can specify the
     * bases yourself (as long as they share no common factors). If you want to resume the sequence, you need only the
     * {@link #index} this was on when you want to resume it, as well as possibly the {@link #baseX}, {@link #baseY},
     * {@link #baseZ}, {@link #baseW}, {@link #baseU}, and {@link #baseV} if those differ, and can call
     * {@link #resume(int, int, int, int, int, int, int)}.
     * All Vector6 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector6 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector6)} to fill an existing Vector6 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class Halton6 extends PointSequence<Vector6> {
        public int baseX, baseY, baseZ, baseW, baseU, baseV, index;

        /**
         * Uses base (2,3,5,7,11,13) and starts at index 0.
         */
        public Halton6() {
            this(2, 3, 5, 7, 11, 13, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, wBase, uBase, and vBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         * @param uBase base for u, must not share any common factors with other bases (use a prime)
         * @param vBase base for v, must not share any common factors with other bases (use a prime)
         */
        public Halton6(int xBase, int yBase, int zBase, int wBase, int uBase, int vBase) {
            this(xBase, yBase, zBase, wBase, uBase, vBase, 0);
        }

        /**
         * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
         * Using different (small) prime numbers for xBase, yBase, zBase, wBase, uBase, and vBase is a good idea.
         * @param xBase base for x, must not share any common factors with other bases (use a prime)
         * @param yBase base for y, must not share any common factors with other bases (use a prime)
         * @param zBase base for z, must not share any common factors with other bases (use a prime)
         * @param wBase base for w, must not share any common factors with other bases (use a prime)
         * @param uBase base for u, must not share any common factors with other bases (use a prime)
         * @param vBase base for v, must not share any common factors with other bases (use a prime)
         * @param index which (usually small) positive index to start at; often starts at 0
         */
        public Halton6(int xBase, int yBase, int zBase, int wBase, int uBase, int vBase, int index) {
            this.baseX = xBase;
            this.baseY = yBase;
            this.baseZ = zBase;
            this.baseW = wBase;
            this.baseU = uBase;
            this.baseV = vBase;
            this.index = index;
        }

        @Override
        public Vector6 next() {
            ++index;
            return new Vector6(vanDerCorput(baseX, index), vanDerCorput(baseY, index), vanDerCorput(baseZ, index),
                    vanDerCorput(baseW, index), vanDerCorput(baseU, index), vanDerCorput(baseV, index));
        }

        /**
         * Sets the x,y,z,w,u,v of {@code into} to the x,y,z,w,u,v of the next item in this Halton sequence, and
         * advances the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector6 nextInto(Vector6 into) {
            ++index;
            return into.set(vanDerCorput(baseX, index), vanDerCorput(baseY, index), vanDerCorput(baseZ, index),
                    vanDerCorput(baseW, index), vanDerCorput(baseU, index), vanDerCorput(baseV, index));
        }

        public Halton6 resume(int index){
            this.index = index;
            return this;
        }

        public Halton6 resume(int baseX, int baseY, int baseZ, int baseW, int baseU, int baseV, int index){
            this.baseX = baseX;
            this.baseY = baseY;
            this.baseZ = baseZ;
            this.baseW = baseW;
            this.baseU = baseU;
            this.baseV = baseV;
            this.index = index;
            return this;
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("h6");
            json.writeValue("x", baseX);
            json.writeValue("y", baseY);
            json.writeValue("z", baseZ);
            json.writeValue("w", baseW);
            json.writeValue("u", baseU);
            json.writeValue("v", baseV);
            json.writeValue("i", index);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("h6");
            baseX = jsonData.getInt("x");
            baseY = jsonData.getInt("y");
            baseZ = jsonData.getInt("z");
            baseW = jsonData.getInt("w");
            baseU = jsonData.getInt("u");
            baseV = jsonData.getInt("v");
            index = jsonData.getInt("i");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(baseX);
            out.writeInt(baseY);
            out.writeInt(baseZ);
            out.writeInt(baseW);
            out.writeInt(baseU);
            out.writeInt(baseV);
            out.writeInt(index);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            baseX = in.readInt();
            baseY = in.readInt();
            baseZ = in.readInt();
            baseW = in.readInt();
            baseU = in.readInt();
            baseV = in.readInt();
            index = in.readInt();
        }

        @Override
        public Halton6 copy() {
            return new Halton6(baseX, baseY, baseZ, baseW, baseU, baseV, index);
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
            this.y = random.nextFloat();
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

        @Override
        public R2 copy() {
            return new R2(x, y);
        }
    }

    /**
     * A very simple Iterator or Iterable over Vector3 items, this produces Vector3 points that won't overlap
     * or be especially close to each other for a long time by using the
     * <a href="https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">R3 Sequence</a>.
     * This uses a slight variant credited to
     * <a href="https://www.martysmods.com/a-better-r2-sequence/">Pascal Gilcher's article</a>.
     * If constructed with no arguments, this gets random
     * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
     * resume the sequence, you only need the last Vector3 produced, and can call {@link #resume(Vector3)} with it.
     * All Vector3 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector3 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector3)} to fill an existing Vector3 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class R3 extends PointSequence<Vector3> {
        public float x, y, z;

        /**
         * Gets random initial offsets from {@link MathUtils#random}.
         */
        public R3() {
            this(MathUtils.random);
        }

        /**
         * Gets random initial offsets from the given {@link Random} (or subclass).
         * @param random any Random or a subclass of Random, such as RandomXS128
         */
        public R3(Random random) {
            this.x = random.nextFloat();
            this.y = random.nextFloat();
            this.z = random.nextFloat();
        }

        /**
         * Uses the given initial offsets; this only uses their fractional parts.
         * @param x initial offset for x
         * @param y initial offset for y
         * @param z initial offset for z
         */
        public R3(float x, float y, float z) {
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
        }

        public R3(Vector3 offsets) {
            this(offsets.x, offsets.y, offsets.z);
        }

        @Override
        public Vector3 next() {
            // These specific "magic numbers" are what make this the R3 sequence, as found here:
            // https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // These specific numbers are 1f minus the original constants, an approach to minimize
            // floating-point error noted by: https://www.martysmods.com/a-better-r2-sequence/
            x += 0.18082748660383552f;
            y += 0.32895639329621074f;
            z += 0.45029952209802970f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            return new Vector3(x, y, z);
        }

        /**
         * Sets the x,y,z of {@code into} to the x,y,z of the next item in the R3 sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector3 nextInto(Vector3 into) {
            x += 0.18082748660383552f;
            y += 0.32895639329621074f;
            z += 0.45029952209802970f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            return into.set(x, y, z);
        }

        public R3 resume(float x, float y, float z){
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            return this;
        }

        public R3 resume(Vector3 previous) {
            return resume(previous.x, previous.y, previous.z);
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("r3");
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeValue("z", z);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("r3");
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
            z = jsonData.getFloat("z");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(z);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
            z = in.readFloat();
        }

        @Override
        public R3 copy() {
            return new R3(x, y, z);
        }
    }

    /**
     * A very simple Iterator or Iterable over Vector4 items, this produces Vector4 points that won't overlap
     * or be especially close to each other for a long time by using the
     * <a href="https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">R4 Sequence</a>.
     * This uses a slight variant credited to
     * <a href="https://www.martysmods.com/a-better-r2-sequence/">Pascal Gilcher's article</a>.
     * If constructed with no arguments, this gets random
     * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
     * resume the sequence, you only need the last Vector4 produced, and can call {@link #resume(Vector4)} with it.
     * All Vector4 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector4 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector4)} to fill an existing Vector4 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class R4 extends PointSequence<Vector4> {
        public float x, y, z, w;

        /**
         * Gets random initial offsets from {@link MathUtils#random}.
         */
        public R4() {
            this(MathUtils.random);
        }

        /**
         * Gets random initial offsets from the given {@link Random} (or subclass).
         * @param random any Random or a subclass of Random, such as RandomXS128
         */
        public R4(Random random) {
            this.x = random.nextFloat();
            this.y = random.nextFloat();
            this.z = random.nextFloat();
            this.w = random.nextFloat();
        }

        /**
         * Uses the given initial offsets; this only uses their fractional parts.
         * @param x initial offset for x
         * @param y initial offset for y
         * @param z initial offset for z
         * @param w initial offset for w
         */
        public R4(float x, float y, float z, float w) {
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
        }

        public R4(Vector4 offsets) {
            this(offsets.x, offsets.y, offsets.z, offsets.w);
        }

        @Override
        public Vector4 next() {
            // These specific "magic numbers" are what make this the R4 sequence, as found here:
            // https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // These specific numbers are 1f minus the original constants, an approach to minimize
            // floating-point error noted by: https://www.martysmods.com/a-better-r2-sequence/
            x += 0.14332511614549714f;
            y += 0.26610814337287403f;
            z += 0.37129327896219133f;
            w += 0.46140274277638993f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            return new Vector4(x, y, z, w);
        }

        /**
         * Sets the x,y,z,w of {@code into} to the x,y,z,w of the next item in the R4 sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector4 nextInto(Vector4 into) {
            x += 0.14332511614549714f;
            y += 0.26610814337287403f;
            z += 0.37129327896219133f;
            w += 0.46140274277638993f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            return into.set(x, y, z, w);
        }

        public R4 resume(float x, float y, float z, float w){
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
            return this;
        }

        public R4 resume(Vector4 previous) {
            return resume(previous.x, previous.y, previous.z, previous.w);
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("r4");
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeValue("z", z);
            json.writeValue("w", w);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("r4");
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
            z = jsonData.getFloat("z");
            w = jsonData.getFloat("w");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(z);
            out.writeFloat(w);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
            z = in.readFloat();
            w = in.readFloat();
        }

        @Override
        public R4 copy() {
            return new R4(x, y, z, w);
        }
    }

    /**
     * A very simple Iterator or Iterable over Vector5 items, this produces Vector5 points that won't overlap
     * or be especially close to each other for a long time by using the
     * <a href="https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">R5 Sequence</a>.
     * This uses a slight variant credited to
     * <a href="https://www.martysmods.com/a-better-r2-sequence/">Pascal Gilcher's article</a>.
     * If constructed with no arguments, this gets random
     * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
     * resume the sequence, you only need the last Vector5 produced, and can call {@link #resume(Vector5)} with it.
     * All Vector5 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector5 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector5)} to fill an existing Vector5 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class R5 extends PointSequence<Vector5> {
        public float x, y, z, w, u;

        /**
         * Gets random initial offsets from {@link MathUtils#random}.
         */
        public R5() {
            this(MathUtils.random);
        }

        /**
         * Gets random initial offsets from the given {@link Random} (or subclass).
         * @param random any Random or a subclass of Random, such as RandomXS128
         */
        public R5(Random random) {
            this.x = random.nextFloat();
            this.y = random.nextFloat();
            this.z = random.nextFloat();
            this.w = random.nextFloat();
            this.u = random.nextFloat();
        }

        /**
         * Uses the given initial offsets; this only uses their fractional parts.
         * @param x initial offset for x
         * @param y initial offset for y
         * @param z initial offset for z
         * @param w initial offset for w
         * @param u initial offset for u
         */
        public R5(float x, float y, float z, float w, float u) {
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
            this.u = u - MathUtils.floor(u);
        }

        public R5(Vector5 offsets) {
            this(offsets.x, offsets.y, offsets.z, offsets.w, offsets.u);
        }

        @Override
        public Vector5 next() {
            // These specific "magic numbers" are what make this the R5 sequence, as found here:
            // https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // These specific numbers are 1f minus the original constants, an approach to minimize
            // floating-point error noted by: https://www.martysmods.com/a-better-r2-sequence/
            x += 0.11872853836643038f;
            y += 0.22336061091023185f;
            z += 0.31556987041465745f;
            w += 0.39683125931427177f;
            u += 0.46844460228420870f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            u -= (int)u;
            return new Vector5(x, y, z, w, u);
        }

        /**
         * Sets the x,y,z,w,u of {@code into} to the x,y,z,w,u of the next item in the R5 sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector5 nextInto(Vector5 into) {
            x += 0.11872853836643038f;
            y += 0.22336061091023185f;
            z += 0.31556987041465745f;
            w += 0.39683125931427177f;
            u += 0.46844460228420870f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            u -= (int)u;
            return into.set(x, y, z, w, u);
        }

        public R5 resume(float x, float y, float z, float w, float u){
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
            this.u = u - MathUtils.floor(u);
            return this;
        }

        public R5 resume(Vector5 previous) {
            return resume(previous.x, previous.y, previous.z, previous.w, previous.u);
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("r5");
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeValue("z", z);
            json.writeValue("w", w);
            json.writeValue("u", u);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("r5");
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
            z = jsonData.getFloat("z");
            w = jsonData.getFloat("w");
            u = jsonData.getFloat("u");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(z);
            out.writeFloat(w);
            out.writeFloat(u);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
            z = in.readFloat();
            w = in.readFloat();
            u = in.readFloat();
        }

        @Override
        public R5 copy() {
            return new R5(x, y, z, w, u);
        }
    }

    /**
     * A very simple Iterator or Iterable over Vector6 items, this produces Vector6 points that won't overlap
     * or be especially close to each other for a long time by using the
     * <a href="https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/">R6 Sequence</a>.
     * This uses a slight variant credited to
     * <a href="https://www.martysmods.com/a-better-r2-sequence/">Pascal Gilcher's article</a>.
     * If constructed with no arguments, this gets random
     * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
     * resume the sequence, you only need the last Vector6 produced, and can call {@link #resume(Vector6)} with it.
     * All Vector6 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
     * to 1.0 (exclusive) range. This allocates a new Vector6 every time you call {@link #next()}. You can also
     * use {@link #nextInto(Vector6)} to fill an existing Vector6 with what would otherwise be allocated by
     * {@link #next()}.
     * <br>
     * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
     * understands the {@link Externalizable} interface.
     */
    public static class R6 extends PointSequence<Vector6> {
        public float x, y, z, w, u, v;

        /**
         * Gets random initial offsets from {@link MathUtils#random}.
         */
        public R6() {
            this(MathUtils.random);
        }

        /**
         * Gets random initial offsets from the given {@link Random} (or subclass).
         * @param random any Random or a subclass of Random, such as RandomXS128
         */
        public R6(Random random) {
            this.x = random.nextFloat();
            this.y = random.nextFloat();
            this.z = random.nextFloat();
            this.w = random.nextFloat();
            this.u = random.nextFloat();
            this.v = random.nextFloat();
        }

        /**
         * Uses the given initial offsets; this only uses their fractional parts.
         * @param x initial offset for x
         * @param y initial offset for y
         * @param z initial offset for z
         * @param w initial offset for w
         * @param u initial offset for u
         * @param v initial offset for v
         */
        public R6(float x, float y, float z, float w, float u, float v) {
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
            this.u = u - MathUtils.floor(u);
            this.v = v - MathUtils.floor(v);
        }

        public R6(Vector6 offsets) {
            this(offsets.x, offsets.y, offsets.z, offsets.w, offsets.u, offsets.v);
        }

        @Override
        public Vector6 next() {
            // These specific "magic numbers" are what make this the R6 sequence, as found here:
            // https://extremelearning.com.au/unreasonable-effectiveness-of-quasirandom-sequences/
            // These specific numbers are 1f minus the original constants, an approach to minimize
            // floating-point error noted by: https://www.martysmods.com/a-better-r2-sequence/
            x += 0.10134628737130069f;
            y += 0.19242150477865516f;
            z += 0.27426658703024020f;
            w += 0.34781697405602830f;
            u += 0.41391330242203050f;
            v += 0.47331101329926406f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            u -= (int)u;
            v -= (int)v;
            return new Vector6(x, y, z, w, u, v);
        }

        /**
         * Sets the x,y,z,w,u,v of {@code into} to the x,y,z,w,u,v of the next item in the R6 sequence, and advances
         * the sequence. Does not allocate. Modifies {@code into} in-place.
         * @param into will be overwritten with new values, modified in-place
         * @return {@code into}, after modifications
         */
        public Vector6 nextInto(Vector6 into) {
            x += 0.10134628737130069f;
            y += 0.19242150477865516f;
            z += 0.27426658703024020f;
            w += 0.34781697405602830f;
            u += 0.41391330242203050f;
            v += 0.47331101329926406f;
            x -= (int)x;
            y -= (int)y;
            z -= (int)z;
            w -= (int)w;
            u -= (int)u;
            v -= (int)v;
            return into.set(x, y, z, w, u, v);
        }

        public R6 resume(float x, float y, float z, float w, float u, float v){
            this.x = x - MathUtils.floor(x);
            this.y = y - MathUtils.floor(y);
            this.z = z - MathUtils.floor(z);
            this.w = w - MathUtils.floor(w);
            this.u = u - MathUtils.floor(u);
            this.v = v - MathUtils.floor(v);
            return this;
        }

        public R6 resume(Vector6 previous) {
            return resume(previous.x, previous.y, previous.z, previous.w, previous.u, previous.v);
        }

        @Override
        public void write(Json json) {
            json.writeObjectStart("r6");
            json.writeValue("x", x);
            json.writeValue("y", y);
            json.writeValue("z", z);
            json.writeValue("w", w);
            json.writeValue("u", u);
            json.writeValue("v", v);
            json.writeObjectEnd();
        }

        @Override
        public void read(Json json, JsonValue jsonData) {
            jsonData = jsonData.get("r6");
            x = jsonData.getFloat("x");
            y = jsonData.getFloat("y");
            z = jsonData.getFloat("z");
            w = jsonData.getFloat("w");
            u = jsonData.getFloat("u");
            v = jsonData.getFloat("v");
        }

        @GwtIncompatible
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeFloat(x);
            out.writeFloat(y);
            out.writeFloat(z);
            out.writeFloat(w);
            out.writeFloat(u);
            out.writeFloat(v);
        }

        @GwtIncompatible
        public void readExternal(ObjectInput in) throws IOException {
            x = in.readFloat();
            y = in.readFloat();
            z = in.readFloat();
            w = in.readFloat();
            u = in.readFloat();
            v = in.readFloat();
        }

        @Override
        public R6 copy() {
            return new R6(x, y, z, w, u, v);
        }
    }
}
