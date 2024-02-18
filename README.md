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

`stringSerialize()` and `stringDeserialize()` store random stuff in like a string and totes load it later

`read()` and `write()` do that but like with json and the json stuff does it like auto

u get the picture

# o yeh

theres this other stuff now

u can make a `GapShuffler` out of a randommy thingy and like a lot of stuff u dont wanna repeat

that one will go on like foreeeeeeverrrrrr so u gotta only get a lil bit k

theres this like `WeightedTable` and it gets u ints but like each one has its own weight

the big chonky ones get picked more. like my cat lol

um theres like a new one called `UniqueIdentifier` and it just like makes unique things when u call `next()`

its just like `UUID` but it works on GWT i guess and its like `Json.Serializable` which is p. cool

o and theres `Scramblers` to take a number and make it like wiggly jiggly or smth

# o wate moar!!!

now there like all these like um `RawNoise` types

each 1 has liek a diff flavor i guess?

`PerlinNoise` is all squarey but also liek blobby

`SimplexNoise` is liek triagnles n stuffs

`ValueNoise` is real dum squarezzz so u better use it wit somethin else

`HoneyNoise` mixiz it up wit `SimplexNoise` and `ValueNoise` all hot n hevay

`FoamNoise` is super orgnanic an natrual lookin

`CyclicNoise` uses som liek rotaiton stuf an looks real diff

`SorbetNoise` tastes nice but its also liek `CyclicNoise` and bettr in 2D i think?

then u can give 1 of thoes 2 `ContinuousNoise` n get liek rideged mode n octaevs n stuf

these r all liek `Json.Serializable` so u can liek saev and laod stuf

`LineWobble` is kinda liek those but jus 4 1D noise

# even moar!!!!!!11!!!

`PoissonDiskDiscrete` maeks poitns placed far aways from each ohter, but liek on a liek grid

`PoissonDiskSmooth` maeks poitns placed far aways from each ohter, but liek not on um a grid

`PointHasher` is liek `Scrambler` but 4 a bunch of numburs insted of liek 1

`EncryptedFileHandle` is liek lamebad encarption 4 ur filez but it does liek hide ur images and scriptz

this new stuf is p. cool

# get

The easiest way to use this library is to copy files from the source code.
If you copy [GdxRandom](src/main/java/com/github/tommyettinger/cringe/GdxRandom.java) and
[GwtIncompatible](src/main/java/com/github/tommyettinger/cringe/GwtIncompatible.java),
and one of the three `Random` classes adjacent to it
([RandomAce320](src/main/java/com/github/tommyettinger/cringe/RandomAce320.java),
[RandomDistinct64](src/main/java/com/github/tommyettinger/cringe/RandomDistinct64.java),
or [RandomXMX256](src/main/java/com/github/tommyettinger/cringe/RandomXMX256.java)), you
can use the `Random` class as a drop-in replacement for `java.util.Random` or an
almost-identical replacement for libGDX's `RandomXS128`.

The different GdxRandom implementations differ in various properties. All are high-quality statistically, passing
the PractRand battery of tests to 64TB of tested data. They each have different "periods," meaning the number of random
results that can be produced before the sequence repeats. In practice, a game will probably never have to worry about
exhausting an entire period and actually seeing the sequence repeat. However, far enough along through the actual period
of a generator, it becomes possible for an adversary (typically a player) to notice and exploit patterns. The shortest
I believe this could happen is in `RandomDistinct64`, after over 4 billion random numbers are generated (2 to the 32).
For `RandomXMX256`, the earliest point should be 2 to the 128, which just means it isn't going to happen. `AceRandom320`
has many different possible cycles, with the shortest possible one at least as long as `RandomDistinct64`'s period.

`RandomAce320` is very fast. It uses very few types of instruction, and only performs one math instruction on each state
to get a random result. It also can perform those 5 math instructions using instruction-level parallelism, which the JVM
has been good at exploiting to improve performance since Java 16 or so. The minimum cycle length is 2 to the 64, and the
maximum is over 2 to the 300 (but it's impractical to say exactly). Most random seeds will be in one of the largest
cycles, and even in the minuscule chance that you randomly start in the shortest cycle, that's still almost always going
to be good enough for any app, and effectively always enough for any game. It has five `long`s of state, each of which
can have any value.

`RandomDistinct64` produces each `long` from `nextLong()` exactly once, which makes it good for producing random UUIDs.
It's also rather fast on most JVMs, and might be much faster on GraalVM (version 20 and later). I need to verify some
unusual benchmarks using GraalVM 20, but they appear to show RandomDistinct64 producing over 4 billion long values per
second, which is over twice as fast as RandomAce320. This could be the benchmark code eliminating the tested loop
entirely, so take it with a large grain of salt. It has one `long` of state, which can have any value.

`RandomXMX256` is a fortified version of `Xoshiro256**`, which is present in Java 17, but removes its `**` or StarStar
"scrambler" and replaces it with the much more robust MX3 unary hash, which is also used in our `Scramblers` class.
This generator produces each sequence of four numbers exactly once, except for `0, 0, 0, 0`, which never appears.
RandomXMX256 is slower than the other two generators, but should be much more resilient against statistical quality
issues, probably to the point of being overkill. It has 4 `long`s of state, each of which can have any value unless all
states are 0 (which is not permitted).

There are also a few files that use random number generators in some way:

  - [GapShuffler](src/main/java/com/github/tommyettinger/cringe/GapShuffler.java) is an infinite
    `Iterable` that will never produce the same item twice in a row, as long as more than one item
    was given to it and all items are unique. It has a different way of shuffling its input to do this.
  - [Scramblers](src/main/java/com/github/tommyettinger/cringe/Scramblers.java) is 
    purely static and provides methods to "scramble" `long` input into different formats.
  - [WeightedTable](src/main/java/com/github/tommyettinger/cringe/WeightedTable.java) takes
    an array of weights and produces indices with its `random()` method with frequency adjusted by weight.

You don't need any of these 3 files to use the library, but they may come in handy.

[UniqueIdentifier](src/main/java/com/github/tommyettinger/cringe/UniqueIdentifier.java) is also here, and doesn't use
any GdxRandom. Instead, it produces a sequence of UUID-like values that are guaranteed to be unique unless an impossibly
large amount of identifiers have been produced (2 to the 128 minus 1).

In more recent versions, `cringe` has gotten larger, and now has code for continuous noise, which is fittingly
random-like, and `EncryptedFileHandle`s, which are akin to randomized, but reversible, scrambling of files.
The noise classes are almost always used with a `ContinuousNoise` object wrapping some `RawNoise` implementation.
The ContinuousNoise provides the most useful features, but the RawNoise provides the underlying algorithm. The
noise classes are more inter-dependent; most RawNoise types use `LineWobble` to provide 1D noise, and classes like
`FoamNoise` and `HoneyNoise` call `ValueNoise` methods directly. Meanwhile, `SorbetNoise` inherits from `CyclicNoise`,
and there's lots of other connections. To use the noise, you should probably have a dependency on all of `cringe`.

You can depend on the library as a whole, using it as a normal Gradle or Maven dependency.

`api "com.github.tommyettinger:cringe:0.0.3"`

If you use GWT, then your GWT module needs to depend on:

`implementation "com.github.tommyettinger:cringe:0.0.3:sources"`

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

`<inherits name="com.github.tommyettinger.cringe" />`

You don't need the `inherits` line if you just copy a few classes into your sources.

# license

[apache 2.0](LICENSE)

also thanks to rafa skoberg for like giving me a ton of work to do lololol jk

# yeah

now put on ur robe and wizard hat

its time for number magic
