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
import java.util.Random;

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
 * to 1.0 (exclusive) range. This allocates a new Vector2 every time you call {@link #next()}.
 * <br>
 * This can be serialized out-of-the-box with libGDX Json or Apache Fury, as well as anything else that
 * understands the {@link Externalizable} interface.
 */
public class R2Sequence implements Iterator<Vector2>, Iterable<Vector2>, Json.Serializable, Externalizable {
    public float x, y;

    /**
     * Gets random initial offsets from {@link MathUtils#random}.
     */
    public R2Sequence() {
        this(MathUtils.random);
    }

    /**
     * Gets random initial offsets from the given {@link Random} (or subclass).
     * @param random any Random or a subclass of Random, such as RandomXS128
     */
    public R2Sequence(Random random) {
        this.x = random.nextFloat();
        this.y  = random.nextFloat();
    }

    /**
     * Uses the given initial offsets; this only uses their fractional parts.
     * @param x initial offset for x
     * @param y initial offset for y
     */
    public R2Sequence(float x, float y) {
        this.x = x - MathUtils.floor(x);
        this.y = y - MathUtils.floor(y);
    }

    public R2Sequence(Vector2 offsets) {
        this(offsets.x, offsets.y);
    }

    @Override
    public boolean hasNext() {
        return true;
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

    public R2Sequence resume(float x, float y){
        this.x = x - MathUtils.floor(x);
        this.y = y - MathUtils.floor(y);
        return this;
    }

    public R2Sequence resume(Vector2 previous) {
        return resume(previous.x, previous.y);
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
