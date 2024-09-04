package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

/**
 * A very simple Iterator or Iterable over Vector2 items, this produces Vector2 points that won't overlap
 * or be especially close to each other for a long time. If constructed with no arguments, this gets random
 * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
 * resume the sequence, you need only the {@link #index} this was on when you want to resume it, and possibly
 * also the {@link #baseX} and {@link #baseY} if those differ, and can call {@link #resume(int, int, int)}.
 * All Vector2 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
 * to 1.0 (exclusive) range. This allocates a new Vector2 every time you call {@link #next()}. You can also
 * use {@link #nextInto(Vector2)} to fill an existing Vector2 with what would otherwise be allocated by
 * {@link #next()}.
 * <br>
 * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
 * understands the {@link Externalizable} interface.
 */
public class HaltonSequence implements Iterator<Vector2>, Iterable<Vector2>, Json.Serializable, Externalizable {
    public int baseX, baseY, index;

    /**
     * Uses base (2,3) and starts at index 0.
     */
    public HaltonSequence() {
        this(2, 3, 0);
    }

    /**
     * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
     * Using different (small) prime numbers for xBase and yBase is a good idea.
     * @param xBase Base for x, must not share any common factors with other bases (use a prime)
     * @param yBase Base for y, must not share any common factors with other bases (use a prime)
     */
    public HaltonSequence(int xBase, int yBase) {
        this(xBase, yBase, 0);
    }

    /**
     * Uses the given bases, which should be "relatively prime" (meaning they shouldn't share any common factors).
     * Using different (small) prime numbers for xBase and yBase is a good idea.
     * @param xBase Base for x, must not share any common factors with other bases (use a prime)
     * @param yBase Base for y, must not share any common factors with other bases (use a prime)
     * @param index which (usually small) positive index to start at; often starts at 0
     */
    public HaltonSequence(int xBase, int yBase, int index) {
        this.baseX = xBase;
        this.baseY = yBase;
        this.index = index;
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

    @Override
    public boolean hasNext() {
        return true;
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

    public HaltonSequence resume(int index){
        this.index = index;
        return this;
    }

    public HaltonSequence resume(int baseX, int baseY, int index){
        this.baseX = baseX;
        this.baseY = baseY;
        this.index = index;
        return this;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove from an infinite sequence.");
    }

    @Override
    public Iterator<Vector2> iterator() {
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
