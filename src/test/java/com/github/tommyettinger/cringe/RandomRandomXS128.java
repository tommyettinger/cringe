package com.github.tommyettinger.cringe;

import com.badlogic.gdx.math.RandomXS128;

public class RandomRandomXS128 extends GdxRandom {
    public RandomXS128 wrapped;

    public RandomRandomXS128() {
        wrapped = new RandomXS128(seedFromMath(), seedFromMath());
    }

    public RandomRandomXS128(long seed) {
        wrapped = new RandomXS128(seed);
    }

    public RandomRandomXS128(long state0, long state1) {
        wrapped = new RandomXS128(state0, state1);
    }

    /**
     * Returns the String {@code "RandomRandomXS128"}, which is the tag here.
     * @return the String {@code "RandomRandomXS128"}
     */
    @Override
    public String getTag() {
        return "RandomRandomXS128";
    }

    /**
     * Sets the seed of this random number generator using a single
     * {@code long} seed. This should behave exactly the same as if a new
     * object of this type was created with the constructor that takes a single
     * {@code long} value. This does not necessarily assign the state
     * variable(s) of the implementation with the exact contents of seed, so
     * {@link #getSelectedState(int)} should not be expected to return
     * {@code seed} after this, though it may. If this implementation has more
     * than one {@code long} of state, then the expectation is that none of
     * those state variables will be exactly equal to {@code seed} (almost all
     * the time).
     *
     * @param seed the initial seed
     */
    @Override
    public void setSeed(long seed) {
        if(wrapped != null)
            wrapped.setSeed(seed);
    }

    /**
     * Returns the next pseudorandom, uniformly distributed {@code long}
     * value from this random number generator's sequence. The general
     * contract of {@code nextLong} is that one {@code long} value is
     * pseudorandomly generated and returned.
     * <br>
     * The only methods that need to be implemented by this interface are
     * this and {@link #copy()}, though other methods can be implemented
     * as appropriate for generators that, for instance, natively produce
     * ints rather than longs.
     *
     * @return the next pseudorandom, uniformly distributed {@code long}
     * value from this random number generator's sequence
     */
    @Override
    public long nextLong() {
        return wrapped.nextLong();
    }

    /**
     * Creates a new GdxRandom with identical states to this one, so if the same GdxRandom methods are
     * called on this object and its copy (in the same order), the same outputs will be produced. This is not
     * guaranteed to copy the inherited state of any parent class, so if you call methods that are
     * only implemented by a superclass and not this one, the results may differ.
     * It is strongly suggested that subclasses return their own class and not GdxRandom.
     *
     * @return a deep copy of this GdxRandom.
     */
    @Override
    public RandomRandomXS128 copy() {
        return new RandomRandomXS128(wrapped.getState(0), wrapped.getState(1));
    }

    /**
     * Gets the number of possible state variables that can be selected with
     * {@link #getSelectedState(int)} or {@link #setSelectedState(int, long)}.
     * If this returns 0, that makes no state variable available for
     * reading or writing. An implementation that has only one {@code long}
     * state should return {@code 1}. A generator that permits setting two
     * different {@code long} values, or two {@code int} values, should return
     * {@code 2}. Much larger values are possible for types like the Mersenne
     * Twister or some CMWC generators.
     *
     * @return the non-negative number of selections possible for state variables
     */
    @Override
    public int getStateCount() {
        return 2;
    }

    /**
     * Gets a selected state value from this GdxRandom. The number of possible selections
     * is up to the implementing class, and is accessible via {@link #getStateCount()}, but
     * negative values for {@code selection} are typically not tolerated. This should return
     * the exact value of the selected state, assuming it is implemented. The default
     * implementation throws an UnsupportedOperationException, and implementors only have to
     * allow reading the state if they choose to implement this differently. If this method
     * is intended to be used, {@link #getStateCount()} must return an int greater than 0.
     *
     * @param selection used to select which state variable to get; generally non-negative
     * @return the exact value of the selected state
     */
    @Override
    public long getSelectedState(int selection) {
        return wrapped.getState(selection);
    }

