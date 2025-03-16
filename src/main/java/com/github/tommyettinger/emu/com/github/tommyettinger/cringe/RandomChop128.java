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
 * A random number generator that is optimized for performance on 32-bit machines and with Google Web Toolkit, this uses
 * only add, bitwise-rotate, and XOR operations (no multiplication). Has a state that runs like a counter, guaranteeing
 * a minimum period of 2 to the 32.
 * <br>
 * The actual speed of this is going to vary wildly depending on the platform being benchmarked. It's hard to find a
 * faster high-quality way to generate long values on GWT (this is, surprisingly, faster than generators like
 * {@link RandomAce320} or {@link RandomDistinct64} on GWT at generating either int or long values, while this is likely
 * half the speed of RandomAce320 when generating long values on Java 17 HotSpot). RandomChop128 has a guaranteed
 * minimum period of 2 to the 32, and is very likely to have a much longer period for almost all initial states.
 * <br>
 * This generator passes 64TB of PractRand testing without anomalies. Its expected period is much higher than its
 * minimum guaranteed period (2 to the 32 calls to {@link #nextInt()}), and is most likely about 2 to the 80. This has
 * many possible sub-cycles, and the longest subcycle is (nearly) guaranteed to have a period less than 2 to the 128.
 * <br>
 * The algorithm used here has four states purely to exploit instruction-level parallelism; one state is a counter (this
 * gives the guaranteed minimum period of 2 to the 32), and the others combine the values of the four states across three
 * variables. There's a complex tangle of dependencies across the states, but it is possible to invert the generator
 * given a full 128-bit state; this is vital for its period and quality.
 * <br>
 * It is strongly recommended that you seed this with {@link #setSeed(long)} instead of
 * {@link #setState(long, long, long, long)}, because if you give sequential seeds to both setSeed() and setState(), the
 * former will start off random, while the latter will start off repeating the seed sequence. After about 20-40 random
 * numbers generated, any correlation between similarly seeded generators will probably be completely gone, though.
 * <br>
 * This class is a {@link GdxRandom} and is also a JDK {@link java.util.Random} as a result.
 * This implements all optional methods in GdxRandom except {@link #skip(long)}; it does implement
 * {@link #previousLong()} (and {@link #previousInt()}) without using skip().
 * <br>
 * This is called RandomChop128 because it operates on words that are "chopped in half" relative to similar generators.
 */
public class RandomChop128 extends GdxRandom {
	/**
	 * Returns the String {@code "RandomChop128"}, which is the tag here.
	 * @return the String {@code "RandomChop128"}
	 */
	@Override
	public String getTag() {
		return "RandomChop128";
	}

	/**
	 * The first state; can be any int.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public int stateA;
	/**
	 * The second state; can be any int.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public int stateB;
	/**
	 * The third state; can be any int.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public int stateC;
	/**
	 * The fourth state; can be any int.
	 * <br>
	 * This is a public field to match the style used by libGDX and to make changes easier.
	 */
	public int stateD;

	/**
	 * Creates a new RandomChop128 with a random state.
	 */
	public RandomChop128() {
		stateA = (int)seedFromMath();
		stateB = (int)seedFromMath();
		stateC = (int)seedFromMath();
		stateD = (int)seedFromMath();
	}

	/**
	 * Creates a new RandomChop128 with the given seed; all {@code long} values are permitted.
	 * The seed will be passed to {@link #setSeed(long)} to attempt to adequately distribute the seed randomly.
	 *
	 * @param seed any {@code long} value
	 */
	public RandomChop128(long seed) {
		setSeed(seed);
	}

	/**
	 * Creates a new RandomChop128 with the given five states; all {@code int} values are permitted.
	 * These states will be used verbatim.
	 *
	 * @param stateA any {@code int} value
	 * @param stateB any {@code int} value
	 * @param stateC any {@code int} value
	 * @param stateD any {@code int} value
	 */
	public RandomChop128(int stateA, int stateB, int stateC, int stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	/**
	 * This generator has 4 {@code int} states, so this returns 4.
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
	 * @return the value of the selected state, which is an int that will be promoted to long
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
	 * Sets one of the states, determined by {@code selection}, to the lower 32 bits of {@code value}, as-is.
	 * Selections 0, 1, 2, and 3 refer to states A, B, C, and D,  and if the selection is anything
	 * else, this treats it as 3 and sets stateD. This always casts {@code value} to an int before using it.
	 *
	 * @param selection used to select which state variable to set; generally 0, 1, 2, or 3
	 * @param value     the exact value to use for the selected state, if valid
	 */
	@Override
	public void setSelectedState (int selection, long value) {
		switch (selection) {
			case 0:
				stateA = (int)value;
				break;
			case 1:
				stateB = (int)value;
				break;
			case 2:
				stateC = (int)value;
				break;
			default:
				stateD = (int)value;
				break;
		}
	}

	/**
	 * This initializes all 4 states of the generator to random values based on the given seed.
	 * (2 to the 64) possible initial generator states can be produced here, all with a different
	 * first value returned by {@link #nextLong()} (because {@code stateC} is guaranteed to be
	 * different for every different {@code seed}).
	 *
	 * @param seed the initial seed; may be any long
	 */
	@Override
	public void setSeed (long seed) {
		long x = (seed += 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateA = (int)(x ^ x >>> 27);
		x = (seed += 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateB = (int)(x ^ x >>> 27);
		x = (seed += 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateC = (int)(x ^ x >>> 27);
		x = (seed + 0x9E3779B97F4A7C15L);
		x ^= x >>> 27;
		x *= 0x3C79AC492BA7B653L;
		x ^= x >>> 33;
		x *= 0x1C69B3F74AC4AE35L;
		stateD = (int)(x ^ x >>> 27);
	}

	public long getStateA () {
		return stateA;
	}

	/**
	 * Sets the first part of the state by casting the parameter to an int.
	 *
	 * @param stateA can be any long, but will be cast to an int before use
	 */
	public void setStateA (long stateA) {
		this.stateA = (int)stateA;
	}

	public long getStateB () {
		return stateB;
	}

	/**
	 * Sets the second part of the state by casting the parameter to an int.
	 *
	 * @param stateB can be any long, but will be cast to an int before use
	 */
	public void setStateB (long stateB) {
		this.stateB = (int)stateB;
	}

	public long getStateC () {
		return stateC;
	}

	/**
	 * Sets the third part of the state by casting the parameter to an int.
	 * Note that if you call {@link #nextInt()} immediately after this,
	 * it will return the given {@code stateC} (cast to int) as-is, so you
	 * may want to call some random generation methods (such as nextInt()) and discard
	 * the results after setting the state.
	 *
	 * @param stateC can be any long, but will be cast to an int before use
	 */
	public void setStateC (long stateC) {
		this.stateC = (int)stateC;
	}

	public long getStateD () {
		return stateD;
	}

	/**
	 * Sets the fourth part of the state by casting the parameter to an int.
	 *
	 * @param stateD can be any long, but will be cast to an int before use
	 */
	public void setStateD (long stateD) {
		this.stateD = (int)stateD;
	}

	/**
	 * Sets the state completely to the given four state variables, casting each to an int.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group. You may want
	 * to call {@link #nextInt()} a few times after setting the states like this, unless
	 * the value for stateC (in particular) is already adequately random; the first call
	 * to {@link #nextInt()}, if it is made immediately after calling this, will return {@code stateC} as-is.
	 *
	 * @param stateA the first state; can be any long, but will be cast to an int before use
	 * @param stateB the second state; can be any long, but will be cast to an int before use
	 * @param stateC the third state; can be any long, but will be cast to an int before use
	 * @param stateD the fourth state; can be any long, but will be cast to an int before use
	 */
	@Override
	public void setState (long stateA, long stateB, long stateC, long stateD) {
		this.stateA = (int)stateA;
		this.stateB = (int)stateB;
		this.stateC = (int)stateC;
		this.stateD = (int)stateD;
	}

	/**
	 * Sets the state completely to the given four state variables.
	 * This is the same as calling {@link #setStateA(long)}, {@link #setStateB(long)},
	 * {@link #setStateC(long)}, and {@link #setStateD(long)} as a group, but without any
	 * {@code long} values inuse. You may want
	 * to call {@link #nextInt()} a few times after setting the states like this, unless
	 * the value for stateC (in particular) is already adequately random; the first call
	 * to {@link #nextInt()}, if it is made immediately after calling this, will return {@code stateC} as-is.
	 *
	 * @param stateA the first state; can be any int
	 * @param stateB the second state; can be any int
	 * @param stateC the third state; can be any int
	 * @param stateD the fourth state; can be any int
	 */
	public void setState (int stateA, int stateB, int stateC, int stateD) {
		this.stateA = stateA;
		this.stateB = stateB;
		this.stateC = stateC;
		this.stateD = stateD;
	}

	@Override
	public long nextLong () {
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc; ga = (ga << 26 | ga >>>  6);
		int gb = fc ^ fd; gb = (gb << 11 | gb >>> 21);
		final int gc = fa ^ fb + fc;
		final int gd = fd + 0xADB5B165;
		fa = gb ^ gc;
		stateA = (fa << 26 | fa >>> 6);
		fb = gc ^ gd;
		stateB = (fb << 11 | fb >>> 21);
		stateC = ga ^ gb + gc;
		stateD = fd + 0x5B6B62CA | 0;
		return (long)fc << 32 ^ gc;
	}

	@Override
	public long previousLong () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int gc = (fb >>> 11 | fb << 21) ^ (stateD - 0xADB5B165);
		final int gb = (fa >>> 26 | fa << 6) ^ gc;
		final int ga = fc ^ gb + gc;
		stateC = (gb >>> 11 | gb << 21) ^ (stateD = stateD - 0x5B6B62CA | 0);
		stateB = (ga >>> 26 | ga << 6) ^ stateC;
		stateA = gc ^ stateB + stateC;

		return (long)stateC << 32 ^ gc;
	}

	@Override
	public int nextInt () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return fc;
	}

	public int previousInt() {
		final int ga = stateA;
		final int gb = stateB;
		final int gc = stateC;
		stateC = (gb >>> 11 | gb << 21) ^ (stateD = stateD - 0xADB5B165 | 0);
		stateB = (ga >>> 26 | ga << 6) ^ stateC;
		stateA = gc ^ stateB + stateC;

		return stateC;
	}

	@Override
	public int next (int bits) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return fc >>> (32 - bits);
	}


	@Override
	public int nextInt (int bound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (int)(bound * (fc & 0xFFFFFFFFL) >> 32) & ~(bound >> 31);
	}

	@Override
	public int nextSignedInt (int outerBound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		outerBound = (int)(outerBound * (fc & 0xFFFFFFFFL) >> 32);
		return outerBound + (outerBound >>> 31) | 0;
	}

	@Override
	public int nextInt (int inner, int outer) {
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		stateA = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		stateB = (gb << 11 | gb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		if(inner >= outer) return inner;
		return (int)(inner + (
				((outer - inner & 0xFFFFFFFFL) * (fc & 0xFFFFFFFFL) >>> 32)
						& ~((long)outer - (long)inner >> 63)));
	}

	@Override
	public int nextSignedInt (int inner, int outer) {
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		stateA = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		stateB = (gb << 11 | gb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		if(inner >= outer) return inner;
		return (int)(inner +
				((outer - inner & 0xFFFFFFFFL) * (fc & 0xFFFFFFFFL) >>> 32));
	}

	@Override
	public int nextUnsignedInt(int bound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (int)((bound & 0xFFFFFFFFL) * (fc & 0xFFFFFFFFL) >>> 32);
	}

	@Override
	public void nextBytes (byte[] bytes) {
		if (bytes != null) {
			for (int i = 0; i < bytes.length; ) {
				final int fa = stateA;
				final int fb = stateB;
				int fc = stateC;
				final int fd = stateD;
				final int sa = fb ^ fc;
				stateA = (sa << 26 | sa >>> 6);
				final int sb = fc ^ fd;
				stateB = (sb << 11 | sb >>> 21);
				stateC = fa ^ fb + fc;
				stateD = fd + 0xADB5B165 | 0;
				for (int n = Math.min(bytes.length - i, 4); n-- > 0; fc >>>= 8) {
					bytes[i++] = (byte) fc;
				}
			}
		}
	}

	@Override
	public long nextLong (long outer) {
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		ga = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		gb = (gb << 11 | gb >>> 21);
		int gc = fa ^ fb + fc;
		int gd = fd + 0xADB5B165;
		fa = gb ^ gc;
		stateA = (fa << 26 | fa >>> 6);
		fb = gc ^ gd;
		stateB = (fb << 11 | fb >>> 21);
		stateC = ga ^ gb + gc;
		stateD = fd + 0x5B6B62CA | 0;
		if(1 >= outer) return 0;
		final long randLow = fc & 0xFFFFFFFFL;
		final long randHigh = gc & 0xFFFFFFFFL;
		final long boundLow = outer & 0xFFFFFFFFL;
		final long boundHigh = (outer >>> 32);
		return (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	@Override
	public long nextLong (long inner, long outer) {
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		ga = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		gb = (gb << 11 | gb >>> 21);
		int gc = fa ^ fb + fc;
		int gd = fd + 0xADB5B165;
		fa = gb ^ gc;
		stateA = (fa << 26 | fa >>> 6);
		fb = gc ^ gd;
		stateB = (fb << 11 | fb >>> 21);
		stateC = ga ^ gb + gc;
		stateD = fd + 0x5B6B62CA | 0;
		if(inner >= outer) return inner;
		final long randLow = fc & 0xFFFFFFFFL;
		final long randHigh = gc & 0xFFFFFFFFL;
		final long bound = outer - inner;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long boundHigh = (bound >>> 32);
		return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	@Override
	public long nextSignedLong (long outer) {
		long inner;
		if(outer < 0) {
			inner = outer + 1L;
			outer = 1L;
		}
		else {
			inner = 0L;
		}
		final long bound = outer - inner;
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		ga = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		gb = (gb << 11 | gb >>> 21);
		int gc = fa ^ fb + fc;
		int gd = fd + 0xADB5B165;
		fa = gb ^ gc;
		stateA = (fa << 26 | fa >>> 6);
		fb = gc ^ gd;
		stateB = (fb << 11 | fb >>> 21);
		stateC = ga ^ gb + gc;
		stateD = fd + 0x5B6B62CA | 0;
		final long randLow = fc & 0xFFFFFFFFL;
		final long randHigh = gc & 0xFFFFFFFFL;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long boundHigh = (bound >>> 32);
		return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	@Override
	public long nextSignedLong (long inner, long outer) {
		if(outer < inner) {
			long t = outer;
			outer = inner + 1L;
			inner = t + 1L;
		}
		final long bound = outer - inner;
		int fa = stateA;
		int fb = stateB;
		int fc = stateC;
		int fd = stateD;
		int ga = fb ^ fc;
		ga = (ga << 26 | ga >>> 6);
		int gb = fc ^ fd;
		gb = (gb << 11 | gb >>> 21);
		int gc = fa ^ fb + fc;
		int gd = fd + 0xADB5B165;
		fa = gb ^ gc;
		stateA = (fa << 26 | fa >>> 6);
		fb = gc ^ gd;
		stateB = (fb << 11 | fb >>> 21);
		stateC = ga ^ gb + gc;
		stateD = fd + 0x5B6B62CA | 0;
		final long randLow = fc & 0xFFFFFFFFL;
		final long randHigh = gc & 0xFFFFFFFFL;
		final long boundLow = bound & 0xFFFFFFFFL;
		final long boundHigh = (bound >>> 32);
		return inner + (randHigh * boundLow >>> 32) + (randLow * boundHigh >>> 32) + randHigh * boundHigh;
	}

	@Override
	public boolean nextBoolean () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return fc < 0;
	}

	@Override
	public float nextFloat () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (fc >>> 8) * 0x1p-24f;
	}

	@Override
	public float nextFloat (float outerBound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (fc >>> 8) * 0x1p-24f * outerBound;

	}

	@Override
	public float nextFloat (float innerBound, float outerBound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return innerBound + (fc >>> 8) * 0x1p-24f * (outerBound - innerBound);
	}

	@Override
	public float nextInclusiveFloat () {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (0x1000001L * (fc & 0xFFFFFFFFL) >> 32) * 0x1p-24f;
	}

	@Override
	public float nextInclusiveFloat (float outerBound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return (0x1000001L * (fc & 0xFFFFFFFFL) >> 32) * 0x1p-24f * outerBound;
	}

	@Override
	public float nextInclusiveFloat (float innerBound, float outerBound) {
		final int fa = stateA;
		final int fb = stateB;
		final int fc = stateC;
		final int fd = stateD;
		final int sa = fb ^ fc; stateA = (sa << 26 | sa >>>  6);
		final int sb = fc ^ fd; stateB = (sb << 11 | sb >>> 21);
		stateC = fa ^ fb + fc;
		stateD = fd + 0xADB5B165 | 0;
		return innerBound + (0x1000001L * (fc & 0xFFFFFFFFL) >> 32) * 0x1p-24f * (outerBound - innerBound);
	}

	@Override
	public RandomChop128 copy () {
		return new RandomChop128(stateA, stateB, stateC, stateD);
	}

	/**
	 * Given a String in the format produced by {@link #stringSerialize()}, this will attempt to set this RandomChop128
	 * object to match the state in the serialized data. Returns this RandomChop128, after possibly changing its state.
	 *
	 * @param data a String probably produced by {@link #stringSerialize()}
	 * @return this, after setting its state
	 */
	@Override
	public RandomChop128 stringDeserialize(String data) {
		super.stringDeserialize(data);
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		RandomChop128 that = (RandomChop128)o;

		return stateA == that.stateA && stateB == that.stateB && stateC == that.stateC && stateD == that.stateD;
	}

	public String toString () {
		return "RandomChop128{" + "stateA=" + (stateA) + ", stateB=" + (stateB) + ", stateC=" + (stateC) + ", stateD=" + (stateD) + "}";
	}
}
