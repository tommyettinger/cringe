package com.github.tommyettinger.cringe;

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
}
