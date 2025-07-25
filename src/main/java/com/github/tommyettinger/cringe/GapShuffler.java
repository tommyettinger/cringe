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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;

/**
 * Meant to take a fixed-size set of items and produce a shuffled stream of them such that an element is never chosen in
 * quick succession; that is, there should always be a gap between the same item's occurrences. This is an Iterable of
 * T, not an Array, because it can iterate without stopping, infinitely, unless you break out of a foreach loop that
 * iterates through one of these, or call the iterator's next() method only a limited number of times. Arrays have
 * a size that can be checked, but Iterables can be infinite (and in this case, this one is).
 * @param <T> the type of items to iterate over; ideally, the items are unique
 */
public class GapShuffler<T> implements Iterator<T>, Iterable<T>, Json.Serializable, Externalizable {
    public GdxRandom random;
    protected Array<T> elements;
    protected int index;
    public GapShuffler() {
        random = new RandomAce320();
        elements = new Array<>();
        index = 0;
    }

    public GapShuffler(T single)
    {
        random = new RandomAce320();
        elements = new Array<>(1);
        elements.add(single);
        index = 0;
    }
    /**
     * Constructor that takes any Array of T, shuffles it with an unseeded RandomAce320, and can then iterate
     * infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a single
     * element should always have a large amount of "gap" in order between one appearance and the next. It helps to keep
     * the appearance of a gap if every item in elements is unique, but that is not necessary and does not affect how
     * this works.
     * @param elements a Array of T that will not be modified
     */
    public GapShuffler(Array<T> elements)
    {
        this(elements, new RandomAce320());

    }

    /**
     * Constructor that takes any Array of T, shuffles it with the given GdxRandom, and can then
     * iterate infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a
     * single element should always have a large amount of "gap" in order between one appearance and the next. It helps
     * to keep the appearance of a gap if every item in items is unique, but that is not necessary and does not affect
     * how this works. The random parameter is copied so externally using it won't change the order this produces its
     * values; the random field is used whenever the iterator needs to re-shuffle the internal ordering of items.
     * @param items a Array of T that will not be modified
     * @param random a GdxRandom; will be copied and not used directly
     */
    public GapShuffler(Array<T> items, GdxRandom random)
    {
        this(items, random, false);
    }

    /**
     * Constructor that takes any Array of T, shuffles it with the given GdxRandom, and can then
     * iterate infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a
     * single element should always have a large amount of "gap" in order between one appearance and the next. It helps
     * to keep the appearance of a gap if every item in items is unique, but that is not necessary and does not affect
     * how this works. The random parameter will be copied if {@code shareRandom} is true, otherwise the reference will be
     * shared (which could make the results of this GapShuffler depend on outside code, though it will always maintain a
     * gap between identical elements if the elements are unique).
     * @param items a Array of T that will not be modified
     * @param random aa GdxRandom; will be copied and not used directly
     * @param shareRandom if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public GapShuffler(Array<T> items, GdxRandom random, boolean shareRandom)
    {
        this.random = shareRandom ? random : random.copy();
        elements = new Array<>(items);
        this.random.shuffle(elements);
        index = 0;
    }

    /**
     * Constructor that takes any Array of T, shuffles it with the given GdxRandom, and can then
     * iterate infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a
     * single element should always have a large amount of "gap" in order between one appearance and the next. It helps
     * to keep the appearance of a gap if every item in items is unique, but that is not necessary and does not affect
     * how this works. The random parameter will be copied if {@code shareRandom} is true, otherwise the reference will be
     * shared (which could make the results of this GapShuffler depend on outside code, though it will always maintain a
     * gap between identical elements if the elements are unique). This constructor takes an {@code index} to allow
     * duplicating an existing GapShuffler given the parts that are externally available (using {@link #random},
     * {@link #fillInto(Array)}, and {@link #getIndex()}). To actually complete a (shallow) copy, {@code shareRandom}
     * must be false; to do a deep copy, you need to deep-copy {@code items}.
     * @param items a Array of T that will not be modified
     * @param random a GdxRandom; will be copied and not used directly
     * @param index an index in the iteration, as obtained by {@link #getIndex()}
     * @param shareRandom if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public GapShuffler(Array<T> items, GdxRandom random, int index, boolean shareRandom)
    {
        this.random = shareRandom ? random : random.copy();
        elements = new Array<>(items);
        this.random.shuffle(elements);
        this.index = (index & 0x7FFFFFFF) % elements.size;
    }

    /**
     * Constructor that takes any Array of T, optionally shuffles it with the given GdxRandom (only if
     * {@code initialShuffle} is true), and can then
     * iterate infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a
     * single element should always have a large amount of "gap" in order between one appearance and the next. It helps
     * to keep the appearance of a gap if every item in items is unique, but that is not necessary and does not affect
     * how this works. The random parameter will be copied if {@code shareRandom} is true, otherwise the reference will be
     * shared (which could make the results of this GapShuffler depend on outside code, though it will always maintain a
     * gap between identical elements if the elements are unique). This constructor takes an {@code index} to allow
     * duplicating an existing GapShuffler given the parts that are externally available (using {@link #random},
     * {@link #fillInto(Array)}, and {@link #getIndex()}). To actually complete a (shallow) copy, {@code shareRandom}
     * must be false; to do a deep copy, you need to deep-copy {@code items}. You can use {@code initialShuffle} to
     * copy items from another GapShuffler; most of the constructors here act like this does when {@code initialShuffle}
     * is true, but the copy constructor {@link #GapShuffler(GapShuffler)} does not (it uses this method).
     * @param items a Array of T that will not be modified
     * @param random a GdxRandom; will be copied and not used directly
     * @param index an index in the iteration, as obtained by {@link #getIndex()}
     * @param shareRandom if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     * @param initialShuffle if true, this will shuffle its copy of {@code items}; if false, then items will be kept in the same order
     */
    public GapShuffler(Array<T> items, GdxRandom random, int index, boolean shareRandom, boolean initialShuffle) {
        this.random = shareRandom ? random : random.copy();
        elements = new Array<>(items);
        if(initialShuffle)
            this.random.shuffle(elements);
        this.index = (index & 0x7FFFFFFF) % elements.size;
    }

