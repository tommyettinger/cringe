/*
 * Copyright (c) 2023 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.tommyettinger.cringe;

/**
 * A variant on Java 8's SplittableRandom algorithm, removing the splittable quality so this has one possible stream.
 * You'd typically use this when you want every output of {@link #nextLong()} from one generator to be a different,
 * unique number until every {@code long} has been generated, such as for generating unique seeds or IDs. The reasons
 * for removing the splittable quality are a little complicated, but it's enough to say that by having a fixed stream,
 * this is a little faster, and it avoids the possibility of some streams being lower-quality. This uses Pelle Evensen's
 * <a href="https://mostlymangling.blogspot.com/2019/12/stronger-better-morer-moremur-better.html">Moremur mixer</a>
 * instead of SplittableRandom's Variant 13, which should give it roughly equivalent performance but somewhat higher
 * statistical quality. Like many variations on SplittableRandom and its SplitMix64 algorithm, this changes its state
 * by a simple counter with a large increment; one of the best increments seems to be (2 to the 64) divided by the
 * golden ratio, plus or minus 1 to make it odd. This number, 0x9E3779B97F4A7C15L or -7046029254386353131L when stored
 * in a signed long, shows up a lot in random number generation and hashing fields because the golden ratio has some
 * unique and helpful properties. The increment is sometimes called the "gamma," and this particular gamma is known to
 * be high-quality, but of the over 9 quintillion possible odd-number gammas, not all are have such nice properties
 * (for instance, {@code 1} would make a terrible gamma if it were used in this generator, because it's so small). We
 * only allow one gamma here, so we can be sure it works.
 * <br>
 * Other useful traits of this generator are that it has exactly one {@code long} of state, that all values are
 * permitted for that state, and that you can {@link #skip(long)} the state forwards or backwards in constant time.
 * It is also quite fast, though not as fast as {@link RandomAce320} on Java 16 or newer.
 * <br>
 * Unlike the multiple-state generators here, RandomDistinct64 tolerates being given sequential seeds and/or states, and
 * in fact doesn't randomize the seed when given one with {@link #setSeed(long)}.
 * <br>
 * This class is a {@link GdxRandom} and is also a JDK {@link java.util.Random} as a result.
 * This implements all methods from {@link GdxRandom}, including the optional {@link #skip(long)} and
 * {@link #previousLong()} methods.
 * <br>
 * The SplitMix64 algorithm is derived from <a href="https://gee.cs.oswego.edu/dl/papers/oopsla14.pdf">this paper</a> by
 * Guy L. Steele Jr., Doug Lea, and Christine H. Flood. Moremur was written by Pelle Evensen, and improves upon the
 * MurmurHash3 mixer written by Austin Appleby.
 * <br>
 * To use this class in your code, you only need to copy RandomDistinct64.java and GdxRandom.java from this folder into
 * any package in your codebase. They must be in the same package, but there are no other restrictions. You do not need
 * to copy any other subclasses of GdxRandom if you are satisfied with this one.
 */
public class RandomDistinct64 extends GdxRandom {
	/**
	 * Returns the String {@code "DisR"}, which is the tag here.
	 * @return the String {@code "DisR"}
	 */
	@Override
	public String getTag() {
		return "DisR";
	}

	/**
	 * The only state variable; can be any {@code long}.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long state;

	/**
	 * Creates a new RandomDistinct64 with a random state.
	 */
	public RandomDistinct64() {
		this(seedFromMath());
	}

	/**
	 * Creates a new RandomDistinct64 with the given state; all {@code long} values are permitted.
	 *
	 * @param state any {@code long} value
	 */
	public RandomDistinct64(long state) {
		super(state);
		this.state = state;
	}

	/**
	 * This has one long state.
	 *
	 * @return 1 (one)
	 */
	@Override
	public int getStateCount () {
		return 1;
	}

	/**
	 * Gets the only state, which can be any long value.
	 *
	 * @param selection ignored; this always returns the same, only state
	 * @return the only state's exact value
	 */
	@Override
	public long getSelectedState (int selection) {
		return state;
	}

	/**
	 * Sets the only state, which can be given any long value. The selection
	 * can be anything and is ignored.
	 *
	 * @param selection ignored; this always sets the same, only state
	 * @param value     the exact value to use for the state; all longs are valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		state = value;
	}

	/**
	 * Sets the only state, which can be given any long value; this seed value
	 * will not be altered. Equivalent to {@link #setSelectedState(int, long)}
	 * with any selection and {@code seed} passed as the {@code value}.
	 *
	 * @param seed the exact value to use for the state; all longs are valid
	 */
	@Override
	public void setSeed (long seed) {
		state = seed;
	}

	/**
	 * Gets the current state; it's already public, but I guess this could still
	 * be useful. The state can be any {@code long}.
	 *
	 * @return the current state, as a long
	 */
	public long getState () {
		return state;
	}

	/**
	 * Sets each state variable to the given {@code state}. This implementation
	 * simply sets the one state variable to {@code state}.
	 *
	 * @param state the long value to use for the state variable
	 */
	@Override
	public void setState (long state) {
		this.state = state;
	}

	@Override
	public long nextLong () {
		long x = (state += 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		return x ^ x >>> 27;
	}

	/**
	 * Skips the state forward or backwards by the given {@code advance}, then returns the result of {@link #nextLong()}
	 * at the same point in the sequence. If advance is 1, this is equivalent to nextLong(). If advance is 0, this
	 * returns the same {@code long} as the previous call to the generator (if it called nextLong()), and doesn't change
	 * the state. If advance is -1, this moves the state backwards and produces the {@code long} before the last one
	 * generated by nextLong(). More positive numbers move the state further ahead, and more negative numbers move the
	 * state further behind; all of these take constant time.
	 *
	 * @param advance how many steps to advance the state before generating a {@code long}
	 * @return a random {@code long} by the same algorithm as {@link #nextLong()}, using the appropriately-advanced state
	 */
	@Override
	public long skip (long advance) {
		long x = (state += 0x9E3779B97F4A7C15L * advance);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		return x ^ x >>> 27;
	}

	@Override
	public long previousLong () {
		long x = state;
		state -= 0x9E3779B97F4A7C15L;
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		return x ^ x >>> 27;
	}

	@Override
	public int next (int bits) {
		long x = (state += 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		return (int)(x ^ x >>> 27) >>> (32 - bits);
	}

	@Override
	public RandomDistinct64 copy () {
		return new RandomDistinct64(state);
	}

	/**
	 * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this RandomDistinct64
	 * object to match the state in the serialized data. Returns this RandomDistinct64, after possibly changing its state.
	 *
	 * @param data a String probably produced by {@link #stringSerialize()}
	 * @return this, after setting its state
	 */
	@Override
	public RandomDistinct64 stringDeserialize(String data) {
		super.stringDeserialize(data);
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RandomDistinct64 that = (RandomDistinct64)o;

		return state == that.state;
	}

	@Override
	public String toString () {
		return "RandomDistinct64{state=" + (state) + "L}";
	}
}