    /**
     * Sets a selected state value to the given long {@code value}. The number of possible
     * selections is up to the implementing class, but negative values for {@code selection}
     * are typically not tolerated. Implementors are permitted to change {@code value} if it
     * is not valid, but they should not alter it if it is valid. The public implementation
     * calls {@link #setSeed(long)} with {@code value}, which doesn't need changing if the
     * generator has one state that is set verbatim by setSeed(). Otherwise, this method
     * should be implemented when {@link #getSelectedState(int)} is and the state is allowed
     * to be set by users. Having accurate ways to get and set the full state of a random
     * number generator makes it much easier to serialize and deserialize that class.
     *
     * @param selection used to select which state variable to set; generally non-negative
     * @param value     the exact value to use for the selected state, if valid
     */
    @Override
    public void setSelectedState(int selection, long value) {
        if(selection == 0)
            wrapped.setState(value, wrapped.getState(1));
        else
            wrapped.setState(wrapped.getState(0), value);
    }

    /**
     * Sets each state variable to the given {@code state}. If {@link #getStateCount()} is
     * 1, then this should set the whole state to the given value using
     * {@link #setSelectedState(int, long)}. If getStateCount() is more than 1, then all
     * states will be set in the same way (using setSelectedState(), all to {@code state}).
     *
     * @param state the long value to use for each state variable
     */
    @Override
    public void setState(long state) {
        wrapped.setState(0, state);
        wrapped.setState(1, state);
    }

    /**
     * Sets each state variable to either {@code stateA} or {@code stateB}, alternating.
     * This uses {@link #setSelectedState(int, long)} to set the values. If there is one
     * state variable ({@link #getStateCount()} is 1), then this only sets that state
     * variable to stateA. If there are two state variables, the first is set to stateA,
     * and the second to stateB. If there are more, it reuses stateA, then stateB, then
     * stateA, and so on until all variables are set.
     *
     * @param stateA the long value to use for states at index 0, 2, 4, 6...
     * @param stateB the long value to use for states at index 1, 3, 5, 7...
     */
    @Override
    public void setState(long stateA, long stateB) {
        wrapped.setState(stateA, stateB);
    }

    /**
     * Generates random bytes and places them into a user-supplied
     * byte array.  The number of random bytes produced is equal to
     * the length of the byte array.
     *
     * @param bytes the byte array to fill with random bytes
     * @throws NullPointerException if the byte array is null
     */
    @Override
    public void nextBytes(byte[] bytes) {
        wrapped.nextBytes(bytes);
    }