    /**
     * Copy constructor; duplicates {@code other}, you know the deal. This does not share its {@link #random} field with
     * {@code other}.
     * @param other another GapShuffler; its item type will also be used here
     */
    public GapShuffler(GapShuffler<T> other){
        this(other.elements, other.random, other.index, false, false);
    }

    /**
     * Constructor that takes any Array of T, shuffles it with an unseeded RandomAce320, and can then iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements an array of T that will not be modified
     */
    public GapShuffler(T[] elements)
    {
        this(elements, new RandomAce320());
    }

    /**
     * Constructor that takes any Array of T, shuffles it with a RandomAce320 seeded with the given long, and can then
     * iterate infinitely
     * through mostly-random shuffles of the given collection. These shuffles are spaced so that a single element should
     * always have a large amount of "gap" in order between one appearance and the next. It helps to keep the appearance
     * of a gap if every item in elements is unique, but that is not necessary and does not affect how this works.
     * @param elements an array of T that will not be modified
     */
    public GapShuffler(T[] elements, long seed)
    {
        this(elements, new RandomAce320(seed));
    }

    /**
     * Constructor that takes any Array of T, shuffles it with the given GdxRandom (such as a TricycleRandom
     * or WhiskerRandom), and can then iterate infinitely through mostly-random shuffles of the given collection.
     * These shuffles are spaced
     * so that a single element should always have a large amount of "gap" in order between one appearance and the next.
     * It helps to keep the appearance of a gap if every item in items is unique, but that is not necessary and does not
     * affect how this works. The random parameter is copied so externally using it won't change the order this produces
     * its values; the random field is used whenever the iterator needs to re-shuffle the internal ordering of items.
     * @param items an array of T that will not be modified
     * @param random a GdxRandom; will be copied and not used directly
     */
    public GapShuffler(T[] items, GdxRandom random)
    {
        this.random = random.copy();
        elements = Array.with(items);
        this.random.shuffle(elements);
        index = 0;
    }

    /**
     * Constructor that takes any Array of T, shuffles it with the given GdxRandom, and can then
     * iterate infinitely through mostly-random shuffles of the given collection. These shuffles are spaced so that a
     * single element should always have at least one "gap" element between one appearance and the next. It helps to
     * keep the appearance of a gap if every item in items is unique, but that is not necessary and does not affect how
     * this works. The random parameter will be copied if {@code shareRandom} is false, otherwise the reference will be
     * shared (which could make the results of this GapShuffler depend on outside code, though it will always maintain a
     * gap between identical elements if the elements are unique).
     * @param items an array of T that will not be modified
     * @param random a GdxRandom; will be copied and not used directly
     * @param shareRandom if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public GapShuffler(T[] items, GdxRandom random, boolean shareRandom)
    {
        this.random = shareRandom ? random : random.copy();
        elements = Array.with(items);
        this.random.shuffle(elements);
        index = 0;
    }

    /**
     * Gets the next element of the infinite sequence of T this shuffles through. This class can be used as an
     * Iterator or Iterable of type T.
     * @return the next element in the infinite sequence
     */
    public T next() {
        int size = elements.size;
        if(size == 1)
        {
            return elements.get(0);
        }
        if(index >= size)
        {
            final int n = size - 1;
            for (int i = n; i > 1; i--) {
                elements.swap(random.nextInt(i), i - 1);
            }
            elements.swap(1 + random.nextInt(n), n);
            index = 0;
        }
        return elements.get(index++);
    }
    /**
     * Returns {@code true} if the iteration has more elements.
     * This is always the case for GapShuffler.
     *
     * @return {@code true} always
     */
    @Override
    public boolean hasNext() {
        return true;
    }
    /**
     * Not supported.
     *
     * @throws UnsupportedOperationException always throws this exception
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() is not supported");
    }

    /**
     * Returns an <b>infinite</b> iterator over elements of type {@code T}; the returned iterator is this object.
     * You should be prepared to break out of any loops that use this once you've gotten enough elements!
     * The remove() method is not supported by this iterator and hasNext() will always return true.
     *
     * @return an infinite Iterator over elements of type T.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    public GdxRandom getRandom() {
        return random;
    }


    /**
     * Sets the GdxRandom this uses to shuffle the order of elements, always copying the given GdxRandom
     * before using it. Always reshuffles the order, which may eliminate a gap that should have been present, so treat
     * the sequence before and after like separate GapShuffler objects.
     * @param random a GdxRandom; always copied
     */
    public void setRandom(GdxRandom random) {
        setRandom(random, false);
    }

