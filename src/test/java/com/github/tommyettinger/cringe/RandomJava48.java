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
 * This should be similar to {@link java.util.Random}, and uses the same algorithm, but is a GdxRandom.
 */
public class RandomJava48 extends GdxRandom {
	/**
	 * Returns the String {@code "RandomJava48"}, which is the tag here.
	 * @return the String {@code "RandomJava48"}
	 */
	@Override
	public String getTag() {
		return "RandomJava48";
	}

	/**
	 * The only state variable; can be any {@code long}.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long state;

	/**
	 * Creates a new RandomJava48 with a random state.
	 */
	public RandomJava48() {
		this(seedFromMath());
	}

	/**
	 * Creates a new RandomJava48 with the given state; all {@code long} values are permitted, but only the low 48 bits
	 * are actually used.
	 *
	 * @param state any {@code long} value, though only the low 48 bits will be used
	 */
	public RandomJava48(long state) {
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
	 * will be altered in a simple way. This is not equivalent to {@link #setSelectedState(int, long)}.
	 *
	 * @param seed the value to use for the state; all longs are valid, and this will be altered
	 */
	@Override
	public void setSeed (long seed) {
		state = (seed ^ 0x5DEECE66DL) & 0xffffffffffffL;
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
		long hi = (state = (state * 0x5DEECE66DL + 0xBL) & 0xffffffffffffL) << 16 & 0xFFFFFFFF00000000L;
		long lo = (state = (state * 0x5DEECE66DL + 0xBL) & 0xffffffffffffL) >>> 16;
		return hi + lo;
	}

	@Override
	public int next (int bits) {
		return (int)((state = (state * 0x5DEECE66DL + 0xBL) & 0xffffffffffffL) >>> (48 - bits));
	}

	@Override
	public int nextInt() {
		return (int)((state = (state * 0x5DEECE66DL + 0xBL)& 0xffffffffffffL) >>> 16);
	}

	@Override
	public RandomJava48 copy () {
		return new RandomJava48(state);
	}

	/**
	 * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this RandomJava48
	 * object to match the state in the serialized data. Returns this RandomJava48, after possibly changing its state.
	 *
	 * @param data a String probably produced by {@link #stringSerialize()}
	 * @return this, after setting its state
	 */
	@Override
	public RandomJava48 stringDeserialize(String data) {
		super.stringDeserialize(data);
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RandomJava48 that = (RandomJava48)o;

		return state == that.state;
	}

	@Override
	public String toString () {
		return "RandomJava48{state=" + (state) + "L}";
	}
}