    /**
     * Returns the next pseudorandom, uniformly distributed {@code int}
     * value from this random number generator's sequence. The general
     * contract of {@code nextInt} is that one {@code int} value is
     * pseudorandomly generated and returned. All 2<sup>32</sup> possible
     * {@code int} values are produced with (approximately) equal probability.
     *
     * @return the next pseudorandom, uniformly distributed {@code int}
     * value from this random number generator's sequence
     */
    @Override
    public int nextInt() {
        return wrapped.nextInt();
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code int} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.  The general contract of
     * {@code nextInt} is that one {@code int} value in the specified range
     * is pseudorandomly generated and returned.  All {@code bound} possible
     * {@code int} values are produced with (approximately) equal
     * probability.
     * <br>
     * It should be mentioned that the technique this uses has some bias, depending
     * on {@code bound}, but it typically isn't measurable without specifically looking
     * for it. Using the method this does allows this method to always advance the state
     * by one step, instead of a varying and unpredictable amount with the more typical
     * ways of rejection-sampling random numbers and only using numbers that can produce
     * an int within the bound without bias.
     * See <a href="https://www.pcg-random.org/posts/bounded-rands.html">M.E. O'Neill's
     * blog about random numbers</a> for discussion of alternative, unbiased methods.
     *
     * @param bound the upper bound (exclusive). If negative or 0, this always returns 0.
     * @return the next pseudorandom, uniformly distributed {@code int}
     * value between zero (inclusive) and {@code bound} (exclusive)
     * from this random number generator's sequence
     */
    @Override
    public int nextInt(int bound) {
        return wrapped.nextInt(bound);
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code long} value
     * between 0 (inclusive) and the specified value (exclusive), drawn from
     * this random number generator's sequence.  The general contract of
     * {@code nextLong} is that one {@code long} value in the specified range
     * is pseudorandomly generated and returned.  All {@code bound} possible
     * {@code long} values are produced with (approximately) equal
     * probability, though there is a small amount of bias depending on the bound.
     *
     * <br> Note that this advances the state by the same amount as a single call to
     * {@link #nextLong()}, which allows methods like {@link #skip(long)} to function
     * correctly, but introduces some bias when {@code bound} is very large. This will
     * also advance the state if {@code bound} is 0 or negative, so usage with a variable
     * bound will advance the state reliably.
     *
     * <br> This method has some bias, particularly on larger bounds. Actually measuring
     * bias with bounds in the trillions or greater is challenging but not impossible, so
     * don't use this for a real-money gambling purpose. The bias isn't especially
     * significant, though.
     *
     * @param bound the upper bound (exclusive). If negative or 0, this always returns 0.
     * @return the next pseudorandom, uniformly distributed {@code long}
     * value between zero (inclusive) and {@code bound} (exclusive)
     * from this random number generator's sequence
     * @see #nextInt(int) Here's a note about the bias present in the bounded generation.
     */
    @Override
    public long nextLong(long bound) {
        return wrapped.nextLong(bound);
    }

    /**
     * Returns the next pseudorandom, uniformly distributed
     * {@code boolean} value from this random number generator's
     * sequence. The general contract of {@code nextBoolean} is that one
     * {@code boolean} value is pseudorandomly generated and returned.  The
     * values {@code true} and {@code false} are produced with
     * (approximately) equal probability.
     * <br>
     * The public implementation simply returns a sign check on {@link #nextLong()},
     * returning true if the generated long is negative. This is typically the safest
     * way to implement this method; many types of generators have less statistical
     * quality on their lowest bit, so just returning based on the lowest bit isn't
     * always a good idea.
     *
     * @return the next pseudorandom, uniformly distributed
     * {@code boolean} value from this random number generator's
     * sequence
     */
    @Override
    public boolean nextBoolean() {
        return wrapped.nextBoolean();
    }

    /**
     * Returns the next pseudorandom, uniformly distributed {@code float}
     * value between {@code 0.0} (inclusive) and {@code 1.0} (exclusive)
     * from this random number generator's sequence.
     *
     * <p>The general contract of {@code nextFloat} is that one
     * {@code float} value, chosen (approximately) uniformly from the
     * range {@code 0.0f} (inclusive) to {@code 1.0f} (exclusive), is
     * pseudorandomly generated and returned. All 2<sup>24</sup> possible
     * {@code float} values of the form <i>m&nbsp;x&nbsp;</i>2<sup>-24</sup>,
     * where <i>m</i> is a positive integer less than 2<sup>24</sup>, are
     * produced with (approximately) equal probability.
     *
     * <p>The public implementation uses the upper 24 bits of {@link #nextLong()},
     * with an unsigned right shift and a multiply by a very small float
     * ({@code 5.9604645E-8f} or {@code 0x1p-24f}). It tends to be fast if
     * nextLong() is fast, but alternative implementations could use 24 bits of
     * {@link #nextInt()} (or just {@link #next(int)}, giving it {@code 24})
     * if that generator doesn't efficiently generate 64-bit longs.<p>
     *
     * @return the next pseudorandom, uniformly distributed {@code float}
     * value between {@code 0.0} and {@code 1.0} from this
     * random number generator's sequence
     */
    @Override
    public float nextFloat() {
        return wrapped.nextFloat();
    }

    /**
     * Returns the next pseudorandom, uniformly distributed
     * {@code double} value between {@code 0.0} (inclusive) and {@code 1.0}
     * (exclusive) from this random number generator's sequence.
     *
     * <p>The general contract of {@code nextDouble} is that one
     * {@code double} value, chosen (approximately) uniformly from the
     * range {@code 0.0d} (inclusive) to {@code 1.0d} (exclusive), is
     * pseudorandomly generated and returned.
     *
     * <p>The public implementation uses the upper 53 bits of {@link #nextLong()},
     * with an unsigned right shift and a multiply by a very small double
     * ({@code 1.1102230246251565E-16}, or {@code 0x1p-53}). It should perform well
     * if nextLong() performs well, and is expected to perform less well if the
     * generator naturally produces 32 or fewer bits at a time.<p>
     *
     * @return the next pseudorandom, uniformly distributed {@code double}
     * value between {@code 0.0} and {@code 1.0} from this
     * random number generator's sequence
     */
    @Override
    public double nextDouble() {
        return wrapped.nextDouble();
    }
}
