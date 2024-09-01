package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.Iterator;
import java.util.Random;

/**
 * A very simple Iterator or Iterable over Vector2 items, this produces Vector2 points that won't overlap
 * or be especially close to each other for a long time. If constructed with no arguments, this gets random
 * initial offsets from {@link MathUtils#random}. You can specify the offsets yourself, and if you want to
 * resume the sequence, you only need the last Vector2 produced, and can call {@link #resume(Vector2)} with it.
 * All Vector2 items this produces will be (and generally, those it is given should be) in the 0.0 (inclusive)
 * to 1.0 (exclusive) range.
 */
public class R2Sequence implements Iterator<Vector2>, Iterable<Vector2> {
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
        // These specific "magic numbers" are what make this the R2 sequence.
        x += 0.7548776662466927f;
        y += 0.5698402909980532f;
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
}
