package com.github.tommyettinger.cringe;

public class ScramblingChecks {
    public static void mainLong(String[] args){
        long idx = 0x31000000000L;
        for (int i = 0; i < 2048; i++) {
            for (int j = 0; j < 0x40000000; j++) {
                if(idx == Scramblers.scramble(idx))
                    System.err.printf("\n\n 0x%016X is a fixed-point.\n\n", idx);
                idx++;
            }
            System.out.print((i+1) + "/2048... ");
        }
        System.out.printf("\n\n\nGot through 0x%XL indices.\n", idx);
    }
    public static int scrambleInt(int x) {
        x ^= x >>> 16;
        x = Compatibility.imul(x, 0x236F7153);
        x ^= x >>> 12;
        x = Compatibility.imul(x, 0x33CD8663);
        x ^= x >>> 15;
        x = Compatibility.imul(x, 0x3E06B66B);
        x ^= x >>> 16;
        return x;
    }

    /**
     * 0x8329E33B is a fixed-point for a=1.
     * 0x17FD53DC is a fixed-point for a=2.
     * 0xDAC33353 is a fixed-point for a=2.
     * 0x37B8EFA7 is a fixed-point for a=4.
     * 0x976BF754 is a fixed-point for a=4.
     * 0xE4F04311 is a fixed-point for a=4.
     * 0x4428DE41 is a fixed-point for a=5.
     * 0x45AE2C1B is a fixed-point for a=5.
     * 0x8B3C89E3 is a fixed-point for a=6.
     * 0xA152A2DF is a fixed-point for a=7.
     * 0xFDEEACEC is a fixed-point for a=7.
     * 0x05DC4BBB is a fixed-point for a=12.
     * 0x29A00EC3 is a fixed-point for a=12.
     * 0x10E6F189 is a fixed-point for a=13.
     * @param args ignored
     */
    public static void mainAdding(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == scrambleInt(idx + a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * In 0.3.1: 0xBD0F4FA9 and 0xE7AFC875 are fixed-points for scrambleInt().
     */
    public static void main(String[] args){
        int idx = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 0x10000000; j++) {
                if(idx == Scramblers.scrambleInt(idx))
                    System.err.printf(" 0x%08X is a fixed-point.\n", idx);
                idx++;
            }
            System.out.println("Completed " + (i+1) + "/16");
        }
    }
}
