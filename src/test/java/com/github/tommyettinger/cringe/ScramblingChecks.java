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
     * Testing the xor-encrypt-xor system with the same xor value on both sides:
     * 0x00000001 is a fixed-point for a=1.
     * 0x54F4B636 is a fixed-point for a=1.
     * 0xA26E8047 is a fixed-point for a=1.
     * 0x00000002 is a fixed-point for a=2.
     * 0x54F4B635 is a fixed-point for a=2.
     * 0xA26E8044 is a fixed-point for a=2.
     * 0x00000003 is a fixed-point for a=3.
     * 0x54F4B634 is a fixed-point for a=3.
     * 0xA26E8045 is a fixed-point for a=3.
     * 0x00000004 is a fixed-point for a=4.
     * 0x54F4B633 is a fixed-point for a=4.
     * 0xA26E8042 is a fixed-point for a=4.
     * 0x00000005 is a fixed-point for a=5.
     * 0x54F4B632 is a fixed-point for a=5.
     * 0xA26E8043 is a fixed-point for a=5.
     * 0x00000006 is a fixed-point for a=6.
     * 0x54F4B631 is a fixed-point for a=6.
     * 0xA26E8040 is a fixed-point for a=6.
     * 0x00000007 is a fixed-point for a=7.
     * 0x54F4B630 is a fixed-point for a=7.
     * 0xA26E8041 is a fixed-point for a=7.
     * 0x00000008 is a fixed-point for a=8.
     * 0x54F4B63F is a fixed-point for a=8.
     * 0xA26E804E is a fixed-point for a=8.
     * 0x00000009 is a fixed-point for a=9.
     * 0x54F4B63E is a fixed-point for a=9.
     * 0xA26E804F is a fixed-point for a=9.
     * 0x0000000A is a fixed-point for a=10.
     * 0x54F4B63D is a fixed-point for a=10.
     * 0xA26E804C is a fixed-point for a=10.
     * 0x0000000B is a fixed-point for a=11.
     * 0x54F4B63C is a fixed-point for a=11.
     * 0xA26E804D is a fixed-point for a=11.
     * 0x0000000C is a fixed-point for a=12.
     * 0x54F4B63B is a fixed-point for a=12.
     * 0xA26E804A is a fixed-point for a=12.
     * 0x0000000D is a fixed-point for a=13.
     * 0x54F4B63A is a fixed-point for a=13.
     * 0xA26E804B is a fixed-point for a=13.
     * 0x0000000E is a fixed-point for a=14.
     * 0x54F4B639 is a fixed-point for a=14.
     * 0xA26E8048 is a fixed-point for a=14.
     * 0x0000000F is a fixed-point for a=15.
     * 0x54F4B638 is a fixed-point for a=15.
     * 0xA26E8049 is a fixed-point for a=15.
     * <br>
     * Exactly 3 fixed-points for each, with patterns for each!
     *
     * @param args
     */
    public static void mainXEX(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a) ^ a))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * XOR with {@code a} before passing to scrambleInt().
     * <br>
     * 0x17FD53DC is a fixed-point for a=2.
     * 0x9384DF05 is a fixed-point for a=4.
     * 0xE4F04311 is a fixed-point for a=4.
     * 0xD43A4C34 is a fixed-point for a=6.
     * 0xDAC33353 is a fixed-point for a=6.
     * 0x4428DE41 is a fixed-point for a=7.
     * 0x8329E33B is a fixed-point for a=7.
     * 0x8B3C89E3 is a fixed-point for a=10.
     * 0x681470DC is a fixed-point for a=11.
     * 0x29A00EC3 is a fixed-point for a=12.
     * 0x37B8EFA7 is a fixed-point for a=12.
     * 0x976BF754 is a fixed-point for a=12.
     *
     * @param args
     */
    public static void main(String[] args){
        int idx = 0;
        for (int a = 1; a < 16; a++) {
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 0x10000000; j++) {
                    if (idx == (scrambleInt(idx ^ a)))
                        System.err.printf("0x%08X is a fixed-point for a=%d.\n", idx, a);
                    idx++;
                }
            }
        }
    }

    /**
     * In 0.3.1: 0xBD0F4FA9 and 0xE7AFC875 are fixed-points for scrambleInt().
     */
    public static void mainInt(String[] args){
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
