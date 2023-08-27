# cringe

like omg so random

but like

with gdx

theres like soooooo many methods

its p. cool

plus if u use this

u dont need to understand how this works

```java
    public float nextExclusiveFloat () {
        final long bits = nextLong();
        return NumberUtils.intBitsToFloat(126 - Long.numberOfTrailingZeros(bits) << 23 | (int)(bits >>> 41));
    }
```

it can just be some cool magic code or smth

u use it when u want a random float but not 0 or 1 lol

theres other cool stuff 2

`shuffle()` and `randomElement()` these work with libgdx types

all the stuff from mathutils is here 2 but like instance methods on a gdxrandom

`stringSerialize()` and `stringDeserialize()` store random stuff in a string and like load it later

`read()` and `write()` do that but like with json and the json stuff does it like auto

u get the picture

# get

The easiest way to use this library is to copy files from the source code.
If you copy [GdxRandom](src/main/java/com/github/tommyettinger/cringe/GdxRandom.java)
and one of the three `Random` classes adjacent to it
([RandomAce320](src/main/java/com/github/tommyettinger/cringe/RandomAce320.java),
[RandomDistinct64](src/main/java/com/github/tommyettinger/cringe/RandomDistinct64.java),
or [RandomXMX256](src/main/java/com/github/tommyettinger/cringe/RandomXMX256.java)), you
can use the `Random` class as a drop-in replacement for `java.util.Random` or an
almost-identical replacement for libGDX's `RandomXS128`. There are also a few files that
use random number generators in some way:

  - [GapShuffler](src/main/java/com/github/tommyettinger/cringe/GapShuffler.java) is an infinite
    `Iterable` that will never produce the same item twice in a row, as long as more than one item
    was given to it and all items are unique. It has a different way of shuffling its input to do this.
  - [Scramblers](src/main/java/com/github/tommyettinger/cringe/Scramblers.java) is 
    purely static and provides methods to "scramble" `long` input into different formats.
  - [WeightedTable](src/main/java/com/github/tommyettinger/cringe/WeightedTable.java) takes
    an array of weights and produces indices with its `random()` method with frequency adjusted by weight.

You don't need any of these 3 files to use the library, but they may come in handy.

Alternatively, you can depend on the library as a whole, using it as a normal Gradle or Maven
dependency.

`implementation "com.github.tommyettinger:cringe:0.0.1"`

If you use GWT, then your GWT module needs to depend on:

`implementation "com.github.tommyettinger:cringe:0.0.1:sources"`

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

`<inherits name="com.github.tommyettinger.cringe" />`

You don't need the `inherits` line if you just copy a few classes into your sources.

# license

[apache 2.0](LICENSE)

# yeah

now put on ur robe and wizard hat

its time for number magic
