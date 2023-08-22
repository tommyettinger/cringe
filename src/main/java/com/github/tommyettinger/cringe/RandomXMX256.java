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
 * A random number generator that guarantees 4-dimensional equidistribution (except for the quartet with four
 * zeroes in a row, every quartet of long results is produced exactly once over the period). This particular generator
 * is nearly identical to Xoshiro256** (or StarStar), but instead of using the fast but weak StarStar "scrambler,"
 * it runs output through the MX3 unary hash, which is slower but extremely strong. It has a period of
 * (2 to the 256) - 1, which would take millennia to exhaust on current-generation hardware (at least).
 * This isn't a cryptographic generator, but the only issue I know of with Xoshiro and the StarStar scrambler should be
 * fully resolved here. The only invalid state is the one with 0 in each state variable, and this won't ever
 * occur in the normal period of that contains all other states. You can seed this with either {@link #setSeed(long)}
 * or {@link #setState(long, long, long, long)} without encountering problems past the first 4 or so outputs. If you
 * pass very similar initial states to two different generators with {@link #setState(long)}, their output will likely
 * be similar for the first 3 or 4 outputs, and will then diverge rapidly.
 * <br>
 * This generator is substantially slower than {@link RandomAce320}, but has an exactly-known, extremely-long period and
 * is 4-dimensionally equidistributed. It also randomizes even tiny changes in its state using a hash, so very similar
 * states are not likely to produce similar output. Where RandomAce320 generates 1.75 billion longs per second, this
 * generates 554 million longs per second. For many applications, random number generation is far from a bottleneck, so
 * in both cases the generators should be more than fast enough.
 * <br>
 * This class is a {@link GdxRandom} and is also a JDK {@link java.util.Random} as a result.
 * This implements all optional methods in GdxRandom except {@link #skip(long)}; it does implement
 * {@link #previousLong()} without using skip().
 * <br>
 * Xoshiro256** was written in 2018 by David Blackman and Sebastiano Vigna. You can consult their paper for technical details:
 * <a href="https://vigna.di.unimi.it/ftp/papers/ScrambledLinear.pdf">PDF link here</a>. The MX3 unary hash was written
 * 2020 by Jon Maiga, <a href="https://github.com/jonmaiga/mx3">GitHub repo here</a>.
 * <br>
 * To use this class in your code, you only need to copy RandomXMX256.java and GdxRandom.java from this folder into
 * any package in your codebase. They must be in the same package, but there are no other restrictions. You do not need
 * to copy any other subclasses of GdxRandom if you are satisfied with this one.
 */
public class RandomXMX256 extends GdxRandom {
	/**
	 * Returns the String {@code "XMXR"}, which is the tag here.
	 * @return the String {@code "XMXR"}
	 */
	@Override
	public String getTag() {
		return "XMXR";
	}

	/**
	 * The first state; can be any long, as long as all states are not 0.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateA;
	/**
	 * The second state; can be any long, as long as all states are not 0.
	 * This is the state that is scrambled and returned; if it is 0 before a number
	 * is generated, then the next number returned by {@link #nextLong()} will be 0.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateB;
	/**
	 * The third state; can be any long, as long as all states are not 0.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateC;
	/**
	 * The fourth state; can be any long, as long as all states are not 0.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public long stateD;

	/**
	 * Creates a new RandomXMX256 with a random state.
	 */
	public RandomXMX256() {
		super();
		stateA = seedFromMath();
		stateB = seedFromMath();
		stateC = seedFromMath();
		stateD = seedFromMath();
		if ((stateA | stateB | stateC | stateD) == 0L)
			stateD = 0x9E3779B97F4A7C15L;
	}

	/**
	 * Creates a new RandomXMX256 with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public RandomXMX256(long seed) {
		super(seed);
		setSeed(seed);
	}

	/**
	 * Creates a new RandomXMX256 with the given four states; all {@code long} values are permitted.
	 * These states will be used verbatim, as long as they are not all 0. In that case, stateD is changed.
	 *
	 * @param stateA any {@code long} value
	 * @param stateB any {@code long} value
	 * @param stateC any {@code long} value
	 * @param stateD any {@code long} value
	 */
	public RandomXMX256(long stateA, long stateB, long stateC, long stateD) {
		super(stateA);
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
		if ((stateA | stateB | stateC | stateD) == 0L)
			this.stateD = 0x9E3779B97F4A7C15L;
	}

	/**
	 * This generator has 4 {@code long} states, so this returns 4.
	 *
	 * @return 4 (four)
	 */
	@Override
	public int getStateCount () {
		return 4;
	}

	/**
	 * Gets the state determined by {@code selection}, as-is. The value for selection should be
	 * between 0 and 3, inclusive; if it is any other value this gets state D as if 3 was given.
	 *
	 * @param selection used to select which state variable to get; generally 0, 1, 2, or 3
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
		default:
			return stateD;
		}
	}

	/**
	 * Sets one of the states, determined by {@code selection}, to {@code value}, as-is.
	 * Selections 0, 1, 2, and 3 refer to states A, B, C, and D,  and if the selection is anything
	 * else, this treats it as 3 and sets stateD. If this would cause all states to be 0, it
	 * instead sets the selected state to 0x9E3779B97F4A7C15L.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, or 3
	 * @param value     the exact value to use for the selected state, if valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		switch (selection) {
		case 0:
			stateA = ((value | stateB | stateC | stateD) == 0L) ? 0x9E3779B97F4A7C15L : value;
			break;
		case 1:
			stateB = ((stateA | value | stateC | stateD) == 0L) ? 0x9E3779B97F4A7C15L : value;
			break;
		case 2:
			stateC = ((stateA | stateB | value | stateD) == 0L) ? 0x9E3779B97F4A7C15L : value;
			break;
		default:
			stateD = ((stateA | stateB | stateC | value) == 0L) ? 0x9E3779B97F4A7C15L : value;
			break;
		}
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here, all with a different
	 * first value returned by {@link #nextLong()} (because {@code stateB} is guaranteed to be
	 * different for every different {@code seed}).
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		long x = (seed + 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateA = x ^ x >>> 27;
		x = (seed + 0x3C6EF372FE94F82AL);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateB = x ^ x >>> 27;
		x = (seed + 0xDAA66D2C7DDF743FL);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateC = x ^ x >>> 27;
		x = (seed + 0x78DDE6E5FD29F054L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateD = x ^ x >>> 27;
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
	 * Sets the second part of the state. Note that if you set this state to 0, the next random long (or most other types)
	 * will be 0, regardless of the other states.
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

	/**
	 * Sets the state completely to the given four state variables.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group.
	 *
	 * @param stateA the first state; can be any long
	 * @param stateB the second state; can be any long
	 * @param stateC the third state; can be any long
	 * @param stateD the fourth state; this will be returned as-is if the next call is to {@link #nextLong()}
	 */
	@Override
	public void setState (long stateA, long stateB, long stateC, long stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
		if ((stateA | stateB | stateC | stateD) == 0L)
			this.stateD = 0x9E3779B97F4A7C15L;
	}

	@Override
	public long nextLong () {
		long result = stateB;
		long t = stateB << 17;
		stateC ^= stateA;
		stateD ^= stateB;
		stateB ^= stateC;
		stateA ^= stateD;
		stateC ^= t;
		stateD = (stateD << 45 | stateD >>> 19);
		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 29;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		return result ^ result >>> 29;
	}

	@Override
	public int next (int bits) {
		long result = stateB;
		long t = stateB << 17;
		stateC ^= stateA;
		stateD ^= stateB;
		stateB ^= stateC;
		stateA ^= stateD;
		stateC ^= t;
		stateD = (stateD << 45 | stateD >>> 19);
		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 29;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		return (int)((result ^ result >>> 29) >>> 64 - bits);
	}

	@Override
	public long previousLong () {
		stateD = (stateD << 19 | stateD >>> 45); // stateD has d ^ b
		stateA ^= stateD; // StateA has a
		stateC ^= stateB; // StateC has b ^ b << 17;
		stateC ^= stateC << 17;
		stateC ^= stateC << 34; // StateC has b
		stateB ^= stateA; // StateB has b ^ c
		stateC ^= stateB; // StateC has c;
		long result = stateB ^= stateC; // StateB has b;
		stateD ^= stateB; // StateD has d;

		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 29;
		result *= 0xBEA225F9EB34556DL;
		result ^= result >>> 32;
		result *= 0xBEA225F9EB34556DL;
		return result ^ result >>> 29;
	}

	/**
	 * Jumps extremely far in the generator's sequence, such that it requires {@code Math.pow(2, 64)} calls to leap() to
	 * complete a cycle through the generator's entire sequence. This can be used to create over 18 quintillion
	 * substreams of this generator's sequence, each with a period of {@code Math.pow(2, 192)}.
	 * @return the result of what nextLong() would return if it was called at the state this jumped to
	 */
	public long leap()
	{
		long s0 = 0L;
		long s1 = 0L;
		long s2 = 0L;
		long s3 = 0L;
		for (long b = 0x76e15d3efefdcbbfL; b != 0L; b >>>= 1)
		{
			if ((1L & b) != 0L)
			{
				s0 ^= stateA;
				s1 ^= stateB;
				s2 ^= stateC;
				s3 ^= stateD;
			}
			nextLong();
		}
		for (long b = 0xc5004e441c522fb3L; b != 0L; b >>>= 1)
		{
			if ((1L & b) != 0L)
			{
				s0 ^= stateA;
				s1 ^= stateB;
				s2 ^= stateC;
				s3 ^= stateD;
			}
			nextLong();
		}
		for (long b = 0x77710069854ee241L; b != 0L; b >>>= 1)
		{
			if ((1L & b) != 0L)
			{
				s0 ^= stateA;
				s1 ^= stateB;
				s2 ^= stateC;
				s3 ^= stateD;
			}
			nextLong();
		}
		for (long b = 0x39109bb02acbe635L; b != 0L; b >>>= 1)
		{
			if ((1L & b) != 0L)
			{
				s0 ^= stateA;
				s1 ^= stateB;
				s2 ^= stateC;
				s3 ^= stateD;
			}
			nextLong();
		}

		stateA = s0;
		stateB = s1;
		stateC = s2;
		stateD = s3;


		s3 = (s3 << 19 | s3 >>> 45); // s3 has d ^ b
		s0 ^= s3; // s0 has a
		s2 ^= s1; // s2 has b ^ b << 17;
		s2 ^= s2 << 17;
		s2 ^= s2 << 34; // s2 has b
		s1 ^= s0; // s1 has b ^ c
		s2 ^= s1; // s2 has c;
		s1 ^= s2; // StateB has b;

		s1 ^= s1 >>> 32;
		s1 *= 0xBEA225F9EB34556DL;
		s1 ^= s1 >>> 29;
		s1 *= 0xBEA225F9EB34556DL;
		s1 ^= s1 >>> 32;
		s1 *= 0xBEA225F9EB34556DL;
		return s1 ^ s1 >>> 29;
	}


	@Override
	public RandomXMX256 copy () {
		return new RandomXMX256(stateA, stateB, stateC, stateD);
	}

	/**
	 * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this RandomXMX256
	 * object to match the state in the serialized data. Returns this RandomXMX256, after possibly changing its state.
	 *
	 * @param data a String probably produced by {@link #stringSerialize()}
	 * @return this, after setting its state
	 */
	@Override
	public RandomXMX256 stringDeserialize(String data) {
		super.stringDeserialize(data);
		return this;
	}
	
	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RandomXMX256 that = (RandomXMX256)o;

		if (stateA != that.stateA)
			return false;
		if (stateB != that.stateB)
			return false;
		if (stateC != that.stateC)
			return false;
		return stateD == that.stateD;
	}

	public String toString () {
		return "RandomXMX256{" + "stateA=" + (stateA) + "L, stateB=" + (stateB) + "L, stateC=" + (stateC) + "L, stateD=" + (stateD) + "L}";
	}
}
