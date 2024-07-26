/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ContinuousNoise extends RawNoise {

    /**
     * "Standard" layered octaves of noise, where each octave has a different frequency and weight.
     * Tends to look cloudy with more octaves, and generally like a natural process. This only has
     * an effect with 2 octaves or more.
     * <br>
     * Meant to be used with {@link #setMode(int)}.
     */
    public static final int FBM = 0;
    /**
     * A less common way to layer octaves of noise, where most results are biased toward higher values,
     * but "valleys" show up filled with much lower values.
     * This probably has some good uses in 3D or higher noise, but it isn't used too frequently.
     * <br>
     * Meant to be used with {@link #setMode(int)}.
     */
    public static final int BILLOW = 1;
    /**
     * A way to layer octaves of noise so most values are biased toward low values but "ridges" of high
     * values run across the noise. This can be a good way of highlighting the least-natural aspects of
     * some kinds of noise; Perlin Noise has mostly ridges along 45-degree angles,
     * Simplex Noise has many ridges along a triangular grid, and so on. Foam Noise
     * and Honey Noise do well with this mode, though, and look something like lightning or
     * bubbling fluids, respectively. Using Foam or Honey will have this look natural, but Perlin in
     * particular will look unnatural if the grid is visible.
     * <br>
     * Meant to be used with {@link #setMode(int)}.
     */
    public static final int RIDGED = 2;
    /**
     * Layered octaves of noise, where each octave has a different frequency and weight, and the results of
     * earlier octaves affect the inputs to later octave calculations. Tends to look cloudy but with swirling
     * distortions, and generally like a natural process. This only has an effect with 2 octaves or more.
     * <br>
     * Meant to be used with {@link #setMode(int)}.
     */
    public static final int WARP = 3;
    /**
     * Layered octaves of noise, like {@link #WARP} but much more involved. Tends to produce fingerprint-like
     * whorls and loops.
     * See <a href="https://thingonitsown.blogspot.com/2019/02/exo-terrain.html">this blog post</a> for more info.
     * <br>
     * Meant to be used with {@link #setMode(int)}.
     */
    public static final int EXO = 4;

    /**
     * The names that correspond to the numerical mode constants, with the constant value matching the index here.
     */
    public static final String[] MODES = {"FBM", "Billow", "Ridged", "Warp", "Exo"};

    public RawNoise wrapped;
    public float frequency;
    public int mode;
    protected int octaves;

    public ContinuousNoise() {
        this(new ValueNoise(123), 123, 0.03125f, FBM, 1);

    }

    public ContinuousNoise(RawNoise toWrap){
        this(toWrap, toWrap.getSeed(), 0.03125f, FBM, 1);
    }

    public ContinuousNoise(RawNoise toWrap, float frequency, int mode, int octaves){
        this(toWrap, toWrap.getSeed(), frequency, mode, octaves);
    }
    public ContinuousNoise(RawNoise toWrap, int seed, float frequency, int mode, int octaves){
        wrapped = toWrap;
        setSeed(seed);
        this.frequency = frequency;
        this.mode = mode;
        this.octaves = octaves;
    }

    public ContinuousNoise(ContinuousNoise other) {
        setWrapped(other.getWrapped().copy());
        setSeed(other.getSeed());
        setFrequency(other.getFrequency());
        setFractalType(other.getFractalType());
        setFractalOctaves(other.getFractalOctaves());
    }

    public RawNoise getWrapped() {
        return wrapped;
    }

    public void setWrapped(RawNoise wrapped) {
        this.wrapped = wrapped;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    /**
     * Wraps {@link #getFractalType()}.
     * @return an int between 0 and 4, corresponding to {@link #FBM}, {@link #BILLOW}, {@link #RIDGED}, {@link #WARP}, or {@link #EXO}
     */
    public int getMode() {
        return getFractalType();
    }

    /**
     * Wraps {@link #setFractalType(int)}
     * @param mode an int between 0 and 4, corresponding to {@link #FBM}, {@link #BILLOW}, {@link #RIDGED}, {@link #WARP}, or {@link #EXO}
     */
    public void setMode(int mode) {
        setFractalType(mode);
    }

    public int getFractalType() {
        return mode;
    }

    /**
     * @param mode an int between 0 and 4, corresponding to {@link #FBM}, {@link #BILLOW}, {@link #RIDGED}, {@link #WARP}, or {@link #EXO}
     */
    public void setFractalType(int mode) {
        this.mode = mode;
    }

    /**
     * Wraps {@link #getFractalOctaves()}.
     * @return how many octaves this uses to increase detail
     */
    public int getOctaves() {
        return getFractalOctaves();
    }

    /**
     * Wraps {@link #setFractalOctaves(int)}.
     * @param octaves how many octaves to use to increase detail; must be at least 1.
     */
    public void setOctaves(int octaves) {
        setFractalOctaves(octaves);
    }

    public int getFractalOctaves() {
        return octaves;
    }

    /**
     * @param octaves how many octaves to use to increase detail; must be at least 1.
     */
    public void setFractalOctaves(int octaves) {
        this.octaves = Math.max(1, octaves);
    }

    @Override
    public int getMinDimension() {
        return wrapped.getMinDimension();
    }

    @Override
    public int getMaxDimension() {
        return wrapped.getMaxDimension();
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return wrapped.hasEfficientSetSeed();
    }

    @Override
    public String getTag() {
        return "ContinuousNoise";
    }

    @Override
    public String stringSerialize() {
        return "`" + Serializer.serialize(wrapped) + '~' +
                frequency + '~' +
                mode + '~' +
                octaves + '`';
    }

    @Override
    public ContinuousNoise stringDeserialize(String data) {
        int pos = data.indexOf('`', data.indexOf('`', 2) + 1)+1;
        setWrapped(Serializer.deserialize(data.substring(1, pos)));
        setFrequency(MathSupport.floatFromDec(data, pos+1, pos = data.indexOf('~', pos+2)));
        setMode(MathSupport.intFromDec(data, pos+1, pos = data.indexOf('~', pos+2)));
        setOctaves(MathSupport.intFromDec(data, pos+1, pos = data.indexOf('`', pos+2)));
        return this;
    }

    /**
     * Requires the type of the noise generator this wraps ({@link #wrapped}) to be registered.
     * @param out the stream to write the object to
     * @throws IOException
     */
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(wrapped);
        out.writeFloat(frequency);
        out.writeInt(mode);
        out.writeInt(octaves);
    }

    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setWrapped((RawNoise) in.readObject());
        setFrequency(in.readFloat());
        setMode(in.readInt());
        setOctaves(in.readInt());
    }

    @Override
    public ContinuousNoise copy() {
        return new ContinuousNoise(this);
    }

    @Override
    public String toString() {
        return "ContinuousNoise{" +
                "wrapped=" + wrapped +
                ", frequency=" + frequency +
                ", mode=" + mode +
                ", octaves=" + octaves +
                '}';
    }

    @Override
    public String toHumanReadableString() {
        return getTag() + " wrapping (" + wrapped.toHumanReadableString() + "), with frequency " + frequency +
                ", " + octaves + " octaves, and mode " + MODES[mode];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContinuousNoise that = (ContinuousNoise) o;

        if (Float.compare(that.frequency, frequency) != 0) return false;
        if (mode != that.mode) return false;
        if (octaves != that.octaves) return false;
        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        int result = wrapped.hashCode();
        result = 31 * result + (frequency != +0.0f ? NumberUtils.floatToIntBits(frequency) : 0);
        result = 31 * result + mode;
        result = 31 * result + octaves;
        return result;
    }

    // The big part.

    @Override
    public float getNoise(float x) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, seed);
            case 1: return billow(x * frequency, seed);
            case 2: return ridged(x * frequency, seed);
            case 3: return warp(x * frequency, seed);
            case 4: return exo(x * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        final int seed = wrapped.getSeed();
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
        }
    }

    @Override
    public void setSeed(int seed) {
        wrapped.setSeed(seed);
    }

    @Override
    public int getSeed() {
        return wrapped.getSeed();
    }

    @Override
    public float getNoiseWithSeed(float x, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, seed);
            case 1: return billow(x * frequency, seed);
            case 2: return ridged(x * frequency, seed);
            case 3: return warp(x * frequency, seed);
            case 4: return exo(x * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 3: return warp(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 4: return exo(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
        }
    }

    // 1D
    
    protected float fbm(float x, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * 2f;
            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, seed + i + 7777);
            x *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

    // 2D

    protected float fbm(float x, float y, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, float y, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x = x * 2f;
            y = y * 2f;
            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            float b = MathUtils.sinDeg(idx + (180/2f));
            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, float y, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, y * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, y * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, y + striation1 + distort1, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, y * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, y * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, y * 0.5f + striation2 + distort2, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, y * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, y * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, y * 2f, seed + i + 7777);
            x *= 2f;
            y *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

    // 3D
    
    protected float fbm(float x, float y, float z, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
            z *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, float y, float z, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            float b = MathUtils.sinDeg(idx + (180/3f));
            float c = MathUtils.sinDeg(idx + (180*2/3f));

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, float y, float z, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, y * 0.25f, z * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, y * 0.3f, z * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, y + striation1 + distort1, z, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, y * 0.125f, z * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, y * 0.15f, z * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, y * 0.5f + striation2 + distort2, z * 0.5f, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, y * 0.166f, z * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, y * 5f, z * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, y * 2f, z * 2f, seed + i + 7777);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

    // 4D
    
    protected float fbm(float x, float y, float z, float w, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            float b = MathUtils.sinDeg(idx + (180/4f));
            float c = MathUtils.sinDeg(idx + (180*2/4f));
            float d = MathUtils.sinDeg(idx + (180*3/4f));

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, float y, float z, float w, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, y * 0.25f, z * 0.25f, w * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, y * 0.3f, z * 0.3f, w * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, y + striation1 + distort1, z, w, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, y * 0.125f, z * 0.125f, w * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, y * 0.15f, z * 0.15f, w * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, y * 0.5f + striation2 + distort2, z * 0.5f, w * 0.5f, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, y * 0.166f, z * 0.166f, w * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, y * 5f, z * 5f, w * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, y * 2f, z * 2f, w * 2f, seed + i + 7777);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

    // 5D

    protected float fbm(float x, float y, float z, float w, float u, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, u, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, float u, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, float u, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, u, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            float b = MathUtils.sinDeg(idx + (180/5f));
            float c = MathUtils.sinDeg(idx + (180*2/5f));
            float d = MathUtils.sinDeg(idx + (180*3/5f));
            float e = MathUtils.sinDeg(idx + (180*4/5f));

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, u + e, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, float y, float z, float w, float u, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, y * 0.25f, z * 0.25f, w * 0.25f, u * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, y * 0.3f, z * 0.3f, w * 0.3f, u * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, y + striation1 + distort1, z, w, u, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, y * 0.125f, z * 0.125f, w * 0.125f, u * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, y * 0.15f, z * 0.15f, w * 0.15f, u * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, y * 0.5f + striation2 + distort2, z * 0.5f, w * 0.5f, u * 0.5f, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, y * 0.166f, z * 0.166f, w * 0.166f, u * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, y * 5f, z * 5f, w * 5f, u * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, y * 2f, z * 2f, w * 2f, u * 2f, seed + i + 7777);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

    // 6D

    protected float fbm(float x, float y, float z, float w, float u, float v, int seed) {
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            amp *= 0.5f;
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, float v, int seed) {
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            amp *= 0.5f;
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, float u, float v, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;
        }
        return sum / correction - 1f;
    }

    protected float warp(float x, float y, float z, float w, float u, float v, int seed) {
        float latest = wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed);
        if(octaves <= 1) return latest;

        float sum = latest;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;

            final float idx = latest * 180;
            float a = MathUtils.sinDeg(idx);
            float b = MathUtils.sinDeg(idx + (180/6f));
            float c = MathUtils.sinDeg(idx + (180*2/6f));
            float d = MathUtils.sinDeg(idx + (180*3/6f));
            float e = MathUtils.sinDeg(idx + (180*4/6f));
            float f = MathUtils.sinDeg(idx + (180*5/6f));

            amp *= 0.5f;
            sum += (latest = wrapped.getNoiseWithSeed(x + a, y + b, z + c, w + d, u + e, v + f, seed + i)) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float exo(float x, float y, float z, float w, float u, float v, int seed) {
        float power = 0.5f;

        float striation1 = wrapped.getNoiseWithSeed(x * 0.25f, y * 0.25f, z * 0.25f, w * 0.25f, u * 0.25f, v * 0.25f, seed + 1111);
        float distort1 = wrapped.getNoiseWithSeed(x * 0.3f, y * 0.3f, z * 0.3f, w * 0.3f, u * 0.3f, v * 0.3f, seed + 2222);
        float noise1 = wrapped.getNoiseWithSeed(x + striation1 - distort1, y + striation1 + distort1, z, w, u, v, seed) * power;
        for (int i = 1; i < octaves; i++) {
            float striation2 = wrapped.getNoiseWithSeed(x * 0.125f, y * 0.125f, z * 0.125f, w * 0.125f, u * 0.125f, v * 0.125f, seed + i + 3333);
            float distort2 = wrapped.getNoiseWithSeed(x * 0.15f, y * 0.15f, z * 0.15f, w * 0.15f, u * 0.15f, v * 0.15f, seed + i + 4444);
            float noise2 = wrapped.getNoiseWithSeed(x * 0.5f + striation2 - distort2, y * 0.5f + striation2 + distort2, z * 0.5f, w * 0.5f, u * 0.5f, v * 0.5f, seed + i) * 1.5f;
//            float roughness = wrapped.getNoiseWithSeed(x * 0.166f, y * 0.166f, z * 0.166f, w * 0.166f, u * 0.166f, v * 0.166f, seed + i + 5555) - 0.3f;
//            float bumpDistort = wrapped.getNoiseWithSeed(x * 5f, y * 5f, z * 5f, w * 5f, u * 5f, v * 5f, seed + i + 6666);
//            float bumpNoise = wrapped.getNoiseWithSeed((bumpDistort + x) * 2f, y * 2f, z * 2f, w * 2f, u * 2f, v * 2f, seed + i + 7777);
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;
            v *= 2f;
            power *= 0.5f;
            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2) * power;
//            noise1 += (noise2 * noise2 * noise2 + roughness * bumpNoise) * power;
        }

        noise1 /= (0.6f * power * ((1 << octaves) - 1));
        // tanhRougher, from digital.
        // -1f + 2f / (1f + exp(-2f * noise1))
        return -1.0f + 2.0f / (1.0f + Compatibility.intBitsToFloat( (int)(0x800000 * (Math.max(-126.0f, -2.885390043258667f * noise1) + 126.94269504f))));
    }

}
