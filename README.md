# cringe

like omg so random

but like

with gdx

# get

The easiest way to use this library is to copy files from the source code.
If you copy [GdxRandom](src/main/java/com/github/tommyettinger/cringe/GdxRandom.java)
and one of the three `Random` classes adjacent to it
([RandomAce320](src/main/java/com/github/tommyettinger/cringe/RandomAce320.java),
[RandomDistinct64](src/main/java/com/github/tommyettinger/cringe/RandomDistinct64.java),
or [RandomXMX256](src/main/java/com/github/tommyettinger/cringe/RandomXMX256.java)), you
can use the `Random` class as a drop-in replacement for `java.util.Random` or an
almost-identical replacement for libGDX's `RandomXS128`. There's also
[Scramblers](src/main/java/com/github/tommyettinger/cringe/Scramblers.java), which is
purely static and provides methods to "scramble" `long` input into different formats.

# license

[apache 2.0](LICENSE)