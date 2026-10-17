package com.github.tommyettinger.cringe;

/**
 * In 0.3.1: 0xBD0F4FA9 and 0xE7AFC875 are fixed-points.
 */
public class ScramblingChecks {
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
