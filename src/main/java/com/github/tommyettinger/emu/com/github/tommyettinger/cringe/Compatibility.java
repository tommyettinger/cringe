/*
 * Copyright (c) 2022 See AUTHORS file.
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
 */

package com.github.tommyettinger.cringe;

import com.google.gwt.typedarrays.client.Float64ArrayNative;
import com.google.gwt.typedarrays.client.Float32ArrayNative;
import com.google.gwt.typedarrays.client.Int32ArrayNative;
import com.google.gwt.typedarrays.client.Int8ArrayNative;
import com.google.gwt.typedarrays.client.DataViewNative;
import com.google.gwt.typedarrays.shared.Float64Array;
import com.google.gwt.typedarrays.shared.Float32Array;
import com.google.gwt.typedarrays.shared.Int32Array;
import com.google.gwt.typedarrays.shared.Int8Array;
import com.google.gwt.typedarrays.shared.DataView;

public final class Compatibility {
	private Compatibility() {
	}

	public static final Int8Array wba = Int8ArrayNative.create(8);
	public static final Int32Array wia = Int32ArrayNative.create(wba.buffer(), 0, 2);
	public static final Float32Array wfa = Float32ArrayNative.create(wba.buffer(), 0, 2);
	public static final Float64Array wda = Float64ArrayNative.create(wba.buffer(), 0, 1);
	public static final DataView dv = DataViewNative.create(wba.buffer());

	public static long doubleToLongBits (final double value) {
		wda.set(0, value);
		return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
	}

	public static long doubleToRawLongBits (final double value) {
		wda.set(0, value);
		return ((long)wia.get(1) << 32) | (wia.get(0) & 0xffffffffL);
	}

	public static double longBitsToDouble (final long bits) {
		wia.set(1, (int)(bits >>> 32));
		wia.set(0, (int)(bits & 0xffffffffL));
		return wda.get(0);
	}

	public static long doubleToReversedLongBits (final double value) {
		dv.setFloat64(0, value, true);
		return ((long)dv.getInt32(0, false) << 32) | (dv.getInt32(4, false) & 0xffffffffL);
	}

	public static double reversedLongBitsToDouble (final long bits) {
		dv.setInt32(4, (int)(bits >>> 32), true);
		dv.setInt32(0, (int)(bits & 0xffffffffL), true);
		return dv.getFloat64(0, false);
	}

	public static int doubleToLowIntBits (final double value) {
		wda.set(0, value);
		return wia.get(0);
	}

	public static int doubleToHighIntBits (final double value) {
		wda.set(0, value);
		return wia.get(1);
	}

	public static int doubleToMixedIntBits (final double value) {
		wda.set(0, value);
		return wia.get(0) ^ wia.get(1);
	}

	public static int floatToIntBits (final float value) {
		wfa.set(0, value);
		return wia.get(0);
	}

	public static int floatToRawIntBits (final float value) {
		wfa.set(0, value);
		return wia.get(0);
	}

	public static int floatToReversedIntBits (final float value) {
		dv.setFloat32(0, value, true);
		return dv.getInt32(0, false);
	}

	public static float reversedIntBitsToFloat (final int bits) {
		dv.setInt32(0, bits, true);
		return dv.getFloat32(0, false);
	}

	public static float intBitsToFloat (final int bits) {
		wia.set(0, bits);
		return wfa.get(0);
	}

	public static int lowestOneBit(int num) {
		return num & -num;
	}

	public static long lowestOneBit(long num) {
		return num & ~(num - 1L);
	}

	public static native int imul(int left, int right)/*-{
	    return Math.imul(left, right);
	}-*/;

	public static int getExponent(float num) {
		wfa.set(0, num);
		return (wia.get(0) >>> 23 & 0xFF) - 0x7F;
	}

	public static int getExponent(double num) {
		wda.set(0, num);
		return (wia.get(1) >>> 52 & 0x7FF) - 0x3FF;
	}

	public static native int countLeadingZeros(int n)/*-{
	    return Math.clz32(n);
	}-*/;

	public static native int countTrailingZeros(int n)/*-{
	    var i = -n;
	    return ((n | i) >> 31 | 32) & 31 - Math.clz32(n & i);
	}-*/;

	public static int countLeadingZeros(long n) {
		// we store the top 32 bits first.
		int x = (int)(n >>> 32);
		// if the top 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the low 32 bits.
		return x == 0 ? 32 + countLeadingZeros((int)n) : countLeadingZeros(x);
	}


	public static int countTrailingZeros(long n) {
		// we store the bottom 32 bits first.
		int x = (int)n;
		// if the bottom 32 bits are 0, we know we don't need to count zeros in them.
		// if they aren't 0, we know there is a 1 bit in there, so we don't need to count the high 32 bits.
		return x == 0 ? 32 + countTrailingZeros((int)(n >>> 32)) : countTrailingZeros(x);
	}

}
