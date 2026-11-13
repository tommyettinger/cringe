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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

public class WangContinuousNoise extends RawNoise {

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
     * The names that correspond to the numerical mode constants, with the constant value matching the index here.
     */
    public static final String[] MODES = {"FBM", "Billow", "Ridged"};

    public WangRawNoise wrapped;
    public float frequency;
    public int mode;
    protected int octaves;
    private float tileSize = 256;
    public boolean singleEdge = false;

    public WangContinuousNoise() {
        this(new WangPerlinNoise(123), 123, 0.03125f, FBM, 1);

    }

    public WangContinuousNoise(WangRawNoise toWrap){
        this(toWrap, toWrap.getSeed(), 0.03125f, FBM, 1);
    }

    public WangContinuousNoise(WangRawNoise toWrap, float frequency, int mode, int octaves){
        this(toWrap, toWrap.getSeed(), frequency, mode, octaves);
    }
    public WangContinuousNoise(WangRawNoise toWrap, int seed, float frequency, int mode, int octaves){
        wrapped = toWrap;
        setSeed(seed);
        this.frequency = frequency;
        tileSize *= frequency;
        this.mode = mode;
        this.octaves = octaves;
    }

    public WangContinuousNoise(WangContinuousNoise other) {
        setWrapped(other.getWrapped().copy());
        setSeed(other.getSeed());
        setFrequency(other.getFrequency());
        setFractalType(other.getFractalType());
        setFractalOctaves(other.getFractalOctaves());
    }

    public WangRawNoise getWrapped() {
        return wrapped;
    }

    public void setWrapped(WangRawNoise wrapped) {
        this.wrapped = wrapped;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(float frequency) {
        tileSize /= this.frequency;
        this.frequency = frequency;
        tileSize *= frequency;

    }

    /**
     * Wraps {@link #getFractalType()}.
     * @return an int between 0 and 2, corresponding to {@link #FBM}, {@link #BILLOW}, or {@link #RIDGED}
     */
    public int getMode() {
        return getFractalType();
    }

    /**
     * Wraps {@link #setFractalType(int)}
     * @param mode an int between 0 and 2, corresponding to {@link #FBM}, {@link #BILLOW}, or {@link #RIDGED}
     */
    public void setMode(int mode) {
        setFractalType(mode);
    }

    /**
     * @return an int between 0 and 2, corresponding to {@link #FBM}, {@link #BILLOW}, or {@link #RIDGED}
     */
    public int getFractalType() {
        return mode;
    }

    /**
     * @param mode an int between 0 and 2, corresponding to {@link #FBM}, {@link #BILLOW}, or {@link #RIDGED}
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

    public float getTileSize() {
        return MathUtils.roundPositive(tileSize / frequency);
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize * this.frequency;
    }

    public boolean isSingleEdge() {
        return singleEdge;
    }

    public void setSingleEdge(boolean singleEdge) {
        this.singleEdge = singleEdge;
    }

    @Override
    public int getMinDimension() {
        return 2;
    }

    @Override
    public int getMaxDimension() {
        return 6;
    }

    @Override
    public boolean hasEfficientSetSeed() {
        return wrapped.hasEfficientSetSeed();
    }

    @Override
    public String getTag() {
        return "WangContinuousNoise";
    }

    @Override
    public String stringSerialize() {
        return "`" + Serializer.serialize(wrapped) + '~' +
                frequency + '~' +
                mode + '~' +
                octaves + '`';
    }

    @Override
    public WangContinuousNoise stringDeserialize(String data) {
        int pos = data.indexOf('`', data.indexOf('`', 2) + 1)+1;
        setWrapped(WangRawNoise.Serializer.deserialize(data.substring(1, pos)));
        setFrequency(MathSupport.floatFromDec(data, pos+1, pos = data.indexOf('~', pos+2)));
        setMode(MathSupport.intFromDec(data, pos+1, pos = data.indexOf('~', pos+2)));
        setOctaves(MathSupport.intFromDec(data, pos+1, pos = data.indexOf('`', pos+2)));
        return this;
    }

    /**
     * Requires the type of the noise generator this wraps ({@link #wrapped}) to be registered.
     * @param out the stream to write the object to
     * @throws IOException It probably shouldn't???
     */
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(wrapped);
        out.writeFloat(frequency);
        out.writeInt(mode);
        out.writeInt(octaves);
    }

