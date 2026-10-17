package com.github.tommyettinger.cringe;

/**
 * In 0.3.1: 0xBD0F4FA9 and 0xE7AFC875 are fixed-points for scrambleInt().
 */
public class ScramblingChecks {
    public static void main(String[] args){
        long idx = 0x11000000000L;
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
