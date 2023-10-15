package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SerializationTest {
    @Test
    public void testRoundTrip() {
        List<GdxRandom> all = Arrays.asList(new RandomDistinct64(-1L), new RandomXMX256(-1L), new RandomAce320(-1L));
        for (GdxRandom r : all) {
            String s = r.stringSerialize();
            r.nextLong();
            long rl = r.nextLong();
            r.setSeed(rl);
            GdxRandom de = r.copy().stringDeserialize(s);
            System.out.println(s + "   " + de.stringSerialize());
            de.nextLong();
            long dl = de.nextLong();
            Assert.assertEquals("Failure with " + s, rl, dl);
        }
    }

    @Test
    public void testJsonRoundTrip() {
        Json json = new Json();
        List<GdxRandom> all = Arrays.asList(new RandomDistinct64(-1L), new RandomXMX256(-1L), new RandomAce320(-1L));
        for (GdxRandom r : all) {
            String s = json.toJson(r);
            System.out.println(s);
            r.nextLong();
            long rl = r.nextLong();
            r.setSeed(rl);
            GdxRandom de = json.fromJson(r.getClass(), s);
            System.out.println(s + "   " + json.toJson(de));
            de.nextLong();
            long dl = de.nextLong();
            Assert.assertEquals("Failure with " + s, rl, dl);
        }
    }

    @Test
    public void testGapShufflerRoundTrip() {
        Json json = new Json();
        GapShuffler<String> r = new GapShuffler<>(
                new String[]{"IT'S", "PEANUT", "BUTTER", "JELLY", "TIME"}, new RandomAce320(123));
        String s = json.toJson(r);
        System.out.println(s);
        r.next();
        String rl = r.next();
        GapShuffler<String> d = json.fromJson(GapShuffler.class, s);
        System.out.println(s + "   " + json.toJson(d));
        d.next();
        String dl = d.next();
        Assert.assertEquals("Failure with " + s, rl, dl);
        System.out.println(r.next() + " " + r.next() + " " + r.next() + " " + r.next() + " " + r.next());
        System.out.println(d.next() + " " + d.next() + " " + d.next() + " " + d.next() + " " + d.next());
    }
}