    /**
     * Sets the GdxRandom this uses to shuffle the order of elements, optionally sharing a reference between
     * outside code and the internal GdxRandom (when {@code shareRandom} is true). Always reshuffles the order, which
     * may eliminate a gap that should have been present, so treat the sequence before and after like separate
     * GapShuffler objects.
     * @param random a GdxRandom; optionally copied
     * @param shareRandom if false, {@code random} will be copied and no reference will be kept; if true, {@code random} will be shared with the outside code
     */
    public void setRandom(GdxRandom random, boolean shareRandom) {
        this.random = shareRandom ? random : random.copy();
        this.random.shuffle(elements);
    }

    /**
     * The internal items used here are protected, but you can still use this method to put a shallow copy of them into
     * some other Array. If the type {@code T} is mutable, changes to individual items will be reflected in this
     * GapShuffler, so be careful in that case (if T is immutable, like if it is String, then there's nothing to need to
     * be careful about). This copies each item in the GapShuffler's sequence once, in no particular order, but it may
     * give a prediction of what items this will return in the future (or has already returned).
     * @param coll a Array that will have each of the possible items this can produce added into it, in no particular order
     */
    public void fillInto(Array<T> coll) {
        coll.addAll(elements);
    }

    /**
     * Allows getting (but not setting) the internal index this uses as it iterates through shuffled versions of the
     * same collection indefinitely. This is mostly meant for serialization code to be able to replicate the state of
     * this GapShuffler. This is mostly useful with {@link #fillInto(Array)} and some kind of insertion-ordered
     * Array (like {@link Array}, which lets the index match an item in that Array.
     * @return an index used internally to determine how far through a particular shuffle the iteration has gotten
     */
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "GapShuffler{" +
                "elements=" + elements +
                ", random=" + random +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GapShuffler<?> that = (GapShuffler<?>) o;

        if (index != that.index) return false;
        if (!random.equals(that.random)) return false;
        return elements.equals(that.elements);
    }

    @Override
    public void write(Json json) {
        json.writeObjectStart("gs");
        json.writeValue("items", elements, Array.class, null);
        json.writeValue("rng", random, null);
        json.writeValue("idx", getIndex());
        json.writeObjectEnd();

    }

    @Override
    public void read(Json json, JsonValue jsonData) {
        jsonData = jsonData.get("gs");
        elements.clear();
        //noinspection unchecked
        elements.addAll(json.readValue("items", Array.class, jsonData));
        random = json.readValue("rng", GdxRandom.class, jsonData);
        index = jsonData.get("idx").asInt();
    }

    /**
     * Requires the deserializer to be able to read in an {@link Array} of {@code T} items, as well as a
     * {@link GdxRandom}. These typically need to be registered before a GapShuffler can be written.
     * If you are using Apache Fory, the concrete subclass of GdxRandom is required to be registered (typically this is
     * {@link RandomAce320}), but GdxRandom itself does not need to be registered.
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
        out.writeObject(elements);
        out.writeObject(random);
        out.writeInt(index);
    }

    /**
     * Requires the deserializer to be able to read in an {@link Array} of {@code T} items, as well as a
     * {@link GdxRandom}. These typically need to be registered before a GapShuffler can be read in.
     * If you are using Apache Fory, the concrete subclass of GdxRandom is required to be registered (typically this is
     * {@link RandomAce320}), but GdxRandom itself does not need to be registered.
     *
     * @param in the stream to read data from in order to restore the object
     * @throws IOException            if I/O errors occur
     * @throws ClassNotFoundException If the class for an object being
     *                                restored cannot be found.
     */
    @GwtIncompatible
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        //noinspection unchecked
        elements = (Array<T>) in.readObject();
        random = (GdxRandom) in.readObject();
        index = in.readInt();
    }
}
