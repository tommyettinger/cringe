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

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/**
 * A form of probability table, this stores indices into some external Array or other ordered sequence and weights that
 * determine how much to favor producing a given index. Uses positive floats for weights.
 * <br>
 * Uses <a href="http://www.keithschwarz.com/darts-dice-coins/">Vose's Alias Method</a>, and is based fairly-closely on
 * the code given by Keith Schwarz at that link. Vose's Alias Method is remarkably fast; it takes O(1) time to
 * get a random index, and takes O(n) time to construct a WeightedTable instance.
 */
public class WeightedTable implements Json.Serializable, Externalizable {
    protected int[] mixed;
    protected GdxRandom random;

    /**
     * Constructs a useless WeightedTable that always returns the index 0.
     */
    public WeightedTable()
    {
        this(null, 1);
    }

    /**
     * Constructs a WeightedTable with the given GdxRandom and array or varargs of weights for each index. The weights
     * can be any positive non-zero floats, but should usually not be so large or small that
     * precision loss is risked. Each weight will be used to determine the likelihood of that weight's index being
     * returned by {@link #random()} or {@link #random(long)}.
     * @param random if null, this will use an unseeded {@link RandomAce320}; otherwise this will use random unchanged
     * @param probabilities an array or varargs of positive floats representing the weights for their own indices
     */
    public WeightedTable(GdxRandom random, float... probabilities) {
        int size = probabilities.length;
        /* Begin by doing basic structural checks on the inputs. */
        if (size == 0)
            throw new IllegalArgumentException("Array 'probabilities' given to WeightedTable must be nonempty.");
        this.random = random == null ? new RandomAce320() : random;

        mixed = new int[size<<1];

        float sum = 0.0f;

        /* Make a copy of the probabilities array, since we will be making
         * changes to it.
         */
        float[] probs = new float[size];
        for (int i = 0; i < size; ++i) {
            if(probabilities[i] <= 0) continue;
            sum += (probs[i] = probabilities[i]);
        }
        if(sum <= 0)
            throw new IllegalArgumentException("At least one probability must be positive");
        final float average = sum / size, invAverage = 1.0f / average;

        /* Create two stacks to act as worklists as we populate the tables. */
        IntArray small = new IntArray(size);
        IntArray large = new IntArray(size);

        /* Populate the stacks with the input probabilities. */
        for (int i = 0; i < size; ++i) {
            /* If the probability is below the average probability, then we add
             * it to the small list; otherwise we add it to the large list.
             */
            if (probs[i] >= average)
                large.add(i);
            else
                small.add(i);
        }

        /* As a note: in the mathematical specification of the algorithm, we
         * will always exhaust the small list before the big list.  However,
         * due to floating point inaccuracies, this is not necessarily true.
         * Consequently, this inner loop (which tries to pair small and large
         * elements) will have to check that both lists aren't empty.
         */
        while (!small.isEmpty() && !large.isEmpty()) {
            /* Get the index of the small and the large probabilities. */
            int less = small.pop(), less2 = less << 1;
            int more = large.pop();

            /* These probabilities have not yet been scaled up to be such that
             * sum/n is given weight 1.0.  We do this here instead.
             */
            mixed[less2] = (int)(0x7FFFFFFF * (probs[less] * invAverage));
            mixed[less2|1] = more;

            probs[more] += probs[less] - average;

            if (probs[more] >= average)
                large.add(more);
            else
                small.add(more);
        }

        while (!small.isEmpty())
            mixed[small.pop()<<1] = 0x7FFFFFFF;
        while (!large.isEmpty())
            mixed[large.pop()<<1] = 0x7FFFFFFF;
    }

    private WeightedTable(GdxRandom random, int[] mixed, boolean ignored)
    {
        this.random = random.copy();
        this.mixed = mixed;
    }

    /**
     * Copy constructor; avoids sharing any state between this and the original.
     * @param other another WeightedTable to copy; no state will be shared
     */
    public WeightedTable(WeightedTable other){
        this(other.random, Arrays.copyOf(other.mixed, other.mixed.length), true);
    }

