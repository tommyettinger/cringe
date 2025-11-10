package com.github.tommyettinger.cringe;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import org.apache.fory.Fory;
import org.apache.fory.config.Language;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class SerializationTest {
    @Test
    public void testGdxRandomString() {
        List<GdxRandom> all = Arrays.asList(new RandomDistinct64(-1L), new RandomXMX256(-1L), new RandomAce320(-1L), new RandomChop128(-1L));
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
        List<GdxRandom> all = Arrays.asList(new RandomDistinct64(-1L), new RandomXMX256(-1L), new RandomAce320(-1L), new RandomChop128(-1L));
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
    public void testGdxRandomFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(RandomDistinct64.class);
        fory.register(RandomXMX256.class);
        fory.register(RandomAce320.class);
        fory.register(RandomChop128.class);
        List<GdxRandom> all = Arrays.asList(new RandomDistinct64(-1L), new RandomXMX256(-1L), new RandomAce320(-1L), new RandomChop128(-1L));
        for (GdxRandom r : all) {
            GdxRandom cpy = r.copy();
            byte[] s = fory.serializeJavaObject(r);
            r.nextLong();
            long rl = r.nextLong();
            r.setSeed(rl);
            GdxRandom de = fory.deserializeJavaObject(s, r.getClass());
            System.out.println(cpy + "   " + de);
            de.nextLong();
            long dl = de.nextLong();
            Assert.assertEquals("Failure with " + r.getClass(), rl, dl);
        }
    }

    @Test
    public void testGapShufflerJson() {
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
    public void testGapShufflerFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(Array.class);
        fory.register(RandomAce320.class);
        fory.register(GapShuffler.class);
        GapShuffler<String> orig = new GapShuffler<>(
                new String[]{"IT'S", "PEANUT", "BUTTER", "JELLY", "TIME"}, new RandomAce320(123));
        GapShuffler<String> cpy = new GapShuffler<>(orig);
        byte[] ser = fory.serializeJavaObject(orig);
        orig.next();
        String ores = orig.next();
        GapShuffler<?> dser = fory.deserializeJavaObject(ser, GapShuffler.class);
        dser.next();
        String dres = (String) dser.next();
        Assert.assertEquals("Failure with " + cpy, ores, dres);
        System.out.println(orig.next() + " " + orig.next() + " " + orig.next() + " " + orig.next() + " " + orig.next());
        System.out.println(dser.next() + " " + dser.next() + " " + dser.next() + " " + dser.next() + " " + dser.next());
    }

    @Test
    public void testWeightedTableJson() {
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
    public void testWeightedTableFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(RandomAce320.class);
        fory.register(WeightedTable.class);
        WeightedTable orig = new WeightedTable(new RandomAce320(123), 1.1f, 2.2f, 3.3f, 4.4f, 5.5f), cpy = orig.copy();
        byte[] ser = fory.serializeJavaObject(orig);
        orig.random();
        int ores = orig.random();
        WeightedTable dser = fory.deserializeJavaObject(ser, WeightedTable.class);
        System.out.println(cpy + "   " + dser.toString());
        dser.random();
        int dres = dser.random();
        Assert.assertEquals("Failure with " + cpy, ores, dres);
        System.out.println(orig.random() + " " + orig.random() + " " + orig.random() + " " + orig.random() + " " + orig.random());
        System.out.println(dser.random() + " " + dser.random() + " " + dser.random() + " " + dser.random() + " " + dser.random());
    }

    @Test
    public void testUniqueIdentifierJson() {
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

    @Test
    public void testUniqueIdentifierString() {
        UniqueIdentifier orig = UniqueIdentifier.next();
        String ser = orig.stringSerialize();
        System.out.println(ser);
        UniqueIdentifier dser = new UniqueIdentifier().stringDeserialize(ser);
        System.out.println(ser + " deserializes to " + dser.stringSerialize());
        Assert.assertEquals("Failure with " + ser, orig, dser);
        String serG = UniqueIdentifier.GENERATOR.stringSerialize();
        orig = UniqueIdentifier.next();
        orig = UniqueIdentifier.next();
        UniqueIdentifier.GENERATOR.stringDeserialize(serG);

        dser = UniqueIdentifier.next();
        dser = UniqueIdentifier.next();
        Assert.assertEquals(orig, dser);
    }

    @Test
    public void testUniqueIdentifierFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(UniqueIdentifier.class);
        fory.register(UniqueIdentifier.Generator.class);
        UniqueIdentifier orig = UniqueIdentifier.next();
        byte[] ser = fory.serializeJavaObject(orig);
        UniqueIdentifier dser = fory.deserializeJavaObject(ser, UniqueIdentifier.class);
        System.out.println(orig + " deserializes to " + dser);
        Assert.assertEquals("Failure with " + dser, orig, dser);
        byte[] serG = fory.serializeJavaObject(UniqueIdentifier.GENERATOR);
        UniqueIdentifier.next();
        orig = UniqueIdentifier.next();
        UniqueIdentifier.GENERATOR = fory.deserializeJavaObject(serG, UniqueIdentifier.Generator.class);
        UniqueIdentifier.next();
        dser = UniqueIdentifier.next();
        Assert.assertEquals(orig, dser);
    }

    @Test
    public void testRawNoiseString() {
        Array<RawNoise> all = RawNoise.Serializer.getAll();
        for (RawNoise r : all) {
            String s = r.stringSerialize();
            float rl = r.getNoise(0.2f, 0.3f, 0.5f);
            RawNoise de = r.stringDeserialize(s);
            System.out.println(s + "   " + de.stringSerialize());
            float dl = de.getNoise(0.2f, 0.3f, 0.5f);
            Assert.assertEquals("Failure with " + s, rl, dl, 0.0001f);
        }
    }

    @Test
    public void testRawNoiseJson() {
        Json json = new Json();
        Array<RawNoise> all = RawNoise.Serializer.getAll();
        for (RawNoise r : all) {
            String s = json.toJson(r);
            float rl = r.getNoise(0.2f, 0.3f, 0.5f);
            RawNoise de = json.fromJson(r.getClass(), s);
            System.out.println(s + "   " + json.toJson(de));
            float dl = de.getNoise(0.2f, 0.3f, 0.5f);
            Assert.assertEquals("Failure with " + s, rl, dl, 0.0001f);
        }
    }

    @Test
    public void testRawNoiseFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        Array<RawNoise> all = RawNoise.Serializer.getAll();
        for (RawNoise r : all) {
            fory.register(r.getClass());
        }
        for (RawNoise r : all) {
            byte[] s = fory.serializeJavaObject(r);
            float rl = r.getNoise(0.2f, 0.3f, 0.5f);
            RawNoise de = fory.deserializeJavaObject(s, r.getClass());
            System.out.println(r + "   " + de);
            float dl = de.getNoise(0.2f, 0.3f, 0.5f);
            Assert.assertEquals("Failure with " + s, rl, dl, 0.0001f);
        }
    }

    @Test
    public void testContinuousNoiseJson() {
        Json json = new Json();
        Array<RawNoise> all = RawNoise.Serializer.getAll();
        for (RawNoise rn : all) {
            ContinuousNoise cn = new ContinuousNoise(rn, 1234, 0.3f, ContinuousNoise.WARP, 3);
            String s = json.toJson(cn);
            float rl = cn.getNoise(0.2f, 0.3f, 0.5f);
            RawNoise de = json.fromJson(cn.getClass(), s);
            System.out.println(s + "   " + json.toJson(de));
            float dl = de.getNoise(0.2f, 0.3f, 0.5f);
            Assert.assertEquals("Failure with " + s, rl, dl, 0.0001f);
        }
    }

    @Test
    public void testContinuousNoiseFory() {
        Fory fory = Fory.builder().withLanguage(Language.JAVA).build();
        fory.register(ContinuousNoise.class);
        Array<RawNoise> all = RawNoise.Serializer.getAll();
        for (RawNoise rn : all) {
            fory.register(rn.getClass());
        }
        for (RawNoise rn : all) {
            ContinuousNoise cn = new ContinuousNoise(rn, 1234, 0.3f, ContinuousNoise.WARP, 3);
            byte[] s = fory.serializeJavaObject(cn);
            float rl = cn.getNoise(0.2f, 0.3f, 0.5f);
            RawNoise de = fory.deserializeJavaObject(s, cn.getClass());
            System.out.println(cn + "   " + de);
            float dl = de.getNoise(0.2f, 0.3f, 0.5f);
            Assert.assertEquals("Failure with " + s, rl, dl, 0.0001f);
        }
    }

    @Test
    public void testUUIDJson() {
        Json json = new Json();
        json.setSerializer(UUID.class, new Json.Serializer<UUID>() {
            @Override
            public void write(Json json, UUID uuid, Class aClass) {
                json.writeObjectStart();
                json.writeValue("id", uuid.toString(), String.class);
                json.writeObjectEnd();
            }

            @Override
            public UUID read(Json json, JsonValue jsonValue, Class aClass) {
                return UUID.fromString(json.readValue("id", String.class, jsonValue));
            }
        });
        for (int i = 0; i < 10; i++) {
            UUID orig = UUID.randomUUID();
            String ser = json.toJson(orig);
            System.out.println(ser);
            UUID dser = json.fromJson(UUID.class, ser);
            System.out.println(ser + "   " + json.toJson(dser));
            Assert.assertEquals("Failure with " + ser, orig, dser);
        }
    }

}
