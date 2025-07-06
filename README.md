# cringe

like omg so random

but like

with gdx

theres like soooooo many methods

its p. cool

plus if u use this

u dont need 2 understand how this works

```java
    public float nextExclusiveFloat () {
        final long bits = nextLong();
        return NumberUtils.intBitsToFloat(126 - Compatibility.countTrailingZeros(bits) << 23 | (int)(bits >>> 41));
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

o and theres `Scramblers` 2 take a number and make it like wiggly jiggly or smth

# o wate moar!!!

now there like all these like um `RawNoise` types

each 1 has liek a diff flavor i guess?

`PerlinNoise` is all squarey but also liek blobby

`SimplexNoise` is liek triagnles n stuffs

`ValueNoise` is real dum squarezzz so u better use it wit somethin else

`HoneyNoise` mixiz it up wit `SimplexNoise` and `ValueNoise` all hot n hevay

`FoamNoise` is super orgnanic an natrual lookin

`CyclicNoise` uses som liek rotaiton stuf an looks real diff

`SorbetNoise` tastes niiiice but its also liek `CyclicNoise` and bettr in 2D i think?

an those 2 use liek `RotationTools` but u prolly dont have 2 lololol

`PerlueNoise` is liek Perlin but aslo makin Value at teh saem tiem liek, an then woop woop mixes em up!

`OpenSimplex2FastNoise` is tht fancy OpenSimplex2 thgin in fassssst moddddde

`OpenSimplex2SmoothNoise` is tht facny OpenSimplex2 thign in smooooth moddddde

then u can give 1 of thoes 2 `ContinuousNoise` n get liek rideged mode n octaevs n stuf

`ContinuousNoise` is siccnastee cooll dawg an u shuod use it lotts!!!11!

these r all liek `Json.Serializable` so u can liek saev and laod stuf

`LineWobble` is kinda liek those but jus 4 1D noise

it liek um gose upp an dowwn wit a um wiglgy wbobly waev but theres different waevs i thikn

wut even is `GradientVectors`??? dont use it lol i gess???

# even moar!!!!!!11!!!

`Compatibility` maeks ur bit stuff work on the googles web tool kit thingy

`ColorSupport` supports ur colors an stuff liek w hue an satruation an all that

`PoissonDiskDiscrete` maeks poitns placed far aways from each ohter, but liek on a liek grid

`PoissonDiskSmooth` maeks poitns placed far aways from each ohter, but liek not on um a grid

`PointHasher` is liek `Scrambler` but 4 a bunch of numburs insted of liek 1

`EncryptedFileHandle` is liek lamebad encarption 4 ur filez but it does liek hide ur images and scriptz

teh encarption is not 4 web tho :(

`RoughMath` is omg so itchy n rough but has superfast math if u can handle a lil mess >:)

`MathSupport` dos liek basick mathy thigns tht shulod b suporpted as liek standerd

`Distributor` taeks liek unfiorm nubmers n maeks them nomraler

liek wut >:O um whu siad its nomral thees nubmers are liek rly loooogn demcials

but i think this new stuf is p. cool

# an then theres liek anothr i guess

`PointSequence` has liek diff classes insied 4 maekin a buncha poitns but thye wont be nreaby in liek forever

`PointSequence.Halton2` is a 2D one an u can cnofgiure its bases or smth

`PointSequence.R2` what evne is an R liek omg. but it also maeks a buncha 2D poitns an thye wont be cloes

theres like a lot more Ds 4 each of thees, 3D an 4D an 5D an 6D wowowow

`Vector5` an `Vector6` r xtendorizatioins on libgdx `Vector2`, `Vector3`, an `Vector4`, 4 biggar D lololol

# cereal

u can liek cerealize an decerealize stuffs in here

with like

`Json` from [libgdx](https://libgdx.com), out of teh box!!!

or `Kryo` 4 bianry storaeg usign [kryo-more](https://github.com/tommyettinger/kryo-more)

or ~~furry~~ ~~fury~~ `Fory` 4 moar bianry storaeg from [teh apache foudnatoin](https://fory.apache.org), but also liek, out of teh box!!!

or sometimes stuff has codes that liek wriets 2 a String or reads frm 1

im not drukn ur drnuk

heeheeeeeeee

bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb

# get

The easiest way to use this library was to copy files from the source code.
... But, that was only in the earliest versions. In 0.1.0 and newer, you should probably use
Gradle (or Maven or whatever your chosen build tool is) to depend on cringe.

You can depend on the library as a whole, using it as a normal Gradle or Maven dependency.

`api "com.github.tommyettinger:cringe:0.2.2"`

If you use GWT, then your GWT module needs to depend on:

`implementation "com.github.tommyettinger:cringe:0.2.2:sources"`

GWT also needs this `inherits` line added to your `GdxDefinition.gwt.xml` file, with the other inherits lines:

`<inherits name="com.github.tommyettinger.cringe" />`

You don't need the `inherits` line if you just copy a few classes into your sources.

The four [GdxRandom](src/main/java/com/github/tommyettinger/cringe/GdxRandom.java) implementations:
([RandomAce320](src/main/java/com/github/tommyettinger/cringe/RandomAce320.java),
[RandomDistinct64](src/main/java/com/github/tommyettinger/cringe/RandomDistinct64.java),
[RandomXMX256](src/main/java/com/github/tommyettinger/cringe/RandomXMX256.java)),
and [RandomChop128](src/main/java/com/github/tommyettinger/cringe/RandomChop128.java))
can be used as drop-in replacements for `java.util.Random` or an
almost-identical replacement for libGDX's `RandomXS128`.

The different GdxRandom implementations differ in various properties. All are high-quality statistically, passing
the PractRand battery of tests to 64TB of tested data. They each have different "periods," meaning the number of random
results that can be produced before the sequence repeats. In practice, a game will probably never have to worry about
exhausting an entire period and actually seeing the sequence repeat. However, far enough along through the actual period
of a generator, it becomes possible for an adversary (typically a player) to notice and exploit patterns. The shortest
I believe this could happen predictably is in `RandomDistinct64`, after over 4 billion random numbers are generated (2
to the 32). It can theoretically happen earlier in `RandomChop128`, which has a minimum guaranteed period of 2 to the 64
calls to `nextInt()`, but in practice, encountering a period shorter than 2 to the 40-something is vanishingly unlikely
(buy a lottery ticket if you encounter it, why not), putting the closest point that might be predictable at over a
million generated `int`s. For `RandomXMX256`, the earliest point should be 2 to the 128, which just means it isn't going
to happen. `AceRandom320` has many different possible cycles, with the shortest possible one at least as long as
`RandomDistinct64`'s period.

`RandomAce320` is very fast. It uses very few types of instruction, and only performs one math instruction on each state
to get a random result. It also can perform those 5 math instructions using instruction-level parallelism, which the JVM
has been good at exploiting to improve performance since Java 16 or so. The minimum cycle length is 2 to the 64, and the
maximum is over 2 to the 300 (but it's impractical to say exactly). Most random seeds will be in one of the largest
cycles, and even in the minuscule chance that you randomly start in the shortest cycle, that's still almost always going
to be good enough for any app, and effectively always enough for any game. It has five `long`s of state, each of which
can have any value. This generator relies entirely on its state transition to appear random.

`RandomDistinct64` produces each `long` from `nextLong()` exactly once, which makes it good for producing random UUIDs.
It's also rather fast on most JVMs, and might be much faster on GraalVM (version 20 and later). I need to verify some
unusual benchmarks using GraalVM 20, but they appear to show RandomDistinct64 producing over 4 billion long values per
second, which is over twice as fast as RandomAce320. This could be the benchmark code eliminating the tested loop
entirely, so take it with a large grain of salt. It has one `long` of state, which can have any value. This generator
relies entirely on its output function to appear random, and doesn't have a random-like state transition at all.

`RandomXMX256` is a fortified version of `Xoshiro256**`, which is present in Java 17, but removes its `**` or StarStar
"scrambler" and replaces it with the much more robust MX3 unary hash, which is also used in our `Scramblers` class.
This generator produces each sequence of four numbers exactly once, except for `0, 0, 0, 0`, which never appears.
RandomXMX256 is slower than the other two generators, but should be much more resilient against statistical quality
issues, probably to the point of being overkill. That's still only overkill for the most common usages, with either
one generator producing many random numbers, or many generators with random initial states -- if the initial states
are extremely similar, patterns may become observable over time. It has 4 `long`s of state, each of which can have any
value unless all states are 0 (which is not permitted). This generator has a mildly random state transition and an
extremely thorough output function (also called a scrambler).

`RandomChop128` is meant primarily for games that target GWT (or may sometimes target GWT), and it is optimized so it
does almost all possible math on 32-bit values. It's "super-sourced" so a different file is used when GWT compiles it,
and all the GWT quirks are isolated to that file (in the `emu` folder, which isn't built otherwise). This is a subcycle
generator, like `RandomAce320`, but has 4 states instead of 5, and its counter state only goes through 2 to the 32
values instead of `RandomAce320`'s 2 to the 64 values. It also relies entirely on its state transition to appear random,
and it returns its `stateC` verbatim on the first call to `nextInt()`. As a result, it's a good idea to seed one of
these with `setSeed(long)` instead of `setState(long, long, long, long)`, since it randomizes all states well.

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
large amount of identifiers have been produced (2 to the 128 minus 1). Unlike the JDK class UUID, UniqueIdentifier is
compatible with GWT. Random UniqueIdentifier values are less likely to collide than random UUID values, though in both
cases the chance of a collision is mind-bogglingly rare.

In more recent versions, `cringe` has gotten larger, and now has code for continuous noise, which is fittingly
random-like, and `EncryptedFileHandle`s, which are akin to randomized, but reversible, scrambling of files.
The noise classes are almost always used with a `ContinuousNoise` object wrapping some `RawNoise` implementation.
The ContinuousNoise provides the most useful features, but the RawNoise provides the underlying algorithm. The
noise classes are more inter-dependent; most RawNoise types use `LineWobble` to provide 1D noise, and classes like
`FoamNoise` and `HoneyNoise` call `ValueNoise` methods directly. Meanwhile, `SorbetNoise` inherits from `CyclicNoise`,
and there's lots of other connections. More noise classes have been added more recently.
To use the noise, you should probably have a dependency on all of `cringe`.

This also adds `Vector5` and `Vector6` to the `Vector2` through `Vector4` libGDX provides, and the `PointSequence`
class' implementations can generate the appropriate type of Vector for a given dimension. `PointSequence` produces
either Halton sequences or R2-like sequences, and can be 2D through 6D. The `Distributor` class handles conversion
from `double`, `int`, or `long` values to normal-distributed ones. `RoughMath` provides approximations to various
math functions. There was briefly a `Ziggurat` class, but it has been merged entirely into `Distributor`.

# license

[apache 2.0](LICENSE)

thanks 2 paul mineiro 4 makin these liek [blazign fast apropximations](https://code.google.com/archive/p/fastapprox/)
w liek bit haxx

those apropximations are [new bsd license](https://opensource.org/license/BSD-3-Clause) which should be cool i think???

also thanks 2 rafa skoberg for like givin me a ton of work 2 do lololol jk

n thanks 2 raeleus since ur liek the king of cringe

# yeah

now put on ur robe and wizard hat

its time for number magic
