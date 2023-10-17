package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Json;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class SerializationTest {
    @Test
    public void testGdxRandomString() {
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
    public void testGdxRandomJson() {
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
    public void testGapShuffler() {
        Json json = new Json();
        GapShuffler<String> orig = new GapShuffler<>(
                new String[]{"IT'S", "PEANUT", "BUTTER", "JELLY", "TIME"}, new RandomAce320(123));
        String ser = json.toJson(orig);
        System.out.println(ser);
        orig.next();
        String ores = orig.next();
        GapShuffler<String> dser = json.fromJson(GapShuffler.class, ser);
        System.out.println(ser + "   " + json.toJson(dser));
        dser.next();
        String dres = dser.next();
        Assert.assertEquals("Failure with " + ser, ores, dres);
        System.out.println(orig.next() + " " + orig.next() + " " + orig.next() + " " + orig.next() + " " + orig.next());
        System.out.println(dser.next() + " " + dser.next() + " " + dser.next() + " " + dser.next() + " " + dser.next());
    }

    @Test
    public void testWeightedTable() {
        Json json = new Json();
        WeightedTable orig = new WeightedTable(new RandomAce320(123), 1.1f, 2.2f, 3.3f, 4.4f, 5.5f);
        String ser = json.toJson(orig);
        System.out.println(ser);
        orig.random();
        int ores = orig.random();
        WeightedTable dser = json.fromJson(WeightedTable.class, ser);
        System.out.println(ser + "   " + json.toJson(dser));
        dser.random();
        int dres = dser.random();
        Assert.assertEquals("Failure with " + ser, ores, dres);
        System.out.println(orig.random() + " " + orig.random() + " " + orig.random() + " " + orig.random() + " " + orig.random());
        System.out.println(dser.random() + " " + dser.random() + " " + dser.random() + " " + dser.random() + " " + dser.random());
    }

    @Test
    public void testUniqueIdentifier() {
        Json json = new Json();
        UniqueIdentifier orig = UniqueIdentifier.next();
        String ser = json.toJson(orig);
        System.out.println(ser);
        UniqueIdentifier dser = json.fromJson(UniqueIdentifier.class, ser);
        System.out.println(ser + "   " + json.toJson(dser));
        Assert.assertEquals("Failure with " + ser, orig, dser);
        String serG = json.toJson(UniqueIdentifier.GENERATOR);
        System.out.println(serG);
        orig = UniqueIdentifier.next();
        orig = UniqueIdentifier.next();
        UniqueIdentifier.GENERATOR = json.fromJson(UniqueIdentifier.Generator.class, serG);
        dser = UniqueIdentifier.next();
        dser = UniqueIdentifier.next();
        Assert.assertEquals(orig, dser);
    }
}