    /**
     * Copies this WeightedTable; avoids sharing any state between this and the copy.
     * @return an exact copy of this WeightedTable
     */
    public WeightedTable copy() {
        return new WeightedTable(this);
    }
    /**
     * Gets an index of one of the weights in this WeightedTable, with the choice determined deterministically by the
     * given long, but higher weights will be returned by more possible inputs than lower weights. The state parameter
     * can be from a random source, but this will randomize it again anyway, so it is also fine to just give sequential
     * longs. The important thing is that each state input this is given will produce the same result for this
     * WeightedTable every time, so you should give different state values when you want random-seeming results. You may
     * want to call this like {@code weightedTable.random(++state)}, where state is a long, to ensure the inputs change.
     * This will always return an int between 0 (inclusive) and {@link #size} (exclusive).
     * <br>
     * Internally, this uses a unary hash (a function that converts one number to a random-seeming but deterministic
     * other number) to generate two ints, one used for probability and treated as a 31-bit integer and the other used
     * to determine the chosen column, which is bounded to an arbitrary positive int. It does this with just one
     * randomized 64-bit value, allowing the state parameter to be just one long. Using this method does not affect the
     * internal random number generator.
     * @param state a long that should be different every time; consider calling with {@code ++state}
     * @return a random-seeming index from 0 to {@link #size} - 1, determined by weights and the given state
     */
    public int random(long state)
    {
        // This uses the MX3 algorithm to generate a random long given sequential states
        state = Scramblers.scramble(state);
        // get a random int (using half the bits of our previously-calculated state) that is less than size
        int column = (int)((mixed.length * (state & 0xFFFFFFFFL)) >>> 33);
        // use the other half of the bits of state to get a 31-bit int, compare to probability and choose either the
        // current column or the alias for that column based on that probability
        return ((state >>> 33) <= mixed[column & -2]) ? column : mixed[column | 1];
    }
    /**
     * Gets an index of one of the weights in this WeightedTable, with the choice determined by the random number
     * generator this stores internally, but higher weights will be returned more frequently than lower weights.
     * This will return an int between 0 (inclusive) and {@link #size} (exclusive).
     * @return a random index from 0 to {@link #size} - 1, determined by weights and the internal GdxRandom
     */
    public int random()
    {
        final long state = random.nextLong();
        // get a random int (using half the bits of our previously-calculated state) that is less than size
        int column = (int)((mixed.length * (state & 0xFFFFFFFFL)) >>> 33);
        // use the other half of the bits of state to get a 31-bit int, compare to probability and choose either the
        // current column or the alias for that column based on that probability
        return ((state >>> 33) <= mixed[column & -2]) ? column : mixed[column | 1];
    }

    public int size() {
        return mixed.length >>> 1;
    }

    public GdxRandom getRandom() {
        return random;
    }

    /**
     * This will only use the given {@link GdxRandom} if it isn't null.
     * @param random a non-null GdxRandom
     */
    public void setRandom(GdxRandom random) {
        if(random != null)
            this.random = random;
    }

    public String stringSerialize()
    {
        StringBuilder sb = new StringBuilder(random.stringSerialize()).append(mixed.length);
        if(mixed.length == 0) return sb.toString();

        for (int i = 0; i < mixed.length; i++) {
            sb.append(',').append(mixed[i]);
        }
        return sb.toString();
    }

    public WeightedTable stringDeserialize(String data)
    {
        if(data == null || data.isEmpty())
            return null;
        int pos = data.lastIndexOf('`');
        random.stringDeserialize(data);
        int count = MathSupport.intFromDec(data, pos+1, pos = data.indexOf(',', pos+1));
        mixed = new int[count];
        for (int i = 0; i < count; i++) {
            int next = data.indexOf(',', pos+1);
            if(next == -1) next = data.length();
            mixed[i] = MathSupport.intFromDec(data, pos+1, pos = next);
        }
        return this;
    }

    /**
     * Note that this does not check the {@link #getRandom() random} field for equality, and only checks the tables of
     * weights and their indices. This effectively is the same as comparing a table in a rulebook with another table
     * from another edition of the rulebook -- the next results selected from both tables at random could be different
     * or the same, but what matters is that the set of probabilities for the same indices is the same.
     * @param o some other Object, probably another WeightedTable
     * @return true if {@code o} is a WeightedTable and its weights are identical to the weights here
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeightedTable that = (WeightedTable) o;
        return Arrays.equals(mixed, that.mixed);
    }

    /**
     * Only hashes the table of weights and their indices; does not consider the {@link #getRandom() random} field.
     * For the rationale here, see {@link #equals(Object)}.
     * @return a hash code of the weight table only
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(mixed);
    }

    /**
     * Gets a String representation of this WeightedTable. Because the way probabilities are stored is very
     * challenging to read for humans, this returns six sample results, made by calling {@link #random(long)}
     * on (in this order): -101, -102, -103, -201, -202, and -203 . The inputs were chosen in an attempt to
     * avoid common patterns such as "1, 2, 3, 4, 5" since these could easily appear in user data and be
     * confused with return values used by the program.
     * @return a String representation of this WeightedTable
     */
    @Override
    public String toString() {
        return "WeightedTable{" +
                "random=" + random.getTag() + ", samples=(" +
                random(-101L) + ", " + random(-102L) + ", " + random(-103L) + ", " +
                random(-201L) + ", " + random(-202L) + ", " + random(-203L) +
                ")}";
    }

    @Override
    public void write(Json json) {
        json.writeObjectStart("wt");
        json.writeValue("rng", random, null);
        json.writeValue("items", mixed, int[].class);
        json.writeObjectEnd();
    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        jsonData = jsonData.get("wt");
        random = json.readValue("rng", GdxRandom.class, jsonData);
        mixed = jsonData.get("items").asIntArray();
    }

    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @param out the stream to write the object to
     * @throws IOException Includes any I/O exceptions that may occur
     * @serialData Overriding methods should use this tag to describe
     * the data layout of this Externalizable object.
     * List the sequence of element types and, if possible,
     * relate the element to a public/protected field and/or
     * method of this Externalizable class.
     */
    @GwtIncompatible
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(mixed);
        out.writeObject(random);
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        mixed = (int[]) in.readObject();
        random = (GdxRandom) in.readObject();
    }
}