    /**
     * Requires the type of the noise generator ({@link #wrapped}) that is used by {@code in} to be registered.
     * @param in the stream to read data from in order to restore the object
     * @throws IOException It probably shouldn't???
     * @throws ClassNotFoundException if the {@link RawNoise} subclass used by {@code in} is not registered
     */
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        setWrapped((WangPerlinNoise) in.readObject());
        setFrequency(in.readFloat());
        setMode(in.readInt());
        setOctaves(in.readInt());
    }

    @Override
    public WangContinuousNoise copy() {
        return new WangContinuousNoise(this);
    }

    @Override
    public String toString() {
        return "WangContinuousNoise{" +
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

        WangContinuousNoise that = (WangContinuousNoise) o;

        if (Float.compare(that.frequency, frequency) != 0) return false;
        if (mode != that.mode) return false;
        if (octaves != that.octaves) return false;
        return wrapped.equals(that.wrapped);
    }

    @Override
    public int hashCode() {
        int result = wrapped.hashCode();
        result = 31 * result + Compatibility.floatToIntBits(frequency + 0f);
        result = 31 * result + mode;
        result = 31 * result + octaves;
        return result;
    }

    // The big part.

    @Override
    public float getNoise(float x, float y) {
        final int seed = wrapped.getSeed();
        setWangTile2D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z) {
        final int seed = wrapped.getSeed();
        setWangTile3D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w) {
        final int seed = wrapped.getSeed();
        setWangTile4D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u) {
        final int seed = wrapped.getSeed();
        setWangTile5D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
        }
    }

    @Override
    public float getNoise(float x, float y, float z, float w, float u, float v) {
        final int seed = wrapped.getSeed();
        setWangTile6D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
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
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, int seed) {
        setWangTile2D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, int seed) {
        setWangTile3D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, int seed) {
        setWangTile4D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, int seed) {
        setWangTile5D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, seed);
        }
    }

    @Override
    public float getNoiseWithSeed(float x, float y, float z, float w, float u, float v, int seed) {
        setWangTile6D(seed, wrapped);
        switch (mode) {
            default:
            case 0: return fbm(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 1: return billow(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
            case 2: return ridged(x * frequency, y * frequency, z * frequency, w * frequency, u * frequency, v * frequency, seed);
        }
    }

    // 1D
    
    protected float fbm(float x, int seed) {
        wrapped.setEdge((int)(tileSize));
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

    // 2D

    protected float fbm(float x, float y, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = wrapped.getNoiseWithSeed(x, y, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += wrapped.getNoiseWithSeed(x, y, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            wrapped.setEdge((int)(tileSize * (1 << i)));
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
        }
        return sum / correction - 1f;
    }

    // 3D
    
    protected float fbm(float x, float y, float z, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = wrapped.getNoiseWithSeed(x, y, z, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += wrapped.getNoiseWithSeed(x, y, z, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            wrapped.setEdge((int)(tileSize * (1 << i)));
            spike = 1f - Math.abs(wrapped.getNoiseWithSeed(x, y, z, seed + i));
            sum += spike * exp;
            correction += (exp *= 0.5f);
            x *= 2f;
            y *= 2f;
            z *= 2f;
        }
        return sum / correction - 1f;
    }

    // 4D
    
    protected float fbm(float x, float y, float z, float w, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = wrapped.getNoiseWithSeed(x, y, z, w, seed);
        if(octaves <= 1) return sum;

        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += wrapped.getNoiseWithSeed(x, y, z, w, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            wrapped.setEdge((int)(tileSize * (1 << i)));
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

    // 5D

    protected float fbm(float x, float y, float z, float w, float u, int seed) {
        wrapped.setEdge((int)(tileSize));
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
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, int seed) {
        wrapped.setEdge((int)(tileSize));
        float sum = Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed)) * 2 - 1;
        float amp = 1;

        for (int i = 1; i < octaves; i++) {
            x *= 2f;
            y *= 2f;
            z *= 2f;
            w *= 2f;
            u *= 2f;

            amp *= 0.5f;
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, float u, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            wrapped.setEdge((int)(tileSize * (1 << i)));
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

    // 6D

    protected float fbm(float x, float y, float z, float w, float u, float v, int seed) {
        wrapped.setEdge((int)(tileSize));
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
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }
    protected float billow(float x, float y, float z, float w, float u, float v, int seed) {
        wrapped.setEdge((int)(tileSize));
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
            wrapped.setEdge((int)(tileSize * (1 << i)));
            sum += (Math.abs(wrapped.getNoiseWithSeed(x, y, z, w, u, v, seed + i)) * 2 - 1) * amp;
        }

        return sum / (amp * ((1 << octaves) - 1));
    }

    protected float ridged(float x, float y, float z, float w, float u, float v, int seed) {
        float sum = 0f, exp = 1f, correction = 0f, spike;
        for (int i = 0; i < octaves; i++) {
            wrapped.setEdge((int)(tileSize * (1 << i)));
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



    public void setWangTile2D(int index, WangRawNoise noise){
        final int[] altSeeds = noise.getAltSeeds();
        if(singleEdge){
            Arrays.fill(altSeeds, ~index);
            return;
        }
        int seedA = (index & -1 << 4) ^ -4321;
        int seedB = (index & -1 << 4) ^ -5432;
        int i = index & 3;
        int j = index >>> 2 & 3;
        switch (i){
            case 0: altSeeds[0] = seedA; altSeeds[1] = seedA; break;
            case 1: altSeeds[0] = seedA; altSeeds[1] = seedB; break;
            case 2: altSeeds[0] = seedB; altSeeds[1] = seedB; break;
            case 3: altSeeds[0] = seedB; altSeeds[1] = seedA; break;
        }
        switch (j){
            case 0: altSeeds[2] = seedA; altSeeds[3] = seedA; break;
            case 1: altSeeds[2] = seedA; altSeeds[3] = seedB; break;
            case 2: altSeeds[2] = seedB; altSeeds[3] = seedB; break;
            case 3: altSeeds[2] = seedB; altSeeds[3] = seedA; break;
        }
    }

    public void setWangTile3D(int index, WangRawNoise noise){
        final int[] altSeeds = noise.getAltSeeds();
        if(singleEdge){
            Arrays.fill(altSeeds, ~index);
            return;
        }
        int seedA = (index & -1 << 6) ^ -4321;
        int seedB = (index & -1 << 6) ^ -5432;
        int i = index & 3;
        int j = index >>> 2 & 3;
        int k = index >>> 4 & 3;
        switch (i){
            case 0: altSeeds[0] = seedA; altSeeds[1] = seedA; break;
            case 1: altSeeds[0] = seedA; altSeeds[1] = seedB; break;
            case 2: altSeeds[0] = seedB; altSeeds[1] = seedB; break;
            case 3: altSeeds[0] = seedB; altSeeds[1] = seedA; break;
        }
        switch (j){
            case 0: altSeeds[2] = seedA; altSeeds[3] = seedA; break;
            case 1: altSeeds[2] = seedA; altSeeds[3] = seedB; break;
            case 2: altSeeds[2] = seedB; altSeeds[3] = seedB; break;
            case 3: altSeeds[2] = seedB; altSeeds[3] = seedA; break;
        }
        switch (k){
            case 0: altSeeds[4] = seedA; altSeeds[5] = seedA; break;
            case 1: altSeeds[4] = seedA; altSeeds[5] = seedB; break;
            case 2: altSeeds[4] = seedB; altSeeds[5] = seedB; break;
            case 3: altSeeds[4] = seedB; altSeeds[5] = seedA; break;
        }
    }

    public void setWangTile4D(int index, WangRawNoise noise){
        final int[] altSeeds = noise.getAltSeeds();
        if(singleEdge){
            Arrays.fill(altSeeds, ~index);
            return;
        }
        int seedA = (index & -1 << 8) ^ -4321;
        int seedB = (index & -1 << 8) ^ -5432;
        int i = index & 3;
        int j = index >>> 2 & 3;
        int k = index >>> 4 & 3;
        int l = index >>> 6 & 3;
        switch (i){
            case 0: altSeeds[0] = seedA; altSeeds[1] = seedA; break;
            case 1: altSeeds[0] = seedA; altSeeds[1] = seedB; break;
            case 2: altSeeds[0] = seedB; altSeeds[1] = seedB; break;
            case 3: altSeeds[0] = seedB; altSeeds[1] = seedA; break;
        }
        switch (j){
            case 0: altSeeds[2] = seedA; altSeeds[3] = seedA; break;
            case 1: altSeeds[2] = seedA; altSeeds[3] = seedB; break;
            case 2: altSeeds[2] = seedB; altSeeds[3] = seedB; break;
            case 3: altSeeds[2] = seedB; altSeeds[3] = seedA; break;
        }
        switch (k){
            case 0: altSeeds[4] = seedA; altSeeds[5] = seedA; break;
            case 1: altSeeds[4] = seedA; altSeeds[5] = seedB; break;
            case 2: altSeeds[4] = seedB; altSeeds[5] = seedB; break;
            case 3: altSeeds[4] = seedB; altSeeds[5] = seedA; break;
        }
        switch (l){
            case 0: altSeeds[6] = seedA; altSeeds[7] = seedA; break;
            case 1: altSeeds[6] = seedA; altSeeds[7] = seedB; break;
            case 2: altSeeds[6] = seedB; altSeeds[7] = seedB; break;
            case 3: altSeeds[6] = seedB; altSeeds[7] = seedA; break;
        }
    }

    public void setWangTile5D(int index, WangRawNoise noise){
        final int[] altSeeds = noise.getAltSeeds();
        if(singleEdge){
            Arrays.fill(altSeeds, ~index);
            return;
        }
        int seedA = (index & -1 << 10) ^ -4321;
        int seedB = (index & -1 << 10) ^ -5432;
        int i = index & 3;
        int j = index >>> 2 & 3;
        int k = index >>> 4 & 3;
        int l = index >>> 6 & 3;
        int m = index >>> 8 & 3;
        switch (i){
            case 0: altSeeds[0] = seedA; altSeeds[1] = seedA; break;
            case 1: altSeeds[0] = seedA; altSeeds[1] = seedB; break;
            case 2: altSeeds[0] = seedB; altSeeds[1] = seedB; break;
            case 3: altSeeds[0] = seedB; altSeeds[1] = seedA; break;
        }
        switch (j){
            case 0: altSeeds[2] = seedA; altSeeds[3] = seedA; break;
            case 1: altSeeds[2] = seedA; altSeeds[3] = seedB; break;
            case 2: altSeeds[2] = seedB; altSeeds[3] = seedB; break;
            case 3: altSeeds[2] = seedB; altSeeds[3] = seedA; break;
        }
        switch (k){
            case 0: altSeeds[4] = seedA; altSeeds[5] = seedA; break;
            case 1: altSeeds[4] = seedA; altSeeds[5] = seedB; break;
            case 2: altSeeds[4] = seedB; altSeeds[5] = seedB; break;
            case 3: altSeeds[4] = seedB; altSeeds[5] = seedA; break;
        }
        switch (l){
            case 0: altSeeds[6] = seedA; altSeeds[7] = seedA; break;
            case 1: altSeeds[6] = seedA; altSeeds[7] = seedB; break;
            case 2: altSeeds[6] = seedB; altSeeds[7] = seedB; break;
            case 3: altSeeds[6] = seedB; altSeeds[7] = seedA; break;
        }
        switch (m){
            case 0: altSeeds[8] = seedA; altSeeds[9] = seedA; break;
            case 1: altSeeds[8] = seedA; altSeeds[9] = seedB; break;
            case 2: altSeeds[8] = seedB; altSeeds[9] = seedB; break;
            case 3: altSeeds[8] = seedB; altSeeds[9] = seedA; break;
        }
    }

    public void setWangTile6D(int index, WangRawNoise noise){
        final int[] altSeeds = noise.getAltSeeds();
        if(singleEdge){
            Arrays.fill(altSeeds, ~index);
            return;
        }
        int seedA = (index & -1 << 12) ^ -4321;
        int seedB = (index & -1 << 12) ^ -5432;
        int i = index & 3;
        int j = index >>> 2 & 3;
        int k = index >>> 4 & 3;
        int l = index >>> 6 & 3;
        int m = index >>> 8 & 3;
        int n = index >>> 10 & 3;
        switch (i){
            case 0: altSeeds[0] = seedA; altSeeds[1] = seedA; break;
            case 1: altSeeds[0] = seedA; altSeeds[1] = seedB; break;
            case 2: altSeeds[0] = seedB; altSeeds[1] = seedB; break;
            case 3: altSeeds[0] = seedB; altSeeds[1] = seedA; break;
        }
        switch (j){
            case 0: altSeeds[2] = seedA; altSeeds[3] = seedA; break;
            case 1: altSeeds[2] = seedA; altSeeds[3] = seedB; break;
            case 2: altSeeds[2] = seedB; altSeeds[3] = seedB; break;
            case 3: altSeeds[2] = seedB; altSeeds[3] = seedA; break;
        }
        switch (k){
            case 0: altSeeds[4] = seedA; altSeeds[5] = seedA; break;
            case 1: altSeeds[4] = seedA; altSeeds[5] = seedB; break;
            case 2: altSeeds[4] = seedB; altSeeds[5] = seedB; break;
            case 3: altSeeds[4] = seedB; altSeeds[5] = seedA; break;
        }
        switch (l){
            case 0: altSeeds[6] = seedA; altSeeds[7] = seedA; break;
            case 1: altSeeds[6] = seedA; altSeeds[7] = seedB; break;
            case 2: altSeeds[6] = seedB; altSeeds[7] = seedB; break;
            case 3: altSeeds[6] = seedB; altSeeds[7] = seedA; break;
        }
        switch (m){
            case 0: altSeeds[8] = seedA; altSeeds[9] = seedA; break;
            case 1: altSeeds[8] = seedA; altSeeds[9] = seedB; break;
            case 2: altSeeds[8] = seedB; altSeeds[9] = seedB; break;
            case 3: altSeeds[8] = seedB; altSeeds[9] = seedA; break;
        }
        switch (n){
            case 0: altSeeds[10] = seedA; altSeeds[11] = seedA; break;
            case 1: altSeeds[10] = seedA; altSeeds[11] = seedB; break;
            case 2: altSeeds[10] = seedB; altSeeds[11] = seedB; break;
            case 3: altSeeds[10] = seedB; altSeeds[11] = seedA; break;
        }
    }

}
