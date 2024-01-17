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

import java.util.Arrays;

/**
 * This has tools for generating and applying matrix rotations, potentially in higher dimensions than the typical 2 or
 * 3. This has very, very narrow use cases. You can use {@link #randomRotation2D(long)} to quickly generate a single 2D
 * rotation matrix, and to get a 3D matrix, you can either build on that 2D matrix using
 * {@link #randomRotation3D(long, float[])} or use {@link #randomRotation3D(long)} to make a completely new one. The
 * same follows for making a 4D rotation using a 3D one, and so on. You can apply a rotation to a vector with
 * {@link #rotate(float[], float[], float[])}, and there's also a method to multiply square matrices in
 * {@link #matrixMultiply(float[], float[], float[], int)}.
 * <br>
 * My head hurts. Thanks to spenc on the libGDX Discord for carefully guiding me through this code, among several other
 * people who helped a lot.
 */
public final class RotationSupport {
    private static final float ROOT2 = 1.4142135623730950488f;

    /**
     * No instantiation.
     */
    private RotationSupport() {
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), and an output vector to write to (as a 1D float array), and does the math to
     * rotate {@code input} using {@code rotation}, and add the results into {@code output}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m} or greater
     * @param output the output vector of length {@code m}
     */
    public static void rotate(float[] input, float[] rotation, float[] output) {
        int m = 0;
        for (int r = 0; r < input.length; r++) {
            for (int c = 0; m < rotation.length && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), an output vector to write to (as a 1D float array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector of length {@code n}
     * @param rotation a rotation matrix of length {@code n*m}
     * @param output the output vector of length {@code m} or greater; only {@code rotation.length / input.length} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     */
    public static void rotate(float[] input, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / input.length;
        for (int r = 0; r < input.length; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * A "raw" rotation method that takes a rotation matrix (as a row-major 1D float array), an input vector to rotate
     * (as a 1D float array), an output vector to write to (as a 1D float array), and an offset into the output vector
     * to start writing there, and does the math to rotate {@code input} using {@code rotation}, and add the results
     * into {@code output} starting at {@code offsetOut}. This does not erase output
     * before writing to it, so it can be called more than once to sum multiple rotations if so desired. The length of
     * output can be arbitrarily large, so this is complete when it has completely processed rotation. That means this
     * affects {@code rotation.length / input.length} items in output. Typically, if input has length {@code n} and
     * output should receive {@code m} changes, rotation has length {@code n*m}. This does no validation
     * on {@code rotation}, hence why it is "raw" (also because it takes its inputs as unadorned 1D arrays).
     *
     * @param input an input vector that has length at least equal to {@code offsetIn + sizeIn}
     * @param offsetIn the index in {@code input} to start reading
     * @param sizeIn how many elements to read from {@code input}
     * @param rotation a rotation matrix of length {@code sizeIn * m}, where {@code m} is the length of an output vector
     * @param output the output vector of length {@code m} or greater; only {@code rotation.length / sizeIn} items will be written to
     * @param offsetOut the index in {@code output} to start writing the rotated output
     */
    public static void rotate(float[] input, int offsetIn, int sizeIn, float[] rotation, float[] output, int offsetOut) {
        int m = 0;
        final int outEnd = offsetOut + rotation.length / sizeIn, inEnd = offsetIn + sizeIn;
        for (int r = offsetIn; r < inEnd; r++) {
            for (int c = offsetOut; m < rotation.length && c < outEnd && c < output.length; c++) {
                output[c] += rotation[m++] * input[r];
            }
        }
    }

    /**
     * Multiplies two square matrices with side length {@code side}, and stores the result in {@code out}. The inputs
     * {@code lf} and {@code rt} are 1D float arrays treated as row-major matrices.
     * @param lf the left input matrix, as row-major
     * @param rt the right input matrix, as row-major
     * @param out will be modified; this is where the output is summed into, and it is not cleared beforehand
     * @param side side length of each input matrix and the output matrix
     */
    public static void matrixMultiply(float[] lf, float[] rt, float[] out, int side) {
        int o = 0;
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                for (int i = 0; i < side; i++) {
                    out[o] += lf[r * side + i] * rt[side * i + c];
                }
                ++o;
            }
        }
    }

    // Section using a long seed and randomize()

    /**
     * This is just part of a larger rotation generator; it takes a target size (the side length of the matrix this will
     * return), another matrix {@code small} (with a side length 1 less than {@code targetSize}), and a random number
     * seed, and uses the seed and some matrix operations to generate a random rotation based on {@code small}. This
     * allocates a new {@code targetSize * targetSize}-element float array on every call and returns it. It also
     * allocates some more temporary float arrays to work with.
     * <br>
     * This is not meant for usage outside this class, but if you are copying or modifying parts of the code in here,
     * then you will probably need at least one of the rotateStep() methods.
     * <br>
     * See <a href="https://math.stackexchange.com/a/442489">Stack Exchange's links here</a>, and Graphics Gems III
     * (specifically, the part about fast random rotation matrices, not the part about the subgroup algorithm).
     *
     * @param seed random number generator seed; may be a long or an int
     * @param small a smaller square matrix than the result should be; must have side length {@code targetSize - 1}
     * @param targetSize the side length of the square matrix to be returned
     * @return a 1D float array that can be treated as a rotation matrix for inputs of size {@code targetSize}
     */
    public static float[] rotateStep(long seed, float[] small, int targetSize) {
        final int smallSize = targetSize - 1, squareSize = targetSize * targetSize;
        // might be able to get rid of these allocations by holding onto some space...
        float[] gauss = new float[targetSize], house = new float[squareSize], large = new float[squareSize],
                out = new float[squareSize];
        for (int i = 0; i < smallSize; i++) {
            // copy the small matrix into the bottom right corner of the large matrix
            System.arraycopy(small, i * smallSize, large, i * targetSize + targetSize + 1, smallSize);
        }
        large[0] = 1;
        long sd = Scramblers.scramble(seed + squareSize);
        float sum = 0f, t;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = t = (float) Scramblers.scrambleGaussian(sd += 0x9E3779B97F4A7C15L);
            sum += t * t;
        }
        final float inv = 1f / (float) Math.sqrt(sum);
        sum = 0f;
        t = 1f;
        for (int i = 0; i < targetSize; i++) {
            t -= gauss[i] *= inv;
            sum += t * t;
            t = 0f;
        }
        sum = ROOT2 / (float) Math.sqrt(sum); // reused as what the subgroup paper calls c
        t = 1f;
        for (int i = 0; i < targetSize; i++) {
            gauss[i] = (t - gauss[i]) * sum;
            t = 0f;
        }
        for (int row = 0, h = 0; row < targetSize; row++) {
            for (int col = 0; col < targetSize; col++, h++) {
                house[h] = gauss[row] * gauss[col];
            }
        }
        for (int i = 0; i < targetSize; i++) {
            house[targetSize * i + i]--;
        }
        matrixMultiply(house, large, out, targetSize);
        return out;
    }

    /**
     * Creates a new 1D float array that can be used as a 2D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Scrambles the given seed with {@link Scramblers#scrambleFloat(long)},
     * then gets an angle using {@link MathUtils}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 4-element float array, meant as effectively a 2D rotation matrix
     */
    public static float[] randomRotation2D(long seed) {
        final float theta = Scramblers.scrambleFloat(seed) * 6.2831855f;
        final float s = MathUtils.sin(theta);
        final float c = MathUtils.cos(theta);
        return new float[]{c, s, -s, c};
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link MathUtils} and Gaussian floats using {@link Scramblers#scrambleGaussian(long)}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 9-element float array, meant as effectively a 3D rotation matrix
     */
    public static float[] randomRotation3D(long seed) {
        return rotateStep(seed, randomRotation2D(seed - 3), 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 3D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Scramblers#scrambleGaussian(long)}, and uses an existing 2D rotation matrix to avoid redoing
     * any generation work already done for 2D. There will probably be some correlation between the appearance of the 2D
     * rotation this will build upon and the 3D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation3D(long seed, float[] rotation2D) {
        return rotateStep(seed, rotation2D, 3);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link MathUtils} and Gaussian floats using {@link Scramblers#scrambleGaussian(long)}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed) {
        return rotateStep(seed, randomRotation3D(seed - 4), 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 4D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Scramblers#scrambleGaussian(long)}, and uses an existing 3D rotation matrix to avoid redoing
     * any generation work already done for 3D. There will probably be some correlation between the appearance of the 3D
     * rotation this will build upon and the 4D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 16-element float array, meant as effectively a 4D rotation matrix
     */
    public static float[] randomRotation4D(long seed, float[] rotation3D) {
        return rotateStep(seed, rotation3D, 4);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link MathUtils} and Gaussian floats using {@link Scramblers#scrambleGaussian(long)}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(long seed) {
        return rotateStep(seed, randomRotation4D(seed - 5), 5);
    }

    /**
     * Creates a new 1D float array that can be used as a 5D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Scramblers#scrambleGaussian(long)}, and uses an existing 4D rotation matrix to avoid redoing
     * any generation work already done for 4D. There will probably be some correlation between the appearance of the 4D
     * rotation this will build upon and the 5D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 25-element float array, meant as effectively a 5D rotation matrix
     */
    public static float[] randomRotation5D(long seed, float[] rotation4D) {
        return rotateStep(seed, rotation4D, 5);
    }
    
    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link MathUtils} and Gaussian floats using {@link Scramblers#scrambleGaussian(long)}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(long seed) {
        return rotateStep(seed, randomRotation5D(seed - 6), 6);
    }

    /**
     * Creates a new 1D float array that can be used as a 6D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Scramblers#scrambleGaussian(long)}, and uses an existing 5D rotation matrix to avoid redoing
     * any generation work already done for 5D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 6D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 36-element float array, meant as effectively a 6D rotation matrix
     */
    public static float[] randomRotation6D(long seed, float[] rotation5D) {
        return rotateStep(seed, rotation5D, 6);
    }
    
    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get an angle using
     * {@link MathUtils} and Gaussian floats using {@link Scramblers#scrambleGaussian(long)}.
     * @param seed any long; will be scrambled
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(long seed) {
        return rotateStep(seed, randomRotation6D(seed - 7), 7);
    }

    /**
     * Creates a new 1D float array that can be used as a 7D rotation matrix by
     * {@link #rotate(float[], float[], float[])}. Uses the given long seed to get Gaussian floats using
     * {@link Scramblers#scrambleGaussian(long)}, and uses an existing 6D rotation matrix to avoid redoing
     * any generation work already done for 6D. There will probably be some correlation between the appearance of the 5D
     * rotation this will build upon and the 7D rotation this produces, but other factors may make this irrelevant when
     * used for noise.
     * @param seed any long; will be scrambled
     * @param rotation6D an existing 6D rotation matrix stored in a 1D float array; often produced by {@link #randomRotation6D(long)}
     * @return a newly-allocated 49-element float array, meant as effectively a 7D rotation matrix
     */
    public static float[] randomRotation7D(long seed, float[] rotation6D) {
        return rotateStep(seed, rotation6D, 7);
    }
}
