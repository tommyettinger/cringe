package com.github.tommyettinger.cringe;

public class NoiseOutput {
    public static void main(String[] args) {
        RawNoise raw = new FoamNoise(123);
        ContinuousNoise noise = new ContinuousNoise(raw, 123, 0.05f, ContinuousNoise.FBM, 2);
        float[][][] field = new float[40][40][20];
        System.out.print("float[][][] noise = new float[][][]{ ");
        for (int x = 0; x < field.length; x++) {
            System.out.print("  { ");
            for (int y = 0; y < field[x].length; y++) {
                System.out.print("    { ");
                for (int z = 0; z < field[x][y].length; z++) {
                    field[x][y][z] = noise.getNoise(x, y, z);
                    System.out.printf("%10.8ff, ", field[x][y][z]);
                }
                System.out.println("},");
            }
            System.out.println("  },");
        }
        System.out.println("};");
    }
}
