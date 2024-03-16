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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A random number generator with five 64-bit states; does not use multiplication, only add, subtract, XOR, and rotate
 * operations. Has a state that runs like a counter, guaranteeing a minimum period of 2 to the 64. This passes roughly
 * 180 petabytes of intensive testing on the GPU with ReMort, as well as 64TB of PractRand's broad spectrum of tests.
 * It is very fast on modern JDKs (such as HotSpot or Graal, compatible with Java 16 or later), able to generate 1.75
 * billion longs per second with {@link #nextLong()} on a mid-grade laptop. To compare,
 * {@link java.util.Random#nextLong()} is only able to generate 64 million longs per second on the same machine.
 * <br>
 * The maximum and/or expected periods for RandomAce320 are far larger than they would need to be, even if run for
 * decades on current hardware. The minimum period alone would take multiple years to exhaust if using a CPU, let alone
 * to find that particular cycle with the shortest period. Running on a fast GPU would take less time, but still an
 * impractically long time.
 * <br>
 * This class is a {@link GdxRandom} and is also a JDK {@link java.util.Random} as a result.
 * This implements all optional methods in GdxRandom except {@link #skip(long)}; it does implement
 * {@link #previousLong()} without using skip().
 * <br>
 * The name comes from the 52 cards (excluding jokers, but including aces) in a standard playing card deck, since this
 * uses a left rotation by exactly 52 as one of its critical components. Rotations by anything else I tried didn't pass
 * testing as well, or even at all.
 * <br>
 * The algorithm here is (to my knowledge) novel; it was first published as AceRandom by Tommy Ettinger in
 * <a href="https://github.com/tommyettinger/juniper">the juniper library</a>.
 * <br>
 * To use this class in your code, you only need to copy RandomAce320.java and GdxRandom.java from this folder into any
 * package in your codebase. They must be in the same package, but there are no other restrictions. You do not need to
 * copy any other subclasses of GdxRandom if you are satisfied with this one.
 */
public class RandomAce320 extends GdxRandom {

	/**
	 * Returns the String {@code "RandomAce320"}, which is the tag here.
	 * @return the String {@code "RandomAce320"}
	 */
	@Override
	public String getTag() {
		return "RandomAce320";
	}

	/**
	 * The first state; can be any long.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateA;
	/**
	 * The second state; can be any long.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateB;
	/**
	 * The third state; can be any long.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateC;
	/**
	 * The fourth state; can be any long.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateD;
	/**
	 * The fifth state; can be any long. The first call to {@link #nextLong()} will return this verbatim, if no other
	 * methods have been called.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateE;

	/**
	 * Creates a new RandomAce320 with a random state.
	 */
	public RandomAce320() {
		stateA = seedFromMath();
		stateB = seedFromMath();
		stateC = seedFromMath();
		stateD = seedFromMath();
		stateE = seedFromMath();
	}

	/**
	 * Creates a new RandomAce320 with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public RandomAce320(long seed) {
		setSeed(seed);
	}

	/**
	 * Creates a new RandomAce320 with the given five states; all {@code long} values are permitted.
	 * These states will be used verbatim.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any {@code long} value
	 * @param stateC any {@code long} value
	 * @param stateD any {@code long} value
	 * @param stateE any {@code long} value
	 */
	public RandomAce320(long stateA, long stateB, long stateC, long stateD, long stateE) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
		this.stateE = stateE;
	}

	/**
	 * This generator has 5 {@code long} states, so this returns 5.
	 *
	 * @return 5 (five)
	 */
	@Override
	public int getStateCount () {
		return 5;
	}

	/**
	 * Gets the state determined by {@code selection}, as-is. The value for selection should be
	 * between 0 and 4, inclusive; if it is any other value this gets state E as if 4 was given.
	 *
	 * @param selection used to select which state variable to get; generally 0, 1, 2, 3, or 4
	 * @return the value of the selected state
	 */
	@Override
	public long getSelectedState (int selection) {
		switch (selection) {
			case 0:
				return stateA;
			case 1:
				return stateB;
			case 2:
				return stateC;
			case 3:
				return stateD;
			default:
				return stateE;
		}
	}

	/**
	 * Sets one of the states, determined by {@code selection}, to {@code value}, as-is.
	 * Selections 0, 1, 2, 3, and 4 refer to states A, B, C, D, and E, and if the selection is anything
	 * else, this treats it as 4 and sets stateE.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, 3, or 4
	 * @param value     the exact value to use for the selected state, if valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		switch (selection) {
			case 0:
				stateA = value;
				break;
			case 1:
				stateB = value;
				break;
			case 2:
				stateC = value;
				break;
			case 3:
				stateD = value;
				break;
			default:
				stateE = value;
		}
	}

	/**
	 * This initializes all 5 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here, all with a different
	 * first value returned by {@link #nextLong()}.
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		seed = (seed ^ 0x1C69B3F74AC4AE35L) * 0x3C79AC492BA7B653L; // an XLCG
		stateA = seed ^ ~0xC6BC279692B5C323L;
		seed ^= seed >>> 32;
		stateB = seed ^ 0xD3833E804F4C574BL;
		seed *= 0xBEA225F9EB34556DL;                               // MX3 unary hash
		seed ^= seed >>> 29;
		stateC = seed ^ ~0xD3833E804F4C574BL;                      // updates are spread across the MX3 hash
		seed *= 0xBEA225F9EB34556DL;
		seed ^= seed >>> 32;
		stateD = seed ^ 0xC6BC279692B5C323L;;
		seed *= 0xBEA225F9EB34556DL;
		seed ^= seed >>> 29;
		stateE = seed;
	}

	public long getStateA () {
		return stateA;
	}

	/**
	 * Sets the first part of the state.
	 *
	 * @param stateA can be any long
	 */
	public void setStateA (long stateA) {
		this.stateA = stateA;
	}

	public long getStateB () {
		return stateB;
	}

	/**
	 * Sets the second part of the state.
	 *
	 * @param stateB can be any long
	 */
	public void setStateB (long stateB) {
		this.stateB = stateB;
	}

	public long getStateC () {
		return stateC;
	}

	/**
	 * Sets the third part of the state.
	 *
	 * @param stateC can be any long
	 */
	public void setStateC (long stateC) {
		this.stateC = stateC;
	}

	public long getStateD () {
		return stateD;
	}

	/**
	 * Sets the fourth part of the state.
	 *
	 * @param stateD can be any long
	 */
	public void setStateD (long stateD) {
		this.stateD = stateD;
	}

	public long getStateE () {
		return stateE;
	}

	/**
	 * Sets the fifth part of the state.
	 *
	 * @param stateE can be any long
	 */
	public void setStateE (long stateE) {
		this.stateE = stateE;
	}

	/**
	 * Sets the state completely to the given five state variables.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * {@link #setStateC(long)}, {@link #setStateD(long)}, and {@link #setStateE(long)} as a group.
	 *
	 * @param stateA the first state; can be any long
	 * @param stateB the second state; can be any long
	 * @param stateC the third state; can be any long
	 * @param stateD the fourth state; can be any long
	 * @param stateE the fifth state; can be any long
	 */
	@Override
	public void setState (long stateA, long stateB, long stateC, long stateD, long stateE) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
		this.stateE = stateE;
	}

	@Override
	public long nextLong () {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		final long fe = stateE;
		stateA = fa + 0x9E3779B97F4A7C15L;
		stateB = fa ^ fe;
		stateC = fb + fd;
		stateD = (fc << 52 | fc >>> 12);
		return stateE = fb - fc;
	}

	@Override
	public long previousLong () {
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		final long fe = stateE;
		stateA -= 0x9E3779B97F4A7C15L;
		stateC = (fd >>> 52 | fd << 12);
		stateB = stateC + fe;
		stateD = fc - stateB;
		stateE = fb ^ stateA;
		return fe;
	}

	@Override
	public int next (int bits) {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		final long fe = stateE;
		stateA = fa + 0x9E3779B97F4A7C15L;
		stateB = fa ^ fe;
		stateC = fb + fd;
		stateD = (fc << 52 | fc >>> 12);
		return (int) (stateE = fb - fc) >>> (32 - bits);
	}

	/**
	 * Jumps extremely far in the generator's sequence, such that one call to leap() advances the state as many as
	 * {@code Math.pow(2, 48)} calls to {@link #nextLong()}. This can be used to create 65536 substreams of this
	 * generator's sequence, each with a period of at least {@code Math.pow(2, 48)} but likely much more.
	 * @return the result of what nextLong() would return if it was called at the state this jumped to
	 */
	public long leap () {
		final long fa = stateA;
		final long fb = stateB;
		final long fc = stateC;
		final long fd = stateD;
		final long fe = stateE;
		stateA = fa + 0x7C15000000000000L;
		stateB = fa ^ fe;
		stateC = fb + fd;
		stateD = (fc << 52 | fc >>> 12);
		return stateE = fb - fc;
	}

	@Override
	public RandomAce320 copy () {
		return new RandomAce320(stateA, stateB, stateC, stateD, stateE);
	}

	/**
	 * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this RandomAce320
	 * object to match the state in the serialized data. Returns this RandomAce320, after possibly changing its state.
	 *
	 * @param data a String probably produced by {@link #stringSerialize()}
	 * @return this, after setting its state
	 */
	@Override
	public RandomAce320 stringDeserialize(String data) {
		super.stringDeserialize(data);
		return this;
	}

	@GwtIncompatible
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(stateA);
		out.writeLong(stateB);
		out.writeLong(stateC);
		out.writeLong(stateD);
		out.writeLong(stateE);
	}

	@GwtIncompatible
	public void readExternal(ObjectInput in) throws IOException {
		stateA = in.readLong();
		stateB = in.readLong();
		stateC = in.readLong();
		stateD = in.readLong();
		stateE = in.readLong();
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RandomAce320 that = (RandomAce320)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC && stateD == that.stateD &&
				stateE == that.stateE;
	}

	public String toString () {
		return "RandomAce320{" + "stateA=" + (stateA) + "L, stateB=" + (stateB) + "L, stateC=" + (stateC) + "L, stateD=" + (stateD) + "L, stateE=" + (stateE) + "L}";
	}
}
